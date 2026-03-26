package com.train.platform.common.core.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

import java.util.concurrent.*;

import org.apache.commons.compress.archivers.zip.ZipFile;

import static com.train.platform.common.core.constant.CommonConstants.*;
import static com.train.platform.common.core.constant.PlatformConstants.*;
import static org.apache.commons.io.IOUtils.copy;

public class FileUtils {

	private static String NGINX_BASE_PATH = "";

	// 使用单例线程池（按需初始化）
	private static volatile ExecutorService thumbnailExecutor;

	private static final int BUFFER_SIZE = 8 * 1024;

	/**
	 * 缩略图线程
	 * @return 线程service
	 */
	private static synchronized ExecutorService getThumbnailExecutor() {
		if (thumbnailExecutor == null || thumbnailExecutor.isShutdown()) {
			int corePoolSize = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
			thumbnailExecutor = new ThreadPoolExecutor(
					corePoolSize,
					corePoolSize * 2,
					30, TimeUnit.SECONDS,
					new LinkedBlockingQueue<>(500),  // 增大队列容量
					new ThreadFactory() {
						private final AtomicInteger counter = new AtomicInteger(0);

						@Override
						public Thread newThread(Runnable r) {
							Thread t = new Thread(r);
							t.setName("thumbnail-pool-" + counter.incrementAndGet());
							t.setDaemon(true);
							return t;
						}
					},
					new ThreadPoolExecutor.CallerRunsPolicy()
			);
		}
		return thumbnailExecutor;
	}

	public static void copyDir(String sourcePath, String path) throws IOException {
		File file = new File(sourcePath);
		String[] filePath = file.list();

		if (!(new File(path)).exists()) {
			(new File(path)).mkdir();
		}

		assert filePath != null;
		for (String s : filePath) {
			if ((new File(sourcePath + File.separator + s)).isDirectory()) {
				copyDir(sourcePath + File.separator + s, path + File.separator + s);
			}

			if (new File(sourcePath + File.separator + s).isFile()) {
				copyFile(sourcePath + File.separator + s, path + File.separator + s);
			}

		}
	}

	public static void copyFile(String oldPath, String newPath) throws IOException {
		File oldFile = new File(oldPath);
		File file = new File(newPath);
		File parentDir = file.getParentFile();
		// 检查父目录是否存在
		if (parentDir != null && !parentDir.exists()) {
			parentDir.mkdirs();
		}
		// 使用try with语句自动关闭流
		try (FileInputStream in = new FileInputStream(oldFile);
			 FileOutputStream out = new FileOutputStream(file)) {
			byte[] buffer = new byte[2097152];
			int len;
			while ((len = in.read(buffer)) != -1) {
				out.write(buffer, 0, len);
			}
		}
	}

	public static String readFile(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		try {
			InputStream input = new FileInputStream(file);
			byte[] buffer = new byte[1024];
			int length = 0;
			length = input.read(buffer);
			while (length != -1) {
				sb.append(new String(buffer, 0, length));
				length = input.read(buffer);
			}
			input.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	public static Map<String, Object> unzipFile(File zipFile, File targetDir) throws IOException {
		Map<String, Object> res = new HashMap<>();
		List<File> fileList = Collections.synchronizedList(new ArrayList<>());
		List<File> labelFileList = Collections.synchronizedList(new ArrayList<>());
		boolean[] hasStandardPath = new boolean[2]; // [0: image, 1: label]

		// 创建目标目录
		if (!targetDir.exists() && !targetDir.mkdirs()) {
			throw new IOException("无法创建目录: " + targetDir.getAbsolutePath());
		}

		// 使用 Commons Compress 支持 ZIP64 和并行解压
		try (ZipFile zip = new ZipFile(zipFile)) {
			ExecutorService executor = Executors.newFixedThreadPool(
					Math.max(4, Runtime.getRuntime().availableProcessors() * 2)
			);
			List<Future<?>> futures = new ArrayList<>();

			// 遍历 ZIP 条目并提交任务
			Enumeration<ZipArchiveEntry> entries = zip.getEntries();
			while (entries.hasMoreElements()) {
				ZipArchiveEntry entry = entries.nextElement();
				futures.add(executor.submit(() -> {
					try {
						processEntry(zip, entry, targetDir, fileList, labelFileList, hasStandardPath);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}));
			}

			// 等待所有任务完成
			for (Future<?> future : futures) future.get();
			executor.shutdown();
		} catch (Exception e) {
			throw new IOException("解压失败: " + e.getMessage(), e);
		}

		res.put(LABEL_CONFIG_FILES, labelFileList);
		res.put(FILE_LIST, fileList);
		res.put(IMAGES_STANDARD_FORMAT_PATH, hasStandardPath[0]);
		res.put(LABEL_STANDARD_FORMAT_PATH, hasStandardPath[1]);
		return res;
	}

	private static void processEntry(ZipFile zip, ZipArchiveEntry entry, File targetDir,
									 List<File> fileList, List<File> labelFileList,
									 boolean[] hasStandardPath) throws IOException {
		if (entry.isDirectory()) return;

		// 路径安全校验（防路径遍历攻击）
		Path destPath = targetDir.toPath().resolve(entry.getName()).normalize();
		if (!destPath.startsWith(targetDir.toPath())) {
			throw new SecurityException("非法路径: " + entry.getName());
		}
		Files.createDirectories(destPath.getParent());

		// NIO 零拷贝写入（避免内存缓冲）
		try (InputStream is = zip.getInputStream(entry);
			 FileChannel outChannel = FileChannel.open(destPath,
					 StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			outChannel.transferFrom(Channels.newChannel(is), 0, Long.MAX_VALUE);
		}

		File currentFile = destPath.toFile();
		fileList.add(currentFile);

		// 分类标记
		String entryPath = entry.getName();
		if (entryPath.startsWith(IMG_FOLDER)) {
			hasStandardPath[0] = true;
		} else if (entryPath.startsWith(LABEL_FOLDER)) {
			hasStandardPath[1] = true;
			if (entryPath.endsWith(".json")) {
				labelFileList.add(currentFile);
			}
		}
	}

	public static Map<String, Object> unzipInputStream(MultipartFile zipFile, File targetDir) throws IOException {
		// 缓冲区大小调整为128KB (131072 bytes)
		final int BUFFER_SIZE = 128 * 1024;
		Map<String, Object> res = new HashMap<>();
		List<File> fileList = new ArrayList<>();
		List<File> labelFileList = new ArrayList<>();
		boolean hasImageStandardPath = false;
		boolean hasLabelStandardPath = false;

		if (!targetDir.exists() && !targetDir.mkdirs()) {
			throw new IOException("无法创建目标目录：" + targetDir.getAbsolutePath());
		}

		try (InputStream is = zipFile.getInputStream();
			 ZipInputStream zipIn = new ZipInputStream(new BufferedInputStream(is, BUFFER_SIZE))) {

			byte[] buffer = new byte[BUFFER_SIZE];
			ZipEntry entry;

			while ((entry = zipIn.getNextEntry()) != null) {
				// 跳过空条目和目录
				if (entry.isDirectory() || entry.getName().isEmpty()) continue;

				Path resolvedPath = targetDir.toPath().resolve(entry.getName()).normalize();
				// 路径安全校验
				if (!resolvedPath.startsWith(targetDir.toPath())) {
					throw new SecurityException("非法路径：" + entry.getName());
				}

				// 创建父目录
				Files.createDirectories(resolvedPath.getParent());

				// 使用NIO写入文件，自定义缓冲区提升大文件性能
				try (OutputStream fos = new BufferedOutputStream(
						Files.newOutputStream(resolvedPath), BUFFER_SIZE)) {

					int bytesRead;
					while ((bytesRead = zipIn.read(buffer)) != -1) {
						fos.write(buffer, 0, bytesRead);
					}
				}

				File currentFile = resolvedPath.toFile();
				fileList.add(currentFile);

				// 分类处理文件
				String entryPath = entry.getName();
				if (entryPath.startsWith(IMG_FOLDER)) {
					hasImageStandardPath = true;
				} else if (entryPath.startsWith(LABEL_FOLDER)) {
					hasLabelStandardPath = true;
					if (currentFile.getName().endsWith(".json")) {
						labelFileList.add(currentFile);
					}
				}
				zipIn.closeEntry(); // 显式关闭当前条目
			}
		} catch (IOException ex) {
			// 清理已写入的文件（部分解压时出错）
			cleanPartialFiles(fileList);
			throw new IOException("解压失败：" + ex.getMessage(), ex);
		}

		res.put(LABEL_CONFIG_FILES, labelFileList);
		res.put(FILE_LIST, Collections.unmodifiableList(fileList));
		res.put(IMAGES_STANDARD_FORMAT_PATH, hasImageStandardPath);
		res.put(LABEL_STANDARD_FORMAT_PATH, hasLabelStandardPath);
		return res;
	}

	// 清理部分解压的文件
	private static void cleanPartialFiles(List<File> files) {
		for (File file : files) {
			try {
				Files.deleteIfExists(file.toPath());
			} catch (IOException e) {
				// 记录日志，但不要中断主流程
			}
		}
	}

	@SneakyThrows
	public static Map<String, Object> unTarInputStream(MultipartFile file, String descDir) {
		Map<String, Object> res = new HashMap<>();
		List<File> fileList = new ArrayList<>();
		List<File> labelFileList = new ArrayList<>();
		boolean imagesStandardFormatPath = false;
		boolean labelStandardFormatPath = false;

		InputStream fis = null, bis = null;
		ArchiveInputStream<TarArchiveEntry> ais = null;
		try {
			fis = file.getInputStream();
			bis = new BufferedInputStream(fis);
			ais = new TarArchiveInputStream(bis);
			ArchiveEntry entry;
			while (Objects.nonNull(entry = ais.getNextEntry())) {
				if (!ais.canReadEntryData(entry)) {
					continue;
				}
				String name = descDir + File.separator + entry.getName();
				File f = new File(name);
				if (entry.isDirectory()) {
					if (!f.isDirectory() && !f.mkdirs()) {
						f.mkdirs();
					}
				} else {
					File parent = f.getParentFile();
					if (!parent.isDirectory() && !parent.mkdirs()) {
						throw new IOException("failed to create directory " + parent);
					}
					try (OutputStream o = Files.newOutputStream(f.toPath())) {
						copy(ais, o);
					}

					if (f.getPath().contains(BASE_IMAGE_FOLDER)) {
						imagesStandardFormatPath = true;
					} else if (f.getPath().contains(BASE_LABEL_FOLDER)) {
						labelStandardFormatPath = true;
					} else {
						break;
					}

					if (f.getName().endsWith(".json")) {
						labelFileList.add(f);
					}
					fileList.add(f);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			ais.close();
			bis.close();
			fis.close();

		}
		res.put(LABEL_CONFIG_FILES, labelFileList);
		res.put(FILE_LIST, fileList);
		res.put(IMAGES_STANDARD_FORMAT_PATH, imagesStandardFormatPath);
		res.put(LABEL_STANDARD_FORMAT_PATH, labelStandardFormatPath);
		return res;
	}

	public static Map<String, Object> unTarFile(File file, String descDir) {
		Map<String, Object> res = new HashMap<>();
		List<File> fileList = new ArrayList<>();
		boolean imagesStandardFormatPath = false;
		boolean labelStandardFormatPath = false;
		List<File> labelFileList = new ArrayList<>();

		try (InputStream fis = new FileInputStream(file);
			 InputStream bis = new BufferedInputStream(fis);
			 ArchiveInputStream<TarArchiveEntry> ais = new TarArchiveInputStream(bis);
		) {
			ArchiveEntry entry;
			while (Objects.nonNull(entry = ais.getNextEntry())) {
				if (!ais.canReadEntryData(entry)) {
					continue;
				}
				String name = descDir + File.separator + entry.getName();
				File f = new File(name);
				if (entry.isDirectory()) {
					if (!f.isDirectory() && !f.mkdirs()) {
						f.mkdirs();
					}
				} else {
					File parent = f.getParentFile();
					if (!parent.isDirectory() && !parent.mkdirs()) {
						throw new IOException("failed to create directory " + parent);
					}

					try (OutputStream o = Files.newOutputStream(f.toPath())) {
						copy(ais, o);
					}

					if (f.getPath().contains(BASE_IMAGE_FOLDER)) {
						imagesStandardFormatPath = true;
					} else if (f.getPath().contains(BASE_LABEL_FOLDER)) {
						labelStandardFormatPath = true;
					} else {
						break;
					}

					if (f.getName().endsWith(".json")) {
						labelFileList.add(f);
					}
					fileList.add(f);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		res.put(LABEL_CONFIG_FILES, labelFileList);
		res.put(FILE_LIST, fileList);
		res.put(IMAGES_STANDARD_FORMAT_PATH, imagesStandardFormatPath);
		res.put(LABEL_STANDARD_FORMAT_PATH, labelStandardFormatPath);
		return res;
	}

	public static File unGZIP(MultipartFile file) {
		String fileName = file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf("."));
		String finalName = File.separator + fileName;

		InputStream fis = null;
		BufferedInputStream bis = null;
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		GzipCompressorInputStream gcis = null;
		try {
			fis = file.getInputStream();
			bis = new BufferedInputStream(fis);
			fos = new FileOutputStream(finalName);
			bos = new BufferedOutputStream(fos);
			gcis = new GzipCompressorInputStream(bis);
			byte[] buffer = new byte[1024];
			int read = -1;
			while ((read = gcis.read(buffer)) != -1) {
				bos.write(buffer, 0, read);
			}
		} catch (Exception e) {
			//throw e;
			e.printStackTrace();
		} finally {
			try {
				gcis.close();
				bos.close();
				fos.close();
				bis.close();
				fis.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return new File(finalName);
	}


	/**
	 * 解压GZIP文件（支持MultipartFile和File类型）
	 *
	 * @param gzipSource 压缩文件源（MultipartFile或File类型）
	 * @param outputDir  输出目录
	 * @return 解压后的文件
	 * @throws IOException 解压失败时抛出
	 */
	public static File unGzip(Object gzipSource, File outputDir) throws IOException {
		if (gzipSource instanceof MultipartFile) {
			return unGzip((MultipartFile) gzipSource, outputDir);
		} else if (gzipSource instanceof File) {
			return unGzip((File) gzipSource, outputDir);
		} else {
			throw new IllegalArgumentException("不支持的文件类型: " +
					(gzipSource != null ? gzipSource.getClass().getName() : "null"));
		}
	}

	/**
	 * 解压MultipartFile类型的GZIP文件
	 */
	public static File unGzip(MultipartFile gzipFile, File outputDir) throws IOException {
		final int BUFFER_SIZE = 128 * 1024; // 128KB buffer
		// 获取原始文件名并处理扩展名
		String originalFilename = gzipFile.getOriginalFilename();
		String baseName = getBaseName(originalFilename);

		// 创建输出目录（如果不存在）
		createOutputDir(outputDir);

		// 创建输出文件
		File outputFile = new File(outputDir, baseName);

		try (InputStream gzipStream = new BufferedInputStream(gzipFile.getInputStream(), BUFFER_SIZE);
			 GZIPInputStream gzipIn = new GZIPInputStream(gzipStream, BUFFER_SIZE);
			 OutputStream fileOut = new BufferedOutputStream(
					 Files.newOutputStream(outputFile.toPath()), BUFFER_SIZE)) {

			// 使用大缓冲区进行流式复制
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead;
			while ((bytesRead = gzipIn.read(buffer)) != -1) {
				fileOut.write(buffer, 0, bytesRead);
			}

			return outputFile;
		} catch (IOException e) {
			// 失败时删除可能已部分写入的文件
			Files.deleteIfExists(outputFile.toPath());
			throw new IOException("GZIP解压失败: " + e.getMessage(), e);
		}
	}

	/**
	 * 解压File类型的GZIP文件
	 */
	public static File unGzip(File gzipFile, File outputDir) throws IOException {
		final int BUFFER_SIZE = 128 * 1024; // 128KB buffer
		// 验证输入文件
		if (!gzipFile.exists()) {
			throw new FileNotFoundException("文件不存在: " + gzipFile.getAbsolutePath());
		}
		if (!gzipFile.isFile()) {
			throw new IOException("不是文件: " + gzipFile.getAbsolutePath());
		}

		// 处理文件名
		String baseName = getBaseName(gzipFile.getName());

		// 创建输出目录
		createOutputDir(outputDir);

		// 创建输出文件
		File outputFile = new File(outputDir, baseName);

		try (InputStream fileIn = new BufferedInputStream(
				new FileInputStream(gzipFile), BUFFER_SIZE);
			 GZIPInputStream gzipIn = new GZIPInputStream(fileIn, BUFFER_SIZE);
			 OutputStream fileOut = new BufferedOutputStream(
					 Files.newOutputStream(outputFile.toPath()), BUFFER_SIZE)) {

			// 使用NIO的transferTo方法（JDK9+）更高效
			if (useNioTransfer()) {
				gzipIn.transferTo(fileOut);
			} else {
				// 兼容JDK8的复制方式
				byte[] buffer = new byte[BUFFER_SIZE];
				int bytesRead;
				while ((bytesRead = gzipIn.read(buffer)) != -1) {
					fileOut.write(buffer, 0, bytesRead);
				}
			}

			return outputFile;
		} catch (IOException e) {
			// 失败时删除可能已部分写入的文件
			Files.deleteIfExists(outputFile.toPath());
			throw new IOException("GZIP解压失败: " + e.getMessage(), e);
		}
	}

	/**
	 * 获取无扩展名的基本文件名（自动去除.gz后缀）
	 */
	private static String getBaseName(String filename) {
		if (filename == null) return "unnamed_file";

		// 处理多重扩展名（如.tar.gz）
		String base = filename;
		while (base.toLowerCase().endsWith(".gz")) {
			base = base.substring(0, base.length() - 3);
		}

		// 如果多重处理后文件名变空，使用原始文件名
		return base.isEmpty() ? filename : base;
	}

	/**
	 * 创建输出目录（如果不存在）
	 */
	private static void createOutputDir(File outputDir) throws IOException {
		if (!outputDir.exists() && !outputDir.mkdirs()) {
			throw new IOException("无法创建输出目录: " + outputDir.getAbsolutePath());
		}
		if (!outputDir.isDirectory()) {
			throw new IOException("输出路径不是目录: " + outputDir.getAbsolutePath());
		}
	}

	/**
	 * 检查是否可以使用NIO transferTo（JDK9+）
	 */
	private static boolean useNioTransfer() {
		try {
			// 检查transferTo方法是否存在（JDK9+）
			InputStream.class.getMethod("transferTo", OutputStream.class);
			return true;
		} catch (NoSuchMethodException e) {
			return false;
		}
	}

	/**
	 * 高级用法：直接解压到指定文件路径
	 */
	public static File unGzipToPath(Object gzipSource, Path outputPath) throws IOException {
		File outputDir = outputPath.getParent().toFile();
		File outputFile = unGzip(gzipSource, outputDir);

		// 如果指定了不同的文件名，重命名文件
		if (!outputFile.getName().equals(outputPath.getFileName().toString())) {
			Path target = Files.move(
					outputFile.toPath(),
					outputPath,
					StandardCopyOption.REPLACE_EXISTING
			);
			return target.toFile();
		}
		return outputFile;
	}

	public static void walkFile(File dir, boolean isSubDirectory, Set<String> res) {
		for (File file : Objects.requireNonNull(dir.listFiles())) {
			if (file.isDirectory()) {
				if (isSubDirectory) {
					walkFile(file, true, res);
				}
			}

			if (file.isFile()) {
				res.add(file.getPath());
			}
		}
	}

	//根据模型名称、数据集名称、项目实验名称拼接路径
	public static String getPath(Map<String, String> pathParams) {
		String configType = pathParams.getOrDefault("configType", null);
		String type = pathParams.get("type");
		StringBuilder sb = new StringBuilder();
		String separator = FileUtil.FILE_SEPARATOR;

		if (configType != null && configType.equals("id")) {
			String modelId = pathParams.getOrDefault("modelId", "");
			String datasetId = pathParams.getOrDefault("datasetId", "");

			switch (type) {
				case MODEL_FOLDER -> {
					sb.append(MODEL_FOLDER);
					String baseModelId = pathParams.getOrDefault("baseModelId", "");
					if (Objects.equals(modelId, "")) {
						sb.append(separator).append(baseModelId).append(separator).append(BASE_FOLDER);
					} else {
						sb.append(separator).append(baseModelId).append(separator).append(modelId);
					}
				}
				case DATASET_FOLDER -> {
					sb.append(DATASET_FOLDER);
					String baseDatasetId = pathParams.getOrDefault("baseDatasetId", "");
					if (Objects.equals(datasetId, "")) {
						sb.append(separator).append(baseDatasetId).append(separator).append(BASE_FOLDER);
					} else {
						sb.append(separator).append(baseDatasetId).append(separator).append(datasetId);
					}
				}
				case PROGRAM_FOLDER -> {
					sb.append(PROGRAM_FOLDER);
					String experimentId = pathParams.getOrDefault("experimentId", "");
					String programId = pathParams.get("programId");
					if (Objects.equals(experimentId, "")) {
						sb.append(separator).append(programId);
					} else {
						sb.append(separator).append(programId).append(separator).append(experimentId);
					}
				}
				default -> {
				}
			}
		} else {
			String modelName = pathParams.getOrDefault("modelName", "");
			String datasetName = pathParams.getOrDefault("datasetName", "");

			switch (type) {
				case MODEL_FOLDER -> {
					sb.append(MODEL_FOLDER);
					String baseModelName = pathParams.getOrDefault("baseModelName", "");
					if (Objects.equals(modelName, "")) {
						sb.append(separator).append(baseModelName).append(separator).append(BASE_FOLDER);
					} else {
						sb.append(separator).append(baseModelName).append(separator).append(modelName);
					}
				}
				case DATASET_FOLDER -> {
					sb.append(DATASET_FOLDER);
					String baseDatasetName = pathParams.getOrDefault(BASE_DATASET_NAME, "");
					if (Objects.equals(datasetName, "")) {
						sb.append(separator).append(baseDatasetName).append(separator).append(BASE_FOLDER);
					} else {
						sb.append(separator).append(baseDatasetName).append(separator).append(datasetName);
					}
				}
				case PROGRAM_FOLDER -> {
					sb.append(PROGRAM_FOLDER);
					String experimentName = pathParams.getOrDefault("experimentName", "");
					String programName = pathParams.get("programName");
					if (Objects.equals(experimentName, "")) {
						sb.append(separator).append(programName);
					} else {
						sb.append(separator).append(programName).append(separator).append(experimentName);
					}
				}
				default -> {
				}
			}
		}
		sb.append(separator);
		return sb.toString();
	}

	@SneakyThrows
	public static boolean diagStandardFormatPath(MultipartFile file) {
		boolean imageFlag = false, labelFlag = false;

		String extName = FileUtil.extName(file.getOriginalFilename());
		if (ZIP.equals(extName)) {
			ZipInputStream zipIn = new ZipInputStream(file.getInputStream());
			ZipEntry zipEntry;

			while ((zipEntry = zipIn.getNextEntry()) != null) {
				String entryPath = zipEntry.getName();
				if (entryPath.startsWith(IMG_FOLDER)) {
					imageFlag = true;
				} else if (entryPath.startsWith(LABEL_FOLDER)) {
					labelFlag = true;
				} else {
					imageFlag = labelFlag = false;
				}
			}
		}
		return labelFlag && imageFlag;
	}

	public static void downloadZip(String sourcePath, String fileName, HttpServletResponse response) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String downloadName = dateFormat.format(new Date()) + fileName;
		try {
			ServletOutputStream os = response.getOutputStream();
			byte[] data = createZip(sourcePath);
			response.reset();
			response.setCharacterEncoding("UTF-8");
			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Access-Control-Allow-Methods", "*");
			response.setHeader("Access-Control-Expose-Headers", "*");
			response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(downloadName,
					StandardCharsets.UTF_8));
			response.addHeader("Content-Length", String.valueOf(data.length));
			response.setContentType("application/octet-stream;charset=UTF_8");
			IOUtils.write(data, os);
			os.flush();
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static byte[] createZip(String srcSource) throws Exception {
		byte[] byteArray;
		//将目标文件打包成zip导出
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			 ZipOutputStream zip = new ZipOutputStream(outputStream)) {
			File file = new File(srcSource);
			createAllFile(zip, file, "");
			byteArray = outputStream.toByteArray();
		}

		return byteArray;
	}

	public static void createAllFile(ZipOutputStream zip, File file, String baseDir) throws IOException {
		String entryPath = baseDir.isEmpty() ? file.getName() : baseDir + "/" + file.getName();

		if (file.isDirectory()) {
			// 确保目录路径以"/"结尾，兼容空目录
			if (!entryPath.endsWith("/")) {
				entryPath += "/";
			}
			zip.putNextEntry(new ZipEntry(entryPath));
			zip.closeEntry();  // 显式关闭目录条目

			// 递归处理子文件和目录
			File[] files = file.listFiles();
			if (files != null) {  // 避免空指针
				for (File child : files) {
					createAllFile(zip, child, entryPath);
				}
			}
		} else {
			// 使用缓冲区减少小文件频繁I/O
			try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
				ZipEntry entry = new ZipEntry(entryPath);
				zip.putNextEntry(entry);
				IOUtils.copy(bis, zip);  // 使用Apache Commons IO流复制，避免内存溢出
				zip.closeEntry();  // 显式关闭文件条目
			}
		}
	}

	/**
	 * 压缩本地图片保存到本地
	 *
	 * @param path          保存文件的路径，精确到最后一个文件夹
	 * @param localPhotoUrl 本地图片路径，精确到文件
	 * @param fileName      新的文件名
	 * @return
	 */
	public static String localImageCompress(String path, String localPhotoUrl, String fileName) {
		try {
			File f = new File(localPhotoUrl);
			long sourceImgValue = f.length() / 1024;
			BufferedImage sourceImg = ImageIO.read(new FileInputStream(f));
			int width = sourceImg.getWidth();//原图宽度
			int height = sourceImg.getHeight();//原图高度
			int hightValue = width;//宽度和高度的最大值
			if (height > width) hightValue = height;

			double scale111 = 0.5;
			if (1000 <= hightValue && hightValue < 2000) scale111 = 0.3;
			if (2000 <= hightValue && hightValue < 3000) scale111 = 0.2;
			if (3000 <= hightValue && hightValue < 4000) scale111 = 0.1;
			if (4000 <= hightValue) scale111 = BigDecimal.valueOf(500L)
					.divide(BigDecimal.valueOf(hightValue), 2, BigDecimal.ROUND_UP).doubleValue();
			System.out.println("最终scale为：" + scale111);

			/**
			 * 图片小于5M，压缩图片最大尺寸为512K
			 * 大于5M，小于10M，压缩图片最大尺寸为1M
			 * 大于10M，压缩图片最大尺寸为2M
			 */
			int maxSize = 512;//压缩图片的最大尺寸
			if (sourceImgValue < 5 * 1024) maxSize = 512;
			if (5 * 1024 <= sourceImgValue && sourceImgValue < 10 * 1024) maxSize = 1024;
			if (10 * 1024 <= sourceImgValue) maxSize = 2 * 1024;

			String thumbnailPath = path + fileName;
			Thumbnails.of(localPhotoUrl)
					.scale(scale111)
					.outputQuality(0.9)
					.toFile(thumbnailPath);

			File f1 = new File(thumbnailPath);
			long returnValue = f1.length() / 1024;
			System.out.println("第一次生成图片缩略图，大小为：" + returnValue + "KB");
			// 如果图片大小超过1M，则继续压缩，直到图片大小小于或等于149KB
			if (returnValue > maxSize) {
				for (int i = 0; i < 4; i++) {
					double scale = (i + 1) * 0.1;
					double scaleQuality = 0.9 - scale * 2;
					if (scale111 > scale) scale111 = scale111 - scale;
					System.out.println("最终scale为：" + scale111);
					System.out.println("最终scaleQuality为：" + scaleQuality);
					Thumbnails.of(localPhotoUrl)
							.scale(scale111)
							.outputQuality(scaleQuality)
							.toFile(thumbnailPath);
					File f2 = new File(thumbnailPath);
					returnValue = f2.length() / 1024;
					System.out.println("继续压缩图片大小：" + returnValue + "KB");
					if (returnValue > maxSize) {
						continue;
					} else {
						break;
					}
				}
			}
			return thumbnailPath;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


	public static File getThumbnail(MultipartFile file, String fileName) {
		File thFile = new File(fileName);
		try {
			Thumbnails.Builder<? extends InputStream> builder = Thumbnails.of(new InputStream[]{file.getInputStream()});
			builder.outputFormat(FileNameUtil.extName(fileName));
			builder.size(200, 200);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			builder.toOutputStream(out);

			try (FileOutputStream fileOutStream = new FileOutputStream(thFile)) {
				out.writeTo(fileOutStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return thFile;
	}

	public static String generateRandomHexColor() {
		Random random = new Random();
		// 生成 R, G, B 值
		int r = random.nextInt(256); // 0 - 255
		int g = random.nextInt(256); // 0 - 255
		int b = random.nextInt(256); // 0 - 255

		// 将 R, G, B 转换为 2 位的 16 进制字符串
		String hexR = String.format("%02X", r);
		String hexG = String.format("%02X", g);
		String hexB = String.format("%02X", b);

		// 拼接成完整的 16 进制颜色代码
		return "#" + hexR + hexG + hexB;
	}

	/**
	 * 压缩图像并返回输入流
	 *
	 * @param inputStream 原始图像输入流
	 * @param quality     压缩质量（0.0-1.0）
	 * @param format      输出格式（如 "jpg", "png", "webp"）
	 * @return 压缩后的输入流
	 */
	public static InputStream compressToInputstream(InputStream inputStream, float quality, String format) {
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			// 压缩到内存流
			Thumbnails.of(inputStream)
					.scale(1.0)           // 保持原始尺寸
					.outputQuality(quality)
					.outputFormat(format)
					.toOutputStream(outputStream);

			// 将输出流转换为输入流
			return new ByteArrayInputStream(outputStream.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 分块压缩大文件（支持10MB+图像）
	 *
	 * @param inputStream  原始文件输入流
	 * @param outputStream 压缩后的输出流
	 * @param quality      压缩质量（0.0-1.0）
	 * @param format       输出格式（如 "jpg", "webp"）
	 * @param bufferSize   分块缓冲区大小（建议 4KB-8KB）
	 */
	public static void compressWithChunk(InputStream inputStream,
										 OutputStream outputStream,
										 float quality,
										 String format,
										 int bufferSize) throws IOException {

		try (BufferedInputStream bis = new BufferedInputStream(inputStream, bufferSize)) {
			// 使用 Thumbnailator 流式处理（自动分块读取）
			Thumbnails.of(bis)
					.scale(1.0)
					.outputQuality(quality)
					.outputFormat(format)
					.toOutputStream(outputStream);
		}
	}

	/**
	 * 递归压缩控制（确保压缩后文件 ≤ 目标大小）
	 *
	 * @param inputStream 原始文件流
	 * @param maxSizeKB   目标最大大小（单位：KB）
	 */
	public static ByteArrayInputStream compressToTargetSize(InputStream inputStream,
															int maxSizeKB) throws IOException {

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			float quality = 0.8f; // 初始质量
			int bufferSize = 4096; // 4KB 分块缓冲

			do {
				outputStream.reset(); // 清空上一次压缩结果
				compressWithChunk(inputStream, outputStream, quality, "png", bufferSize);
				inputStream.reset();  // 重置输入流（需支持 mark/reset）
				quality -= 0.1f;      // 逐步降低质量
			} while (outputStream.size() > maxSizeKB * 1024 && quality > 0.1f);

			return new ByteArrayInputStream(outputStream.toByteArray());
		}
	}

	/**
	 * 分片压缩大图并转换为输入流
	 *
	 * @param inputStream 原始图输入流
	 * @param maxSizeKB   目标最大大小（单位KB）
	 * @param format      输出格式（jpg/png/webp）
	 */
	public static InputStream compressToStream(InputStream inputStream, int maxSizeKB, String format) {

		final int BUFFER_SIZE = 8 * 1024;
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			 BufferedInputStream bis = new BufferedInputStream(inputStream, BUFFER_SIZE)) {

			float quality = 0.8f;
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead;

			// 分块读取并压缩
			while ((bytesRead = bis.read(buffer)) != -1) {
				try (ByteArrayInputStream chunkStream = new ByteArrayInputStream(buffer, 0, bytesRead)) {
					Thumbnails.of(chunkStream)
							.scale(1)
							.outputQuality(quality)
							.outputFormat(format)
							.toOutputStream(outputStream);
				}
			}

			// 递归调整质量直到达标
			while (outputStream.size() > maxSizeKB * 1024 && quality > 0.1f) {
				quality -= 0.1f;
				outputStream.reset();
				compressToStream(inputStream, maxSizeKB, format); // 递归调用
			}

			return new ByteArrayInputStream(outputStream.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// ===== 参数验证方法 =====
	public static void validateParameters(String rootDirectory, int port) {
		// 校验目录路径格式
		if (!rootDirectory.matches("^[a-zA-Z]:\\\\[^*|\"<>?\\n]*|^/[^*|\"<>?\\n]*")) {
			throw new IllegalArgumentException("无效的目录路径格式");
		}

		// 校验端口范围
		if (port < 1 || port > 65535) {
			throw new IllegalArgumentException("端口必须在1-65535之间");
		}

		// 校验原始目录存在性
		if (!Files.exists(Paths.get(NGINX_BASE_PATH))) {
			throw new IllegalArgumentException("Nginx目录不存在: " + NGINX_BASE_PATH);
		}
	}

	// ===== 目录操作方法 =====
	public static Path createTempDirectory() throws IOException {
		return Files.createTempDirectory("nginx_bundle_");
	}

	public static Path copyNginxDirectory(Path targetDir, String nginxFolder) throws IOException {
		NGINX_BASE_PATH = nginxFolder;
		Path source = Paths.get(NGINX_BASE_PATH);
		Path target = targetDir.resolve("nginx-1.28.0");

		// 递归复制目录（保留文件属性和符号链接）
		Files.walk(source).forEach(sourcePath -> {
			try {
				Path destPath = target.resolve(source.relativize(sourcePath));
				if (Files.isSymbolicLink(sourcePath)) {
					Files.createSymbolicLink(destPath, Files.readSymbolicLink(sourcePath));
				} else {
					Files.copy(sourcePath, destPath, StandardCopyOption.COPY_ATTRIBUTES);
				}
			} catch (IOException e) {
				throw new UncheckedIOException("目录复制失败: " + sourcePath, e);
			}
		});
		return target;
	}

	// ===== 配置文件生成与替换 =====
	public static void generateAndReplaceConfig(Path nginxDir, String rootDir, int port, String serverUri) throws IOException {
		Path configPath = nginxDir.resolve("conf/nginx.conf");

		// 生成新的nginx.conf内容[2,6](@ref)
		String configContent = generateNginxConfig(rootDir, port, serverUri);

		// 写入配置文件
		Files.write(configPath, configContent.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
	}

	public static String generateNginxConfig(String rootDir, int port, String serverUri) {
		// 处理Windows路径格式转换
		String nginxRootPath = rootDir.replace("\\", "/");

		return String.format(
				"worker_processes  1;\n\n" +
						"events {\n" +
						"    worker_connections  1024;\n" +
						"}\n\n" +
						"http {\n" +
						"    include       mime.types;\n" +
						"    default_type  application/octet-stream;\n" +
						"    sendfile        on;\n" +
						"    charset         utf-8;\n" +
						"    keepalive_timeout  65;\n\n" +
						"    # 文件服务器配置[2,6](@ref)\n" +
						"    server {\n" +
						"        listen       %d;\n" +
						"        server_name  localhost;\n\n" +
						"        # 文件服务位置[8](@ref)\n" +
						"        location %s {\n" +
						"            alias      %s;\n" +
						"            autoindex on;   # 启用目录列表\n" +
						"            \n" +
						"            # 安全头部[3](@ref)\n" +
						"            add_header 'Access-Control-Allow-Origin' '*' always;\n" +
						"            add_header 'Access-Control-Allow-Methods' 'GET, OPTIONS' always;\n" +
						"            add_header 'Access-Control-Allow-Headers' 'DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type' always;\n\n" +
						"            if ($request_method = 'OPTIONS') {\n" +
						"               add_header 'Access-Control-Max-Age' 1728000;\n" +
						"               add_header 'Content-Type' 'text/plain; charset=utf-8';\n" +
						"               return 204;\n" +
						"            }\n" +
						"        }\n\n" +
						"        error_page   500 502 503 504  /50x.html;\n" +
						"        location = /50x.html {\n" +
						"            root   html;\n" +
						"        }\n" +
						"    }\n" +
						"}\n",
				port, serverUri, nginxRootPath
		);
	}


	// ===== 响应构建方法 =====
	public static ResponseEntity<InputStreamResource> buildDownloadResponse(Path zipPath) throws IOException {
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=nginx_config_bundle.zip")
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.contentLength(Files.size(zipPath))
				.body(new InputStreamResource(Files.newInputStream(zipPath)));
	}

	// ===== 清理方法 =====
	public static void cleanTempDirectoryAsync(Path tempDir) {
		new Thread(() -> {
			try {
				Files.walk(tempDir)
						.sorted(Comparator.reverseOrder())
						.forEach(path -> {
							try {
								Files.deleteIfExists(path);
							} catch (IOException ignored) {
							}
						});
			} catch (IOException e) {
				System.err.println("临时文件清理失败: " + e.getMessage());
			}
		}).start();
	}

	public static void downloadChunk(String filePath, String fileName, Integer chunkIndex, Integer chunkTotal, Integer chunkSize, HttpServletResponse response) {
		String path = filePath + FileUtil.FILE_SEPARATOR + fileName;
		File resultFile = new File(path);
		long offset = (long) chunkSize * (chunkIndex - 1);

		if (Objects.equals(chunkIndex, chunkTotal)) {
			offset = resultFile.length() - chunkSize;
		}

		byte[] chunk = getChunk(chunkSize, path, offset);

		response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
		response.addHeader("Content-Length", "" + (chunk.length));
		response.setHeader("filename", fileName);
		response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName,
				StandardCharsets.UTF_8));

		response.setContentType("application/octet-stream");
		ServletOutputStream out = null;
		try {
			out = response.getOutputStream();
			out.write(chunk);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static byte[] getChunk(Integer chunkSize, String resultFileName, long offset) {
		try (RandomAccessFile randomAccessFile = new RandomAccessFile(resultFileName, "r")) {
			// 定位到该分片的偏移量
			randomAccessFile.seek(offset);
			//读取
			byte[] buffer = new byte[chunkSize];
			randomAccessFile.read(buffer);
			return buffer;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Map<String, Object> unzipLargeFile(InputStream inputStream, File targetDir) throws IOException {
		Map<String, Object> res = new HashMap<>();
		List<File> fileList = new ArrayList<>();
		List<File> labelFileList = new ArrayList<>();
		boolean hasImageStandardPath = false;
		boolean hasLabelStandardPath = false;
		final int BUFFER_SIZE = 128 * 1024; // 128KB缓冲区

		if (!targetDir.exists() && !targetDir.mkdirs()) {
			throw new IOException("无法创建目标目录: " + targetDir.getAbsolutePath());
		}

		// 异步任务提交计数器（仅用于异常时取消任务）
		List<Future<?>> thumbnailFutures = new ArrayList<>();
		ExecutorService executor = getThumbnailExecutor();

		try (inputStream; ZipInputStream zipIn = new ZipInputStream(new BufferedInputStream(inputStream, BUFFER_SIZE))) {
			byte[] buffer = new byte[BUFFER_SIZE];
			ZipEntry entry;

			while ((entry = zipIn.getNextEntry()) != null) {
				String entryPath = entry.getName();
				// 跳过目录和无效条目
				if (entry.isDirectory() || entryPath.isEmpty()) continue;

				Path resolvedPath = targetDir.toPath().resolve(entryPath).normalize();
				// 安全校验
				if (!resolvedPath.startsWith(targetDir.toPath())) {
					throw new SecurityException("非法路径: " + entryPath);
				}

				// 创建父目录
				Files.createDirectories(resolvedPath.getParent());

				// 大文件写入（使用缓冲流）
				try (FileOutputStream fos = new FileOutputStream(resolvedPath.toFile());
					 BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER_SIZE)) {
					int bytesRead;
					while ((bytesRead = zipIn.read(buffer)) != -1) {
						bos.write(buffer, 0, bytesRead);
					}
					// 修改点2：强制刷新并关闭底层文件描述符
					bos.flush();
					fos.getFD().sync(); // 强制同步到磁盘
				}

				File currentFile = resolvedPath.toFile();
				currentFile.setWritable(true);
				fileList.add(currentFile);

				// 文件分类处理

				if (entryPath.startsWith(IMG_FOLDER)) {
					hasImageStandardPath = true;

					//TODO 如果是图片，立即生成缩略图
					if (isImageFile(entryPath)) {
						String thumbnailBasePath = resolvedPath.getParent().toString().replace("imgs", "imgs" + FileUtil.FILE_SEPARATOR + "thumbnails");
						Path thumbnailDir =  Paths.get(thumbnailBasePath).toAbsolutePath().normalize();
						if (!Files.exists(thumbnailDir)) {
							Files.createDirectories(thumbnailDir);
						}
						// 提交异步任务（不等待）
						thumbnailFutures.add(executor.submit(() -> {
							try {
								generateThumbnail(resolvedPath, thumbnailBasePath, entryPath);
							} catch (Exception e) {
								// 记录错误但不影响主流程
								System.err.println("缩略图生成失败: " + resolvedPath);
								e.printStackTrace();
							}
						}));
					}
				} else if (entryPath.startsWith(LABEL_FOLDER)) {
					hasLabelStandardPath = true;
					if (currentFile.getName().endsWith(".json")) {
						labelFileList.add(currentFile);
					}
				}
				zipIn.closeEntry(); // 显式关闭当前条目
			}
		} catch (IOException ex) {
			// 清理部分解压的文件
			cleanPartialFiles(fileList);
			throw new IOException("解压失败: " + ex.getMessage(), ex);
		}

		res.put(LABEL_CONFIG_FILES, Collections.unmodifiableList(labelFileList));
		res.put(FILE_LIST, Collections.unmodifiableList(fileList));
		res.put(IMAGES_STANDARD_FORMAT_PATH, hasImageStandardPath);
		res.put(LABEL_STANDARD_FORMAT_PATH, hasLabelStandardPath);
		return res;
	}

	public static void generateThumbnail(Path sourcePath, String thumbnailBasePath, String zipEntryName) throws IOException {
		// 读取原始图像
		BufferedImage originalImage = ImageIO.read(sourcePath.toFile());

		// 保持原始尺寸不变
		int width = originalImage.getWidth();
		int height = originalImage.getHeight();

		// 创建目标图像（保持长宽不变）
		BufferedImage compressedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		compressedImage.createGraphics().drawImage(originalImage, 0, 0, null);

		// 获取输出路径
		String[] split = zipEntryName.split("/");
		Path outputPath = Paths.get(thumbnailBasePath, split[split.length - 1]);

		// 使用ImageWriter设置压缩质量
		Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
		if (writers.hasNext()) {
			ImageWriter writer = writers.next();
			try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputPath.toFile())) {
				writer.setOutput(ios);
				ImageWriteParam param = writer.getDefaultWriteParam();

				// 设置压缩模式和质量
				param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				param.setCompressionQuality(0.1f); // 0.0-1.0，1为最高质量

				// 写入压缩后的图像
				writer.write(null, new IIOImage(compressedImage, null, null), param);
			}
			writer.dispose();
		} else {
			// 回退到默认方法（无质量控制）
			ImageIO.write(compressedImage, "jpg", outputPath.toFile());
		}
	}

	public static boolean isImageFile(String fileName) {
		String lowerCase = fileName.toLowerCase();
		return lowerCase.endsWith(".jpg") ||
				lowerCase.endsWith(".jpeg") ||
				lowerCase.endsWith(".png") ||
				lowerCase.endsWith(".gif") ||
				lowerCase.endsWith(".bmp");
	}

	public static void copyFile(File file, OutputStream outputStream) {
		try (InputStream inputStream = new FileInputStream(file)) {
			IOUtils.copy(inputStream, outputStream, BUFFER_SIZE);
		} catch (Throwable throwable) {
			throw new RuntimeException(throwable);
		}
	}
}
