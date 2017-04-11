package org.server.backend.core.statemachine;

/**
 * 状态机上下文
 */
public interface StateContext {
	/**
	 * 获得当前状态
	 * 
	 * @return 当前状态
	 */
	State state();

	/**
	 * 新状态
	 * 
	 * @param state
	 *            新状态
	 */
	void state(State state);

	/**
	 * 获得上下文对象
	 * 
	 * @return 上下文对象
	 */
	Object attached();
}
