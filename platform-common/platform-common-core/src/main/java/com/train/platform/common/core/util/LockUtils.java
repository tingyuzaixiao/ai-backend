package com.train.platform.common.core.util;

public class LockUtils {
	public static <U, T> T getLock(U obj, T[] locks) {
		int index = getIndex(obj, locks.length);
		return locks[index];
	}

	public static <U, T, V> V getLock(U obj1, T obj2, V[][] locks) {
		int index1 = getIndex(obj1, locks.length);
		int index2 = getIndex(obj2, locks[0].length);
		return locks[index1][index2];
	}

	public static <U, T> T[] getLocks(U obj, T[][] locks) {
		int index = getIndex(obj, locks.length);
		return locks[index];
	}

	private static <U> int getIndex(U obj, int length) {
		int hash = obj.hashCode();
		if (hash < 0) {
			hash = -hash;
		}
		return hash % length;
	}
}
