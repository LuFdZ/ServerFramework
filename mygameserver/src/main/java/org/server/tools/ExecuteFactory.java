package org.server.tools;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

/**
 * 消息处理执行工程
 */
public class ExecuteFactory {
	String executeFactoryName;
	int usingThreadCount;
	ExecutorService executeService;
	
	public ExecuteFactory(String factoryName) {
		executeFactoryName = factoryName;
		usingThreadCount = Runtime.getRuntime().availableProcessors();
	}
	
	public String getExecuteFactoryName() {
		return executeFactoryName;
	}

	public int getUsingThreadCount() {
		return usingThreadCount;
	}

	public void setUsingThreadCount(int usingThreadCount) {
		this.usingThreadCount = Math.max(1, usingThreadCount);
	}

	public ExecutorService getExecuteService() {
		return executeService;
	}
	
	public void usingWorkStealingPool() {

		if (executeService != null)
			return;

		ForkJoinPool.ForkJoinWorkerThreadFactory threadFactory = new ForkJoinPool.ForkJoinWorkerThreadFactory() {

			@Override
			public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
				ForkJoinWorkerThread result = ForkJoinPool.defaultForkJoinWorkerThreadFactory
						.newThread(pool);

				if (result != null)
					result.setName(String.format("[%s_%04d]",
							executeFactoryName, result.getId()));

				return result;
			}
		};
		executeService = new ForkJoinPool(usingThreadCount, threadFactory,
				null, true);
	}

	public void doWorker(Runnable runnable) {
		if (executeService != null)
			executeService.execute(runnable);
	}
}
