/*
 * Copyright (c) 2025 AIRCAS Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.train.platform.common.core.config;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author lee
 * @date 2024/2/1 RestTemplate
 */
@AutoConfiguration
public class RestTemplateConfiguration {

	@Bean
	public RestTemplate restTemplate() {
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(100);
		connectionManager.setDefaultMaxPerRoute(25);

		ConnectionConfig connectionConfig = ConnectionConfig.custom()
				.setValidateAfterInactivity(30, TimeUnit.SECONDS)
				.setSocketTimeout(60, TimeUnit.SECONDS)
				.build();
		connectionManager.setDefaultConnectionConfig(connectionConfig);

		CloseableHttpClient httpClient = HttpClients
				.custom()
				.setConnectionManager(connectionManager)
				.build();


		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
		factory.setHttpClient(httpClient);
		factory.setConnectTimeout(Duration.ofSeconds(5));
		factory.setConnectionRequestTimeout(Duration.ofSeconds(3));

		return new RestTemplate(factory);
	}
}
