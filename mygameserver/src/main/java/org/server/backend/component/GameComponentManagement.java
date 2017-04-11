package org.server.backend.component;

import java.io.File;
import java.lang.reflect.Modifier;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javassist.ClassPool;
import javassist.CtClass;
import jodd.io.findfile.ClassScanner;
import jodd.util.ClassLoaderUtil;

import org.server.backend.component.GameComponent.Setting;
import org.server.tools.Toolset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 游戏组件管理器
 */
public class GameComponentManagement {
	
	static final Logger log = LoggerFactory.getLogger(GameComponentManagement.class);
	
	static final Map<GameComponent, Setting> _components = new HashMap<>();
	
	static final Map<Integer, GameComponent> _idByComponents = new HashMap<>();
	
	/**
	 * 类搜索器
	 *
	 */
	static class ComponentFinder extends ClassScanner {
		
		public ComponentFinder() {
			outputLibraryInfo();
		}
		
		@Override
		protected void onEntry(EntryData arg0) throws Exception {
			// 获得名称
			String clsName = arg0.getName();

			// 排除 maven 工程 的 target 目录
			if (clsName.contains("target.classes.")) {
				return;
			}

			// 建立过滤条件, 搜索包名或者类名中带有 component 关键字的类
			if (clsName.toLowerCase().contains("component")) {
				handlerClass(clsName);
			}
		}
		
		void outputLibraryInfo() {
			Pattern pattern = Pattern.compile("(.+?)_.*([0-9]{10})\\.jar");
			DateTimeFormatter datetimeFormat = DateTimeFormatter
					.ofPattern("yyMMddHHmm"), displayFormat = DateTimeFormatter
					.ofPattern("构建于  yyyy 年  MM 月  dd 日  HH 时 mm 分.");

			for (File classFile : ClassLoaderUtil.getDefaultClasspath()) {
				if (classFile.isFile() && classFile.exists()) {
					String fileName = classFile.getName();
					Matcher matcher = pattern.matcher(fileName);
					if (matcher.find()) {
						String moduleName = matcher.group(1);
						String timeText = matcher.group(2);
						try {
							timeText = displayFormat.format(datetimeFormat
									.parse(timeText));
							Toolset.prettyOutput(log, "运行模块  {}{nl}{}",
									moduleName, timeText);
						} catch (Exception ignore) {
							// ignore the exception
						}
					}
				}
			}
		}
	}
	
	/**
	 * 载入所有组件
	 */
	public static void loadAllComponent() {

		ComponentFinder finder = new ComponentFinder();

		finder.scanDefaultClasspath();

		// 运行所有组件
		startAllComponent();
	}
	
	/**
	 * 卸载所有组件
	 */
	public static void unloadAllComponent() {
		orderdComponents().forEach(s -> {
			String cName = s.getClass().getName();
			try {
				s.unload();
				log.info(String.format("[%s] Component Unload.", cName));
			} catch (Exception e) {
				log.error(String.format("尝试卸载组件 [%s] 失败：", cName), e);
			}
		});
		_components.clear();
		_idByComponents.clear();
	}
	
	/**
	 * 获得组件
	 * 
	 * @param id
	 *            组件编号
	 * @return 组件
	 */
	public static GameComponent getActiveComponentById(int id) {
		return _idByComponents.get(id);
	}
	
	/**
	 * 处理类文件
	 * 
	 * @param clsName
	 *            类文件名
	 */
	static void handlerClass(String clsName) {

		// 寻找相关类
		if (!findSomeComponent(clsName, null, GameComponent.class))
			return;

		// 实例化组件
		GameComponent component = newInstanceComponent(clsName);

		if (component == null)
			return;

		// 获得组件设置
		Setting setting = component.getClass().getAnnotation(Setting.class);

		// 注册到缓存
		registerComponent(setting, component);
	}
	
	/**
	 * 寻找到组件
	 * 
	 * @param clsName
	 *            类名
	 * 
	 * @param parentClsName
	 *            查询父类名称
	 * 
	 * @param likeCls
	 *            想要查询的类
	 */
	static boolean findSomeComponent(String clsName, String parentClsName,Class<?> likeCls) {

		// 要查询类的名称
		boolean queryIsParent = true;
		String queryClsName = parentClsName;

		if (queryClsName == null) {
			queryClsName = clsName;
			queryIsParent = false;
		}

		// not self
		if (queryClsName.equals(likeCls.getName()))
			return false;

		// 获得类型池
		ClassPool pool = ClassPool.getDefault();

		try {
			CtClass ctClass = pool.get(queryClsName);

			// 获得类实现接口
			String[] interfaces = ctClass.getClassFile().getInterfaces();

			// 利用 stream 做条件筛选
			boolean isImpl = Arrays.stream(interfaces)
					.filter(x -> x.equals(likeCls.getName())).findAny()
					.isPresent();

			// 判断类是否为抽象
			boolean isAbs = Modifier.isAbstract(ctClass.getModifiers());

			// 父级是否符合套件
			boolean parentMatch = false;

			// 查找父类
			if (ctClass.getSuperclass() != null)
				parentMatch = findSomeComponent(clsName, ctClass
						.getSuperclass().getName(), likeCls);

			// 判断类是否实现接口
			return (isImpl || parentMatch) && (queryIsParent || !isAbs);
		} catch (Throwable e) {
			log.error("Can't Load [{}:{}] Class File ", queryClsName,
					e.getMessage(), e);
		}

		return false;
	}
	
	/**
	 * 创建新实例
	 * 
	 * @param clsName
	 *            类名
	 */
	static GameComponent newInstanceComponent(String clsName) {
		try {
			Class<?> clasz = Class.forName(clsName);
			GameComponent gameComponent = (GameComponent) clasz.newInstance();
			return gameComponent;
		} catch (ClassNotFoundException e) {
			log.error("Not Found Class [{}] !", clsName, e);
		} catch (InstantiationException | IllegalAccessException e) {
			log.error("Not newInstance Class Object [{}]!", clsName, e);
		}
		return null;
	}
	
	/**
	 * 排序后组件流
	 * 
	 * @return 排序后的流
	 */
	public static Stream<GameComponent> orderdComponents() {
		return _components.keySet().stream().
				sorted(GameComponentManagement::sortComponent);
	}
	
	/**
	 * 排序组件方法
	 * 
	 * @param x
	 *            组件1
	 * @param y
	 *            组件2
	 * @return 加载顺序比对值
	 */
	static int sortComponent(GameComponent x, GameComponent y) {
		Setting a = _components.get(x);
		Setting b = _components.get(y);

		int x_value = a != null ? a.loadOrder() : 0;
		int v_value = b != null ? b.loadOrder() : 0;

		return x_value - v_value;
	}
	
	/**
	 * 注册组件
	 *
	 * @param setting
	 *            组件设置
	 * @param newInstance
	 *            组件实例
	 */
	static void registerComponent(Setting setting, GameComponent newInstance) {

		_components.put(newInstance, setting);

		if (setting != null) {

			if (_idByComponents.containsKey(setting.id())) {
				log.warn("[GameComponentManagement] 重复的组件编号:: {} , 请检查！",
						setting.id());
			}

			_idByComponents.put(setting.id(), newInstance);
		}
	}
	
	/**
	 * 开始所有组件
	 * 
	 * @return 启动所有组件是否成功
	 */
	static boolean startAllComponent() {

		Throwable fatalErrorThrowable = null;

		GameComponent components[] = orderdComponents().toArray(
				GameComponent[]::new);

		for (GameComponent s : components) {

			String cName = s.getClass().getName();

			try {

				s.load();

				log.info(String.format("[%s] Component Load.", cName));

			} catch (Throwable e) {

				log.error(String.format("尝试启动组件 [%s] 失败：", cName), e);

				fatalErrorThrowable = e;

				break;
			}
		}

		return fatalErrorThrowable == null;
	}
}
