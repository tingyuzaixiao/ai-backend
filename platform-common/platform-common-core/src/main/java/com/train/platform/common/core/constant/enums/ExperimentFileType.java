package com.train.platform.common.core.constant.enums;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zj
 * @date 2025/9/30 实验文件类型
 */
public enum ExperimentFileType {
	MODEL(0),
	WEIGHTS(1);

	private static final ExperimentFileType[] FILE_OWNER_TYPES = values();
	private static final Map<Integer, ExperimentFileType> VALUE_TO_ENUM_MAP;
	private static final Map<String, ExperimentFileType> NAME_TO_ENUM_MAP;

	static {
		Map<Integer, ExperimentFileType> valueMap = new HashMap<>();
		Map<String, ExperimentFileType> nameMap = new HashMap<>();
		for (ExperimentFileType fileType : FILE_OWNER_TYPES) {
			valueMap.put(fileType.value, fileType);
			nameMap.put(fileType.name(), fileType);
		}
		VALUE_TO_ENUM_MAP = Collections.unmodifiableMap(valueMap);
		NAME_TO_ENUM_MAP = Collections.unmodifiableMap(nameMap);
	}

	private final int value;

	ExperimentFileType(int value) {
		this.value = value;
	}

	public final int getValue() {
		return this.value;
	}

	public static ExperimentFileType[] getAllFileOwnerTypes() {
		return FILE_OWNER_TYPES;
	}

	public static ExperimentFileType valueOf(int value) {
		return VALUE_TO_ENUM_MAP.get(value);
	}

	public static ExperimentFileType nameOf(String name) {
		return NAME_TO_ENUM_MAP.get(name);
	}
}
