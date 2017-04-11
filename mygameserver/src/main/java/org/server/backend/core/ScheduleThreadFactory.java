package org.server.backend.core;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 调用线程工厂
 * 
 * @author Hxms
 *
 */
public class ScheduleThreadFactory implements ThreadFactory {

	AtomicInteger threadId = new AtomicInteger(1);
	
	@Override
	public Thread newThread(Runnable r) {

		Thread thread = new Thread(r);
		
		thread.setName("Backend Schedule Id : " + threadId.getAndIncrement());
		
		return thread;
	}

}
