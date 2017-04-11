package org.server.core.filterchain;

/**
 * 过滤处理结果
 */
public enum FilterResult {
	/**
	 * 处理成功，交由下一个处理链处理
	 */
	OkAndCallNext,

	/**
	 * 处理成功
	 */
	Ok,

	/**
	 * 等待状态
	 */
	Wait,

	/**
	 * 处理失败
	 */
	Defeat,

	/**
	 * 处理错误
	 */
	Error,

	/**
	 * 代表所有状态
	 */
	Any;
}
