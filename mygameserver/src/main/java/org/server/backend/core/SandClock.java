package org.server.backend.core;

import java.time.Duration;

/**
 * 单线程计时器
 */
public class SandClock {
	
	static final Duration EndTime = Duration.ofSeconds(-1);

	Duration max;
	Duration current;
	
	/**
	 * 构造单线程计时器
	 * 
	 * @param maxTimer
	 *            超时时间
	 */
	public SandClock(Duration maxTimer) {
		this.max = maxTimer;
	}

	/**
	 * 获得当前计时时间
	 * 
	 * @return 当前计时时间
	 */
	public Duration current() {
		return current;
	}

	/**
	 * 获得计时器到期时间
	 * 
	 * @return 到期时间
	 */
	public Duration timer() {
		return this.max;
	}

	/**
	 * 判断计时器是否已经开始
	 * 
	 * @return 是否已经开启
	 */
	public boolean started() {
		return current != null;
	}

	/**
	 * 是否已经到最大时间
	 * 
	 * @return 是否已经到达最大时间
	 */
	public boolean done() {
		return current != null && current.compareTo(max) >= 0;
	}

	/**
	 * 开始计时
	 * 
	 * @return 开始是否成功
	 */
	public boolean start() {
		if (current != null)
			return false;
		current = Duration.ZERO;
		return true;
	}

	/**
	 * 结束计时
	 * 
	 * @return 结束是否成功
	 */
	public boolean stop() {
		if (current == null)
			return false;
		current = null;
		return true;
	}

	/**
	 * 重新开始计时
	 */
	public void restart() {
		current = Duration.ZERO;
	}

	/**
	 * 更新时间
	 * 
	 * @param time
	 *            经过的时间
	 * @return 操作是否成功
	 */
	public boolean updateClock(Duration time) {
		if (current == null)
			return false;
		current = current.plus(time);
		return true;
	}

	@Override
	public String toString() {
		return String
				.format("SingleThreadTimer :: isBegin - %s , isTimer - %s , current - %s , max - %s .",
						String.valueOf(started()), String.valueOf(done()),
						current, max);
	}
}
