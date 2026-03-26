package com.train.platform.common.seata.config;

import com.train.platform.common.core.factory.YamlPropertySourceFactory;
import io.seata.spring.annotation.datasource.EnableAutoDataSourceProxy;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Seata 配置类
 *
 * @author lee
 * @date 2024/3/29
 */
@PropertySource(value = "classpath:seata-config.yml", factory = YamlPropertySourceFactory.class)
@EnableAutoDataSourceProxy(useJdkProxy = true)
@Configuration(proxyBeanMethods = false)
public class SeataAutoConfiguration {

}
