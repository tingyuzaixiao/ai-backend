package com.train.platform.common.core.constant.enums;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum ExperimentStopType {
	SAVE_WEIGHTS(0),
	FORCE(1);

	private static final ExperimentStopType[] EXPERIMENT_STOP_TYPES = values();
	private static final Map<Integer, ExperimentStopType> VALUE_TO_ENUM_MAP;
	private static final Map<String, ExperimentStopType> NAME_TO_ENUM_MAP;

	static {
		Map<Integer, ExperimentStopType> valueMap = new HashMap<>();
		Map<String, ExperimentStopType> nameMap = new HashMap<>();
		for (ExperimentStopType experimentStopType : EXPERIMENT_STOP_TYPES) {
			valueMap.put(experimentStopType.value, experimentStopType);
			nameMap.put(experimentStopType.name(), experimentStopType);
		}
		VALUE_TO_ENUM_MAP = Collections.unmodifiableMap(valueMap);
		NAME_TO_ENUM_MAP = Collections.unmodifiableMap(nameMap);
	}

	private final int value;

	ExperimentStopType(int value) {
		this.value = value;
	}

	public final int getValue() {
		return this.value;
	}

	public static ExperimentStopType[] getAllExperimentStopTypes() {
		return EXPERIMENT_STOP_TYPES;
	}

	public static ExperimentStopType valueOf(int value) {
		return VALUE_TO_ENUM_MAP.get(value);
	}

	public static ExperimentStopType nameOf(String name) {
		return NAME_TO_ENUM_MAP.get(name);
	}
}
