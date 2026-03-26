package com.train.platform.common.core.util;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 时间序列ID生成器(20位)<br>
 * 时间戳+循环计数+随机数+网络地址+进程号<br>
 * 优势：<br>
 * TimeId 是有序的（有序但不连贯）<br>
 * TimeId 使用了和 SnowflakeIdWorker 类似的时间戳算法，但是冲突的概率更低<br>
 * TimeId 与 UUID 相比，大小从36个符号减少到20个符号<br>
 * 劣势：<br>
 * TimeId 是字符串，相对 SnowflakeIdWorker（ 长整型） 占用更大的空间，但是比UUID紧凑
 */
public class TimeId {
	private static long tmpID = 0;
	private static final long LOCK_TIME = 1;
	private static final long INCREASE_STEP = 1;
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmssSSS");
	private static final SimpleDateFormat sdf2 = new SimpleDateFormat("MMddHHmmss");

	private static final Lock LOCK = new ReentrantLock();


	public static long nextPkId() {
		try {
			//当前：（年、月、日、时、分、秒、毫秒）
			long timeCount;
			if (LOCK.tryLock(LOCK_TIME, TimeUnit.SECONDS)) {
				timeCount = Long.parseLong(sdf.format(new Date()));
				try {
					if (tmpID < timeCount) {
						tmpID = timeCount;
					} else {
						tmpID += INCREASE_STEP;
						timeCount = tmpID;
					}
					return timeCount;
				} finally {
					LOCK.unlock();
				}
			} else {
				return nextPkId();

			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static String getNextPkStr() {
		// 获取当前的日期和时间
		LocalDateTime now = LocalDateTime.now();

		// 格式化输出
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMddHHmm");
		return now.format(formatter);
	}

	public static String getPkStrMMddHHmmss() {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMddHHmmss");
		return now.format(formatter);
	}

	public static void main(String[] args) {
		System.out.println(getNextPkStr());
	}
}
