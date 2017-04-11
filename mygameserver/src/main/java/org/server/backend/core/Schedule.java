package org.server.backend.core;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 计划调度类
 *
 * @author
 */
public class Schedule {

	static final Logger log = LoggerFactory.getLogger(Schedule.class);

	static ScheduledThreadPoolExecutor executor;

	public static void open() {
		if (executor != null) {
			return;
		}
		int threadCount = Runtime.getRuntime().availableProcessors();
		executor = new ScheduledThreadPoolExecutor(threadCount, new ScheduleThreadFactory());
		log.info("[Schedule] initialize thread count {}.", threadCount);
	}

	public static void shutdown() {
		if (executor == null) {
			return;
		}
		executor.shutdown();
		executor = null;
	}
	
	/**
	 * 按照固定频率时间调度回调(不受执行时间影响)
	 *
	 * @param callback
	 *            要执行的函数
	 * @param reference
	 *            用于参考的时间
	 * @param duration
	 *            基础参考时间的间隔调用时间
	 * @return 任务对象
	 */
	public static ScheduledFuture<?> scheduleAtFixedRate(Runnable callback,
			Duration duration) {
		return scheduleAtFixedRate(callback, LocalDateTime.now(), duration);
	}

	/***
	 * 按照固定频率时间调度回调(不受执行时间影响)
	 * 
	 * @param callBack 要执行的函数
	 * @param reference 用于参考的时间
	 * @param duration 基础参考时间的间隔调用时间
	 * @return 任务对象
	 */
	public static ScheduledFuture<?> scheduleAtFixedRate(Runnable callBack, LocalDateTime reference, Duration duration) {

		if (executor == null) {
			return null;
		}

		LocalDateTime now = LocalDateTime.now();// 当前时间
		while (reference.isBefore(now)) {// 引用时间在现在时间之前
			reference = reference.plus(duration);// 累加间隔时间
		}
		
		return executor.scheduleAtFixedRate(callBack, Duration.between(now, reference).toMillis(), duration.toMillis(), TimeUnit.MILLISECONDS);
	}
	
	/**
	 * 按照固定延迟时间调度回调(受执行时间影响)
	 *
	 * @param callback
	 *            要执行的函数
	 * @param reference
	 *            用于参考的时间
	 * @param duration
	 *            基础参考时间的间隔调用时间
	 * @return 任务对象
	 */
	public static ScheduledFuture<?> scheduleWithFixedDelay(Runnable callback,
			Duration duration) {
		return scheduleWithFixedDelay(callback, LocalDateTime.now(), duration);
	}
	
	/**
	 * 按照固定延迟时间调度回调(受执行时间影响)
	 *
	 * @param callback
	 *            要执行的函数
	 * @param reference
	 *            用于参考的时间
	 * @param duration
	 *            基础参考时间的间隔调用时间
	 * @return 任务对象
	 */
	public static ScheduledFuture<?> scheduleWithFixedDelay(Runnable callback,
			LocalDateTime reference, Duration duration) {
		if (executor == null)
			return null;

		LocalDateTime now = LocalDateTime.now();// 当前时间

		while (reference.isBefore(now)) {// 引用时间在现在时间之前
			reference = reference.plus(duration);// 累加间隔时间
		}

		return executor.scheduleWithFixedDelay(callback,
				Duration.between(now, reference).toMillis(),
				duration.toMillis(), TimeUnit.MILLISECONDS);
	}
}
