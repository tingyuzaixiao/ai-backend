package com.train.platform.common.core.util;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zj
 * @date 2025/9/26 文件路径类
 */
@Slf4j
public class PathUtils {
	public static String appendFilePath(String basePath, String... paths) {
		Path resolvedPath = Paths.get(basePath);
		for (String path : paths) {
			resolvedPath = resolvedPath.resolve(path);
		}
		return resolvedPath.toString();
	}

	public static String resolveUrlFileName(String fileUrl) {
		return fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
	}

	public static String resolveFileName(String filePath) {
		if (StringUtils.isBlank(filePath)) {
			return null;
		}

		Path path = Paths.get(filePath);
		return path.getFileName().toString();
	}

	public static String getLastTwoBranches(String path) {
		if (path == null || path.isEmpty()) {
			return "";
		}

		String normalizedPath = path.replace("\\", "/");

		String[] parts = normalizedPath.split("/");
		List<String> nonEmptyParts = new ArrayList<>();
		for (String part : parts) {
			if (!part.isEmpty()) {
				nonEmptyParts.add(part);
			}
		}

		if (nonEmptyParts.size() < 2) {
			return path;
		}

		String lastBranch = nonEmptyParts.get(nonEmptyParts.size() - 1);
		String secondLastBranch = nonEmptyParts.get(nonEmptyParts.size() - 2);

		return secondLastBranch + File.separator + lastBranch;
	}

	public static List<String> walkDirectory(String rootDir) {
		List<String> fileList = new ArrayList<>();
		Path startPath = Paths.get(rootDir);

		if (!Files.isDirectory(startPath)) {
			throw new IllegalArgumentException("Path is not a directory: " + rootDir);
		}

		try {
			Files.walkFileTree(startPath, new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					if (Files.isRegularFile(file)) {
						fileList.add(file.toString());
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return fileList;
	}

	public static boolean cleanFileAndDirectory(Path file) {
		if (!Files.exists(file)) {
			return false;
		}

		if (Files.isDirectory(file)) {
			throw new IllegalArgumentException("Expected a file, but got a directory: " + file);
		}

		try {
			Files.delete(file);

			Path parentDir = file.getParent();
			if (parentDir == null) {
				return true;
			}

			try (DirectoryStream<Path> stream = Files.newDirectoryStream(parentDir)) {
				if (!stream.iterator().hasNext()) {
					Files.delete(parentDir);
					return true;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return true;
	}
}
