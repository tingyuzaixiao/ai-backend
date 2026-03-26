package com.train.platform.common.core.util;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zj
 * @date 2025/9/22 线程池
 */
@Slf4j
public class ThreadPool {
//	private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
//	private static final int MAX_POOL_SIZE = CORE_POOL_SIZE + 4;
	private static final int QUEUE_CAPACITY = 10000;
	private static final long KEEP_ALIVE_TIME = 60L;
	private static final long SLEEP_INTERVAL = 3600000L;
	private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;


	private final ThreadPoolExecutor threadPool;

	private final AtomicLong submittedTasks = new AtomicLong(0);
	private final AtomicLong completedTasks = new AtomicLong(0);
	private final AtomicInteger rejectedCount = new AtomicInteger(0);

	private final String poolName;

	public ThreadPool(String poolName, int corePoolSize, int maxPoolSize) {
		this.poolName = poolName;

		BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
		ThreadFactory threadFactory = new ThreadFactory() {
			private final AtomicInteger threadNumber = new AtomicInteger(1);

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, poolName + "-worker-" + threadNumber.getAndIncrement());
				t.setUncaughtExceptionHandler((thread, throwable) -> {
					log.error("thread {} occurred an uncaught exception: ", thread.getName(), throwable);
				});
				return t;
			}
		};
		RejectedExecutionHandler rejectionHandler = (r, executor) -> {
			rejectedCount.incrementAndGet();
			log.error("thread pool has rejected task. pool size: {}, queue size: {}, reject: {}",
					executor.getPoolSize(), executor.getQueue().size(), rejectedCount.get()
			);
		};

		this.threadPool = new ThreadPoolExecutor(
				corePoolSize,
				maxPoolSize,
				KEEP_ALIVE_TIME,
				TIME_UNIT,
				queue,
				threadFactory,
				rejectionHandler
		);

		startMonitoring();
		log.info("pool: {} init successful: core: {}, max: {}, queue size: {}",
				poolName, corePoolSize, maxPoolSize, QUEUE_CAPACITY
		);
	}

	public void submit(Runnable task) {
		threadPool.submit(task);
		submittedTasks.incrementAndGet();
	}

	public <T> Future<T> submit(Callable<T> task) {
		return threadPool.submit(task);
	}

	public void shutdownGracefully(long timeout, TimeUnit unit) {
		log.info("start gracefully closing thread pool: {}", poolName);

		threadPool.shutdown();

		try {
			if (!threadPool.awaitTermination(timeout, unit)) {
				log.error("pool: {} was not closed within the specified time and was forcibly terminated",
						poolName);
				List<Runnable> pendingTasks = threadPool.shutdownNow();
				log.warn("pending tasks: {} canceled when forcibly terminated", pendingTasks.size());
			}
		} catch (InterruptedException e) {
			log.error("interrupted when the thread pool is shutting down: {}", e.getMessage());
			Thread.currentThread().interrupt();
		}

		log.info("pool: {} shutdown, submit task: {}, complete: {}, reject: {}",
				poolName, submittedTasks.get(), completedTasks.get(), rejectedCount.get()
		);
	}

	private void startMonitoring() {
		new Thread(() -> {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					Thread.sleep(SLEEP_INTERVAL);

					log.info("pool: {} active: {}, queue size: {}, submit task: {}, complete task: {}, reject task: {}",
							poolName, getActiveCount(),
							getQueueSize(),
							getSubmittedTasks(),
							getCompletedTasks(),
							getRejectedCount()
					);
				} catch (Exception ignored) {
				}
			}
		}, poolName + "-monitor").start();
	}

	public int getActiveCount() {
		return threadPool.getActiveCount();
	}

	public int getQueueSize() {
		return threadPool.getQueue().size();
	}

	public long getSubmittedTasks() {
		return submittedTasks.get();
	}

	public long getCompletedTasks() {
		return completedTasks.get();
	}

	public int getRejectedCount() {
		return rejectedCount.get();
	}
}
