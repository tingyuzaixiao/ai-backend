package com.train.platform.common.core.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

@Component
@Slf4j
public class ProgramLock {
	private final ReentrantLock[] programLocks;

	public ProgramLock(int lockNum) {
		programLocks = new ReentrantLock[lockNum];
		for (int i = 0; i < lockNum; i++) {
			programLocks[i] = new ReentrantLock();
		}
	}

	public ReentrantLock getLock(String name) {
		return LockUtils.getLock(name, programLocks);
	}
}
