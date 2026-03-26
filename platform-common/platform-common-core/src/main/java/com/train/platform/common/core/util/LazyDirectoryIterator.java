package com.train.platform.common.core.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.stream.Stream;

public class LazyDirectoryIterator implements Iterator<String>, AutoCloseable {
	private final Stream<Path> pathStream;
	private final Iterator<Path> pathIterator;
	private final Path root;

	public LazyDirectoryIterator(String rootDir) {
		this.root = Paths.get(rootDir);
		if (!Files.isDirectory(this.root)) {
			throw new IllegalArgumentException("Path is not a directory: " + rootDir);
		}

		try {
			pathStream = Files.walk(this.root);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		this.pathIterator = pathStream.filter(Files::isRegularFile).iterator();
	}

	@Override
	public boolean hasNext() {
		return pathIterator.hasNext();
	}

	@Override
	public String next() {
		return pathIterator.next().toString();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Remove not supported");
	}

	@Override
	public void close() {
		if (pathStream != null) {
			pathStream.close();
		}
	}

	public static void main(String[] args) {
		String rootPath = "/Users/zhangjiang/test/files";

		try (LazyDirectoryIterator iterator = new LazyDirectoryIterator(rootPath)) {
			System.out.println("Processing files one by one (no memory overhead):");
			while (iterator.hasNext()) {
				String file = iterator.next();

				System.out.println(file);
			}
		}
	}
}
