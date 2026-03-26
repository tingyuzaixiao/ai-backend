package com.train.platform.common.core.constant.enums;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zj
 * @date 2025/9/30 mlflow文件类型
 */
public enum MLFileType {
	OUTPUT(0),
	USER(1);

	private static final MLFileType[] FILE_OWNER_TYPES = values();
	private static final Map<Integer, MLFileType> VALUE_TO_ENUM_MAP;
	private static final Map<String, MLFileType> NAME_TO_ENUM_MAP;

	static {
		Map<Integer, MLFileType> valueMap = new HashMap<>();
		Map<String, MLFileType> nameMap = new HashMap<>();
		for (MLFileType fileType : FILE_OWNER_TYPES) {
			valueMap.put(fileType.value, fileType);
			nameMap.put(fileType.name(), fileType);
		}
		VALUE_TO_ENUM_MAP = Collections.unmodifiableMap(valueMap);
		NAME_TO_ENUM_MAP = Collections.unmodifiableMap(nameMap);
	}

	private final int value;

	MLFileType(int value) {
		this.value = value;
	}

	public final int getValue() {
		return this.value;
	}

	public static MLFileType[] getAllFileOwnerTypes() {
		return FILE_OWNER_TYPES;
	}

	public static MLFileType valueOf(int value) {
		return VALUE_TO_ENUM_MAP.get(value);
	}

	public static MLFileType nameOf(String name) {
		return NAME_TO_ENUM_MAP.get(name);
	}
}
