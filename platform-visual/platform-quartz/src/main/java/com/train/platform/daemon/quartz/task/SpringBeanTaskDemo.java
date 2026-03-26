package com.train.platform.daemon.quartz.task;

import com.train.platform.common.core.util.FileUtils;
import com.train.platform.daemon.quartz.constants.PlatformQuartzEnum;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author 郑健楠
 */
@Slf4j
@Component("demo")
public class SpringBeanTaskDemo {


	/**
	 * 测试Spring Bean的演示方法
	 */
	@SneakyThrows
	public String demoMethod(String para) {
		log.info("测试于:{}，输入参数{}", LocalDateTime.now(), para);
		return PlatformQuartzEnum.JOB_LOG_STATUS_SUCCESS.getType();
	}

}
