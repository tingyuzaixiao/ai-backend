package com.train.platform.common.core.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * @author zj
 * @date 2025/9/30 json类
 */
public class JacksonUtils {
	@Getter
	private static final ObjectMapper objectMapper;
	static {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	private static final TypeReference<Map<String, Object>> mapObjType = new TypeReference<>() {};
	private static final TypeReference<List<String>> listStrType = new TypeReference<>() {};

	public static Map<String, Object> deserializeMap(String serializeMsg) {
		try {
			return objectMapper.readValue(serializeMsg, mapObjType);
		} catch(Throwable throwable) {
			throw new RuntimeException(throwable);
		}
	}

	public static List<String> deserializeStrList(String serializeMsg) {
		try {
			return objectMapper.readValue(serializeMsg, listStrType);
		} catch(Throwable throwable) {
			throw new RuntimeException(throwable);
		}
	}

	public static <T> T deserialize(String serializeMsg, Class<T> classT) {
		try {
			return objectMapper.readValue(serializeMsg, classT);
		} catch(Throwable throwable) {
			throw new RuntimeException(throwable);
		}
	}

	public static <T> T deserialize(String serializeMsg, TypeReference<T> typeReference) {
		try {
			return objectMapper.readValue(serializeMsg, typeReference);
		} catch(Throwable throwable) {
			throw new RuntimeException(throwable);
		}
	}

	public static <T> String serialize(T Obj) {
		try {
			return objectMapper.writeValueAsString(Obj);
		} catch(Throwable throwable) {
			throw new RuntimeException(throwable);
		}
	}
}
