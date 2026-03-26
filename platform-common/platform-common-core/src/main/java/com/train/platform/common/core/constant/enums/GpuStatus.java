package com.train.platform.common.core.constant.enums;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum GpuStatus {
	FREE(0),
	OCCUPIED(1);

	private static final GpuStatus[] GPU_STATUSES = values();
	private static final Map<Integer, GpuStatus> VALUE_TO_ENUM_MAP;
	private static final Map<String, GpuStatus> NAME_TO_ENUM_MAP;

	static {
		Map<Integer, GpuStatus> valueMap = new HashMap<>();
		Map<String, GpuStatus> nameMap = new HashMap<>();
		for (GpuStatus status : GPU_STATUSES) {
			valueMap.put(status.value, status);
			nameMap.put(status.name(), status);
		}
		VALUE_TO_ENUM_MAP = Collections.unmodifiableMap(valueMap);
		NAME_TO_ENUM_MAP = Collections.unmodifiableMap(nameMap);
	}

	private final int value;

	GpuStatus(int value) {
		this.value = value;
	}

	public final int getValue() {
		return this.value;
	}

	public static GpuStatus[] getAllStatus() {
		return GPU_STATUSES;
	}

	public static GpuStatus valueOf(int value) {
		return VALUE_TO_ENUM_MAP.get(value);
	}

	public static GpuStatus nameOf(String name) {
		return NAME_TO_ENUM_MAP.get(name);
	}
}
