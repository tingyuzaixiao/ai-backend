package com.train.platform.common.core.constant.enums;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zj
 * @date 2025/9/22 实验状态枚举
 */
public enum ExperimentStatus {
	INIT(0),
	RUNNING(1),
	SCHEDULED(2),
	FINISHED(3),
	FAILED(4),
	KILLED(5),
	KILLING(6);

	private static final ExperimentStatus[] EXPERIMENT_STATUSES = values();
	private static final Map<Integer, ExperimentStatus> VALUE_TO_ENUM_MAP;
	private static final Map<String, ExperimentStatus> NAME_TO_ENUM_MAP;

	static {
		Map<Integer, ExperimentStatus> valueMap = new HashMap<>();
		Map<String, ExperimentStatus> nameMap = new HashMap<>();
		for (ExperimentStatus status : EXPERIMENT_STATUSES) {
			valueMap.put(status.value, status);
			nameMap.put(status.name(), status);
		}
		VALUE_TO_ENUM_MAP = Collections.unmodifiableMap(valueMap);
		NAME_TO_ENUM_MAP = Collections.unmodifiableMap(nameMap);
	}

	private final int value;

	ExperimentStatus(int value) {
		this.value = value;
	}

	public final int getValue() {
		return this.value;
	}

	public static ExperimentStatus[] getAllStatus() {
		return EXPERIMENT_STATUSES;
	}

	public static ExperimentStatus valueOf(int value) {
		return VALUE_TO_ENUM_MAP.get(value);
	}

	public static ExperimentStatus nameOf(String name) {
		return NAME_TO_ENUM_MAP.get(name);
	}
}
