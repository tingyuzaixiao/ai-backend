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

package com.train.platform.codegen;

import com.train.platform.common.datasource.annotation.EnableDynamicDataSource;
import com.train.platform.common.feign.annotation.EnablePlatformFeignClients;
import com.train.platform.common.security.annotation.EnablePlatformResourceServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author lee
 * @date 2024/07/29 代码生成模块
 */
@EnableDynamicDataSource
@EnablePlatformFeignClients
@EnableDiscoveryClient
@EnablePlatformResourceServer
@SpringBootApplication
public class PlatformCodeGenApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlatformCodeGenApplication.class, args);
	}

}
