package org.server.backend.io.handle.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 消息回调函数
 *
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogicHandle {
	/**
	 * 模块相关信息
	 * 
	 * @return 模块信息
	 */
	LogicModelMeta model();
}
