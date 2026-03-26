package com.train.platform.common.core.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimeUtils {
	public static LocalDateTime convertLongToDateTime(long ms) {
		long seconds = ms / 1000;
		Instant instant = Instant.ofEpochSecond(seconds);
		ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
		return zonedDateTime.toLocalDateTime();
	}
}
