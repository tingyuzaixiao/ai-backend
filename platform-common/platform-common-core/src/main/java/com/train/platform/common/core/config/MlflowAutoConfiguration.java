package com.train.platform.common.core.config;

import com.train.platform.common.core.mlflow.MlflowWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class MlflowAutoConfiguration {
	@Value("${mlflow.config.uri}")
	private String trackingUri;

	@Bean
	public MlflowWrapper mlflowWrapper() {
		// 使用获取到的配置值创建MlflowWrapper实例
		return new MlflowWrapper(trackingUri);
	}
}
