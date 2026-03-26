package com.train;

import com.train.platform.common.core.util.TimeId;
import com.train.platform.common.security.util.SecurityUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class TestFolder {

	@SneakyThrows
	@Test
	public void runAbsolutePath() {
		Path startPath = Paths.get("C:\\Users\\lee\\Downloads\\resource\\defected-soa\\defected"); // 替换为你的起始目录
		Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				// System.out.println(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				// 处理文件访问失败的情况
				return FileVisitResult.CONTINUE;
			}
		});
	}

	@Test
	public void runRelativePath() {
		File root = new File("C:\\Users\\lee\\Downloads\\resource\\defected-soa\\defected"); // 替换为你的目录路径
		List<File> allFiles = new ArrayList<>();
		getAllFilesRelativePath(root, allFiles);
		for (File file : allFiles) {
			// System.out.println(file.getPath());
		}
	}

	public static void getAllFilesRelativePath(File dir, List<File> fileList) {
		File[] files = dir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					getAllFilesRelativePath(file, fileList);
				} else {
					fileList.add(file);
				}
			}
		}
	}


	/**
	 * 获取目录下所有子目录
	 */
	@Test
	public void testRelative2() {
		long startTime = System.currentTimeMillis();
		Set<String> res = new HashSet<>();
		listFiles(new File("D:\\opt\\ai\\imgs\\"), "", res);

		// System.out.println(res.size());
		for (String s : res) {
			// System.out.println(s);
		}

		long endTime = System.currentTimeMillis();

		System.out.println("Execution time: " + (endTime - startTime) + " milliseconds");
	}


	public void listFiles(File folder, String prefix, Set<String> res) {
		if (folder.isDirectory()) {
			File[] files = folder.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isDirectory()) {
						res.add(prefix);
						listFiles(file, prefix + file.getName() + "/", res);
					} else {
						// res.add(prefix + file.getName());
					}
				}
			}
		}
	}

	@Test
	public void testFolder() {
		File root = new File("D:\\opt\\ai\\imgs");
		walk(root);
	}


	public void walk(File dir) {
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				// walk(file);
				// System.out.println("Dir: " + file.getAbsolutePath());
			}
		}
	}

	@Test
	public void testMap() {
		Map map = new HashMap();
		map.put("value","标注1");
		System.out.println(map.toString());
	}

	@Test
	public void testTimeId() {
		String userId = "1729042725964881921";
		String substring = TimeId.nextPkId() + userId.substring(userId.length() - 4);
		System.out.println(Long.valueOf(substring).longValue());
	}
}
