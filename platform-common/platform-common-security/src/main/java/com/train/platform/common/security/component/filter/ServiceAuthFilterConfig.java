package com.train.platform.common.security.component.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ServiceAuthFilterConfig {
	@Value("${service.internal.key:platform}")
	private String serviceSecret;


	@Bean
	public ServiceAuthFilter serviceAuthFilter() {
		return new ServiceAuthFilter(serviceSecret);
	}
}
