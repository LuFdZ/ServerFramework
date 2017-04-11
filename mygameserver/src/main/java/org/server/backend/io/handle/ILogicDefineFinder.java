package org.server.backend.io.handle;

import java.util.List;

/**
 * 消息元素寻找器
 */
public interface ILogicDefineFinder {
	/**
	 * 寻找定义
	 * 
	 * @param object
	 *            对象
	 */
	public void searchDefine(Object[] objects);

	/**
	 * 设置方法支持类型
	 * 
	 * @param types
	 */
	public void setMethodSupportTypes(List<Class<?>> types);
}
