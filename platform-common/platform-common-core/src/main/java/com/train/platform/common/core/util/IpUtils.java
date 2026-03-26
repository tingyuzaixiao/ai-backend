package com.train.platform.common.core.util;

public class IpUtils {
	public static String normalizeIp(String ip) {
		// 仅在开发环境需要：将IPv6回环地址转为IPv4
		if ("0:0:0:0:0:0:0:1".equals(ip)) {
			return "127.0.0.1";
		}
		return ip;
	}
}
