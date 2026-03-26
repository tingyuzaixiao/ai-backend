/*
 *    Copyright (c) 2019-2025, lee All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the name of the platform4cloud.com developer nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Author: lee 
 */

package com.train.platform.daemon.quartz.config;

import com.train.platform.daemon.quartz.constants.PlatformQuartzEnum;
import com.train.platform.daemon.quartz.service.SysJobService;
import com.train.platform.daemon.quartz.util.TaskUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 郑健楠
 * <p>
 * 初始化加载定时任务
 */
@Slf4j
@Configuration
@AllArgsConstructor
public class PlatformInitQuartzJob implements InitializingBean {

	private final SysJobService sysJobService;

	private final TaskUtil taskUtil;

	private final Scheduler scheduler;

	@Override
	public void afterPropertiesSet() throws Exception {
		sysJobService.list().forEach(sysjob -> {
			if (PlatformQuartzEnum.JOB_STATUS_RELEASE.getType().equals(sysjob.getJobStatus())) {
				taskUtil.removeJob(sysjob, scheduler);
			}
			else if (PlatformQuartzEnum.JOB_STATUS_RUNNING.getType().equals(sysjob.getJobStatus())) {
				taskUtil.resumeJob(sysjob, scheduler);
			}
			else if (PlatformQuartzEnum.JOB_STATUS_NOT_RUNNING.getType().equals(sysjob.getJobStatus())) {
				taskUtil.pauseJob(sysjob, scheduler);
			}
			else {
				taskUtil.removeJob(sysjob, scheduler);
			}
		});
	}

}
