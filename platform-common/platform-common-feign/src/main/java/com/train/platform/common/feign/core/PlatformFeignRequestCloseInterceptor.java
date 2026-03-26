package com.train.platform.common.feign.core;

import feign.RequestInterceptor;
import org.springframework.http.HttpHeaders;

/**
 * @author lee
 * @date 2024/3/15
 * <p>
 * http connection close
 */
public class PlatformFeignRequestCloseInterceptor implements RequestInterceptor {

	/**
	 * set connection close
	 * @param template
	 */
	@Override
	public void apply(feign.RequestTemplate template) {
		template.header(HttpHeaders.CONNECTION, "close");
	}

}
