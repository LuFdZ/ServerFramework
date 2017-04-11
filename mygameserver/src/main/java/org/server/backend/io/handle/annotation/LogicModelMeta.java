package org.server.backend.io.handle.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.server.backend.io.handle.impl.LogicModelSerialize;

/**
 * 消息元数据
 */
@Documented
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface LogicModelMeta {
	/**
	 * 标识的消息对象类
	 * 
	 * @return
	 */
	Class<?> owner();

	/**
	 * 命令值
	 * 
	 * @return 模块命令值
	 */
	int command();

	/**
	 * 序列化方式
	 * 
	 * @return 序列化方式
	 */
	LogicModelSerialize serialize();
}
