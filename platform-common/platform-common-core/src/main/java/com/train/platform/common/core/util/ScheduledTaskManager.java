package com.train.platform.common.core.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author zj
 * @date 2025/10/09 定时器
 */
@Slf4j
public class ScheduledTaskManager {
	private final ScheduledExecutorService executor;


	public ScheduledTaskManager(String name, int corePoolSize) {
		this.executor = buildExecutor(name, corePoolSize);
		log.info("ScheduledTaskManager: {} init success, core: {}", name, corePoolSize);
	}

	private ScheduledExecutorService buildExecutor(String name, int corePoolSize) {

		ThreadFactory threadFactory = new ThreadFactory() {
			private final AtomicInteger threadNumber = new AtomicInteger(1);

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r,  name + "-Task-Thread-" + threadNumber.getAndIncrement());
				t.setDaemon(true);
				return t;
			}
		};

		return new ScheduledThreadPoolExecutor(
				corePoolSize,
				threadFactory,
				new ThreadPoolExecutor.CallerRunsPolicy()
		) {
			@Override
			public void shutdown() {
				log.info("begin shutdown");
				super.shutdown();
				try {
					if (!super.awaitTermination(5, TimeUnit.MINUTES)) {
						log.warn("shutdown timeout，force shutdown");
						super.shutdownNow();
					}
				} catch (InterruptedException e) {
					log.error("interrupted when ScheduledExecutorService is shutting down: ", e);
					Thread.currentThread().interrupt();
				}
			}
		};
	}

	public void schedule(Runnable task, long delay, TimeUnit timeUnit) {
		executor.schedule(() -> {
			try {
				task.run();
			} catch (Exception e) {
				log.error("task: {} catch exception: {}", task, e.getMessage());
			}
		}, delay, timeUnit);
	}

	public void scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit timeUnit) {
		executor.scheduleAtFixedRate(() -> {
			try {
				task.run();
			} catch (Exception e) {
				log.error("FixedRate task: {} catch exception: {}", task, e.getMessage());
			}
		}, initialDelay, period, timeUnit);
	}

	public void scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit timeUnit) {
		executor.scheduleWithFixedDelay(() -> {
			try {
				task.run();
			} catch (Exception e) {
				log.error("FixedDelay task: {} catch exception: {}", task, e.getMessage());
			}
		}, initialDelay, delay, timeUnit);
	}

	public void shutdown() {
		executor.shutdown();
	}
}
