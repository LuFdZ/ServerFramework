package org.server.core.filterchain;

/**
 * 过滤器任务
 */
public interface FilterTask {
	
	public int status();

	public void status(int newStatus);
}
