package org.server.backend.io.handle.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogicAuthorize {
	/**
	 * 验证函数
	 * 
	 * @return 函数编号
	 */
	int id();

	/**
	 * 验证函数名称
	 * 
	 * @return 函数名称
	 */
	String name();
}
