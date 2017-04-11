package org.server.backend.core.statemachine;

/**
 * 抽象状态机上下文
 */
public abstract class AbstractStateContext implements StateContext {
	/**
	 * 获得状态
	 * 
	 * @return 状态
	 */
	protected abstract State state0();

	/**
	 * 设置状态
	 * 
	 * @param state
	 *            状态
	 */
	protected abstract void state0(State state);

	/**
	 * 读取状态
	 */
	@Override
	public State state() {
		return state0();
	}
	
	/**
	 * 新状态事件
	 * 
	 * @param state
	 */
	protected void newState(State state) {
	}
	
	@Override
	public void state(State state) {

		State beforeState = state0();
		State newState = state;
		StateStep beforeStateStep = null;
		StateStep newStateStep = null;

		if (beforeState instanceof StateStep)
			beforeStateStep = (StateStep) beforeState;

		if (newState instanceof StateStep)
			newStateStep = (StateStep) newState;

		// trigger quit event
		if (beforeStateStep != null)
			beforeStateStep.quit(this);

		state0(newState);

		// trigger enter event
		if (newStateStep != null)
			newStateStep.enter(this);
		
		// trigger newstate event
		newState(newState);
	}
}
