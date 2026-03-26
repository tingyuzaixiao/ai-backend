
package com.train.platform.common.file.local;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 本地文件 配置信息
 *
 * @author lee
 * <p>
 * bucket 设置公共读权限
 */
@Data
@EnableConfigurationProperties({LocalFileProperties.class})
@ConfigurationProperties(prefix = "local")
public class LocalFileProperties {

	/**
	 * 是否开启
	 */
	private boolean enable;

	/**
	 * 默认路径
	 */
	private String basePath;

	/**
	 * 局域网
	 */
	private String localAreaNetwork;

	/**
	 * 公网
	 */
	private String externalNetwork;
}
