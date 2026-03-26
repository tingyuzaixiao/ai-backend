package com.train.platform.common.core.util;

public class StringUtils extends org.apache.commons.lang3.StringUtils {

	public static String truncateAfter(String input, String delimiter) {
		if (input == null || delimiter == null) return input;
		int index = input.indexOf(delimiter);
		if (index == -1) return input; // 未找到分隔符，返回原字符串
		return input.substring(index + delimiter.length());
	}
}
