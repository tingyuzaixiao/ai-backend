package com.train.platform.common.core.util;

import org.springframework.cglib.beans.BeanCopier;

public class ObjectCopyUtils {
	public static <T, U> void copyProperties(T source, U target) {
		try {
			BeanCopier copier = BeanCopier.create(source.getClass(), target.getClass(), false);
			copier.copy(source, target, null);
		} catch (Exception e) {
			throw new RuntimeException("object copy failed", e);
		}
	}
}
