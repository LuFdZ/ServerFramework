package org.server.backend.component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.server.backend.BackendServer;
import org.server.backend.BackendServerConfig;
import org.server.backend.BackendServerCustomConfig;

public interface GameComponent {
	/**
	 * 加载游戏组件
	 */
	void load();

	/**
	 * 卸载游戏组件
	 */
	void unload();
	
	/**
	 * 获得 BackendServer 配置
	 * 
	 * @return backend 配置对象
	 */
	default BackendServerConfig getServerConfig() {
		return BackendServer.getInstance().getServerConfig();
	}

	/**
	 * 获得服务自定义配置
	 *
	 * @return 自定义配置
	 */
	default BackendServerCustomConfig getServerCustomConfig() {
		return BackendServer.getInstance().getCustomConfig();
	}

	/**
	 * 
	 * 组件设置 Annotation
	 *
	 */
	@Inherited
	@Documented
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
    @interface Setting {

		/**
		 * 组件Id
		 * 
		 * @return
		 */
		public int id();

		/**
		 * 组件名称
		 * 
		 * @return 组件的名称
		 */
		public String name();

		/**
		 * 加载顺序 ，决定谁先谁后加载，数值越小越靠前。
		 * 
		 * @return
		 */
		public int loadOrder() default 0;
	}
}
