package org.server.backend.core.statemachine;

/**
 * 状态处理步骤
 */
public interface StateStep extends State {
	/**
	 * 处理
	 * 
	 * @param context
	 *            上下文
	 * @return 处理是否成功
	 */
	boolean process(StateContext context);

	/**
	 * 进入状态事件
	 * 
	 * @param context
	 *            上下文
	 * @return 处理是否成功
	 */
	void enter(StateContext context);

	/**
	 * 退出状态事件
	 * 
	 * @param context
	 *            上下文
	 * @return 处理是否成功
	 */
	void quit(StateContext context);
}
