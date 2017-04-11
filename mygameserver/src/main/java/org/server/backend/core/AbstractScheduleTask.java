package org.server.backend.core;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 定时器任务
 */
public abstract class AbstractScheduleTask implements ScheduleTask {
	
	static final Logger log = LoggerFactory.getLogger(AbstractScheduleTask.class);
	
	String taskName;
	Duration duration;
	ScheduledFuture<?> scheduledFuture;

	SandClock subTaskClock;
	ScheduleTask subTask;
	
	/**
	 * 构造固定执行任务
	 * 
	 * @param duration
	 *            时间间隔
	 */
	protected AbstractScheduleTask(String taskName, Duration duration) {
		this(taskName, LocalDateTime.now(), duration);
	}
	
	/**
	 * 构造固定执行任务
	 * 
	 * @param reference
	 *            参考时间
	 * 
	 * @param duration
	 *            时间间隔
	 */
	protected AbstractScheduleTask(String taskName, LocalDateTime reference,
			Duration duration) {

		if (duration == null) {
			duration = Duration.ofSeconds(1);
			log.warn("[AbstractScheduleTask]:: default task execute duration one second!");
		}

		this.taskName = taskName;
		this.duration = duration;
		this.scheduledFuture = Schedule.scheduleWithFixedDelay(this::runTask,
				reference, duration);

		if (this.taskName == null) {
			this.taskName = "NoNameTask";
		}
	}
	
	/**
	 * 获得任务执行间隔
	 * 
	 * @return 执行间隔
	 */
	protected Duration getTaskExecuteDuration() {
		return duration;
	}

	/**
	 * 是否已经开始
	 * 
	 * @return 定时器是否开始
	 */
	protected boolean isStart() {
		return scheduledFuture != null;
	}

	/**
	 * 是否已经停止
	 * 
	 * @return 定时器是否停止
	 */
	protected boolean isStop() {
		return scheduledFuture == null;
	}

	/**
	 * 停止定时器
	 */
	protected void stop() {
		if (scheduledFuture != null) {
			scheduledFuture.cancel(false);
			scheduledFuture = null;
		}
	}
	
	protected boolean registerSandTask(Duration subTaskDuration,
			ScheduleTask subTask) {

		if (subTaskDuration == null || subTask == null)
			return false;

		this.subTask = subTask;
		this.subTaskClock = new SandClock(subTaskDuration);
		this.subTaskClock.start();

		return true;
	}
	
	@Override
	public void runTask() {

		Thread currentThread = Thread.currentThread();
		String beforeThreadName = currentThread.getName();
		currentThread.setName(taskName);

		try {
			runTask0();
		} catch (Throwable e) {
			log.error("[AbstractScheduleTask]::runTask0 exception!", e);
		}

		if (subTaskClock != null && subTaskClock.updateClock(duration)
				&& subTaskClock.done()) {
			if (subTask != null) {
				try {
					subTask.runTask();
				} catch (Throwable e) {
					log.error("[AbstractScheduleTask]::run subtask exception!",
							e);
				}
			}
			subTaskClock.restart();
		}

		currentThread.setName(beforeThreadName);
	}

	/**
	 * 执行定时任务
	 */
	protected abstract void runTask0();
}
