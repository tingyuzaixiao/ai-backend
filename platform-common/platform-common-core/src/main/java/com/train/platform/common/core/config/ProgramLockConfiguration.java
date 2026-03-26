package com.train.platform.common.core.config;

import com.train.platform.common.core.util.ProgramLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ProgramLockConfiguration {
	@Value("${program.lock.num:16}")
	private Integer lockNum;

	@Bean
	public ProgramLock programLock() {
		// 使用获取到的配置值创建ProgramLock实例
		return new ProgramLock(lockNum);
	}
}
