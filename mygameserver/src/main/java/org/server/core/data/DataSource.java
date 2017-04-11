package org.server.core.data;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ftamework data source
 */
public class DataSource {
	static final Logger log = LoggerFactory.getLogger(DataSource.class);
	static SessionFactory sessionFactory;
	
	public static final List<Class<?>> otherMappingsClasses = new ArrayList<Class<?>>();
	
	/**
	 * 初始化 hibernate 工厂
	 * 
	 * @param driver
	 *            驱动
	 * @param url
	 *            连接地址
	 * @param user
	 *            用户名
	 * @param password
	 *            密码
	 * 
	 */
	static void initializeFactory(String driver, String url, String user, String password, int fixedConnectionSize) {
		try {
			// 引导 hibernate 日志配置 see
			System.setProperty("org.jboss.logging.provider", "slf4j");
			// A StandardServiceRegistryBuilder
			StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder().configure(); 
			registryBuilder.applySetting("hibernate.connection.driver_class", driver);
			registryBuilder.applySetting("hibernate.connection.url", url);
			registryBuilder.applySetting("hibernate.connection.username", user);
			registryBuilder.applySetting("hibernate.connection.password", password);

			// overwrite dialect
			String usingDialect = "";
			if (driver.toLowerCase().contains("mysql"))
				usingDialect = "org.hibernate.dialect.MySQLDialect";
			else if (driver.toLowerCase().contains("oracle"))
				usingDialect = "org.hibernate.dialect.Oracle10gDialect";

			registryBuilder.applySetting("hibernate.dialect", usingDialect);
			log.info("[DataSource]::hibernate using dialect {}.", usingDialect);

			// config with proxool
			ProxoolHelper.addProxoolConfig(registryBuilder);
			ProxoolHelper.configProxoolAlias(user, password, url, driver, fixedConnectionSize);

			// A SessionFactory is set up once for an application!
			final StandardServiceRegistry registry = registryBuilder.build();
			MetadataSources metadataSources = new MetadataSources(registry);

			// attach other mappings
			for (Class<?> cls : otherMappingsClasses)
				metadataSources.addAnnotatedClass(cls);

			// build session factory
			sessionFactory = metadataSources.buildMetadata().buildSessionFactory();
		} catch (Throwable ex) {
			log.error("Hibernate SessionFactory Build Failer !: ", ex);
		}
	}

	/**
	 * 初始化数据库工具
	 * 
	 * @param driver
	 *            驱动
	 * @param url
	 *            连接地址
	 * @param user
	 *            用户名
	 * @param password
	 *            密码
	 * @return 初始化是否成功
	 */
	public static boolean initialize(String driver, String url, String user, String password, int fixedConnectionSize) {
		try {
			Class<?> driverClass = Class.forName(driver);
			if (driverClass == null)
				return false;
			Driver driverObject = (Driver) driverClass.newInstance();
			if (driverObject == null)
				return false;
			DriverManager.registerDriver(driverObject);
			try {
				try (Connection connection = DriverManager.getConnection(url, user, password)) {
				}
			} catch (Throwable e) {
				log.error("[DataSource] try connect database has a error : [{}:{}] !", e.getClass().getName(),
						e.getMessage());
				return false;
			}
		} catch (Throwable throwable) {
			return false;
		}
		initializeFactory(driver, url, user, password, fixedConnectionSize);
		return true;
	}

	/**
	 * 打开会话
	 *
	 * @return Hibernate 数据会话
	 */
	public static Session openSession() {
		if (sessionFactory == null) {
			log.error("[DataSource]  Plase First Call Initialize Function . !");
			return null;
		}

		Session session = sessionFactory.openSession();
		return session;
	}

	/**
	 * 打开会话
	 *
	 * @return Hibernate 数据会话
	 */
	public static StatelessSession openStatelessSession() {

		if (sessionFactory == null) {
			log.error("[DataSource]  Plase First Call Initialize Function . !");
			return null;
		}

		StatelessSession session = sessionFactory.openStatelessSession();
		return session;
	}

	/**
	 * 插入模块对象到数据库
	 *
	 * @param objs
	 *            对象集合
	 * @return 是否插入成功
	 */
	public static boolean insert(Object... objs) {

		if (sessionFactory == null) {
			log.error("[DataSource]  Plase First Call Initialize Function . !");
			return false;
		}

		return insertOrUpdate(false, objs);
	}

	/**
	 * 更新模块对象
	 *
	 * @param objs
	 *            对象集合
	 * @return 是否更新成功
	 */
	public static boolean update(Object... objs) {

		if (sessionFactory == null) {
			log.error("[DataSource]  Plase First Call Initialize Function . !");
			return false;
		}

		return insertOrUpdate(true, objs);
	}

	/**
	 * 从数据库删除模块对象
	 *
	 * @param objs
	 *            对象集合
	 * @return 是否删除成功
	 */
	public static boolean delete(Object... objs) {
		if (sessionFactory == null) {
			log.error("[DataSource]  Plase First Call Initialize Function . !");
			return false;
		}
		return usingStatelessSession(session -> {
			for (Object o : objs) {
				if (o != null) {
					session.delete(o);
				}
			}
		});
	}

	/**
	 * 尝试初始化对象
	 *
	 * @param entity
	 *            实体
	 * @param obj
	 *            对象
	 * @return 初始化是否成功
	 */
	public static boolean tryInitialize(Object entity, Object obj) {
		if (sessionFactory == null) {
			log.error("[DataSource]  Plase First Call Initialize Function . !");
			return false;
		}
		if (!Hibernate.isInitialized(obj)) {
			Object entity_ = entity;
			try {
				Hibernate.initialize(obj);
			} catch (Exception e) {
				usingSession(s -> {
					s.update(entity_);
					Hibernate.initialize(obj);
				});
			}
		}
		return true;
	}

	/**
	 * 插入或者更新对象列表
	 *
	 * @param update
	 *            true 为 更新 false 为插入
	 * @param objs
	 *            数据实体模块
	 * @return 操作是否成功
	 */
	public static boolean insertOrUpdate(boolean update, Object[] objs) {
		if (sessionFactory == null) {
			log.error("[DataSource]  Plase First Call Initialize Function . !");
			return false;
		}
		return usingStatelessSession(session -> {
			try {
				for (Object o : objs)
					if (update)
						session.update(o);
					else
						session.insert(o);
			} catch (Throwable e) {
				log.error("[DataSource::insertOrUpdate] insert or update entity throw exception .", e);
				throw e;
			}
		});
	}

	static boolean usingSession(Consumer<Session> action) {
		if (sessionFactory != null) {
			Session session = openSession();
			try {
				Transaction transaction = session.beginTransaction();
				action.accept(session);
				transaction.commit();
				return true;
			} catch (Throwable e) {
				log.error("Hibernate Handler Objests Error:", e);
				return false;
			} finally {
				session.close();
			}
		}
		return false;
	}

	static boolean usingStatelessSession(Consumer<StatelessSession> action) {
		if (sessionFactory != null) {
			StatelessSession session = sessionFactory.openStatelessSession();
			try {
				Transaction transaction = session.beginTransaction();
				action.accept(session);
				transaction.commit();
				return true;
			} catch (Throwable e) {
				return false;
			} finally {
				session.close();
			}
		}
		return false;
	}
}
