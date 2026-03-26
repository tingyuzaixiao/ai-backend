package com.train.platform.common.file.local;

import cn.hutool.core.io.FileUtil;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.train.platform.common.file.core.FileProperties;
import com.train.platform.common.file.core.FileTemplate;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 本地文件读取模式
 *
 * @author lee
 * @date 2024/4/19
 */
@Slf4j
@RequiredArgsConstructor
public class LocalFileTemplate implements FileTemplate {

	private final FileProperties properties;

	/**
	 * 创建bucket
	 *
	 * @param bucketName bucket名称
	 */
	@Override
	public void createBucket(String bucketName) {
		Path fullPath = Paths.get(properties.getLocal().getBasePath(), bucketName);
		if (Files.exists(fullPath)) {
			if (!Files.isDirectory(fullPath)) {
				throw new RuntimeException(String.format("createBucket failed! full path:%s is a file", fullPath));
			}
		} else {
			try {
				Files.createDirectories(fullPath);
				log.info("bucketName: {} create success, full path: {}", bucketName, fullPath);
			} catch (Throwable throwable) {
				throw new RuntimeException(String.format("bucketName: %s create failed, full path: %s",
						bucketName, fullPath), throwable);
			}
		}
	}

	@Override
	public void createFolder(String bucketName, String folderPath) {
		Path fullPath = Paths.get(properties.getLocal().getBasePath(), bucketName, folderPath);
		if (Files.exists(fullPath)) {
			if (!Files.isDirectory(fullPath)) {
				throw new RuntimeException(String.format("createFolder failed! path:%s is a file", fullPath));
			}
		} else {
			try {
				Files.createDirectories(fullPath);
				log.info("bucketName: {} folderPath: {} create success, full path: {}", bucketName, folderPath, fullPath);
			} catch (Throwable throwable) {
				throw new RuntimeException(String.format("bucketName: %s folderPath: %s create failed, full path: %s",
						bucketName, folderPath, fullPath), throwable);
			}
		}
	}

	@Override
	public Path getFilePath(String bucketName, String... paths) {
		Path basePath = Paths.get(properties.getLocal().getBasePath());
		Path resolvedPath = basePath.resolve(bucketName);
		for (String path : paths) {
			resolvedPath = resolvedPath.resolve(path);
		}
		return resolvedPath;
	}

	/**
	 * 获取全部bucket
	 * <p>
	 * <p>
	 * API Documentation</a>
	 */
	@Override
	public List<Bucket> getAllBuckets() {
		return Arrays.stream(FileUtil.ls(properties.getLocal().getBasePath()))
				.filter(FileUtil::isDirectory)
				.map(dir -> new Bucket(dir.getName()))
				.collect(Collectors.toList());
	}

	/**
	 * @param bucketName bucket名称
	 * @see <a href= Documentation</a>
	 */
	@Override
	public void removeBucket(String bucketName) {
		FileUtil.del(properties.getLocal().getBasePath() + FileUtil.FILE_SEPARATOR + bucketName);
	}

	/**
	 * 上传文件
	 *
	 * @param bucketName  bucket名称
	 * @param objectName  文件名称
	 * @param stream      文件流
	 * @param contextType 文件类型
	 */
	@Override
	public void putObject(String bucketName, String objectName, InputStream stream, String contextType) {
		// 当 Bucket 不存在时创建
		String dir = properties.getLocal().getBasePath() + FileUtil.FILE_SEPARATOR + bucketName;
		if (!FileUtil.isDirectory(properties.getLocal().getBasePath() + FileUtil.FILE_SEPARATOR + bucketName)) {
			createBucket(bucketName);
		}

		// 写入文件
		File file = FileUtil.file(dir + FileUtil.FILE_SEPARATOR + objectName);


		byte[] fileBytes = null; // 使用Apache Commons I/O
		try {
			fileBytes = IOUtils.toByteArray(stream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		try (InputStream stream1 = new ByteArrayInputStream(fileBytes);
			 InputStream stream2 = new ByteArrayInputStream(fileBytes)) {
			//流存在bug
			FileUtil.writeFromStream(stream1, file);
			String thumbnailPath = properties.getLocal().getBasePath() + FileUtil.FILE_SEPARATOR + "thumbnails" + FileUtil.FILE_SEPARATOR + bucketName;
			// 创建目录
			FileUtil.mkdir(thumbnailPath);
			resizeFromStream(stream2, thumbnailPath + FileUtil.FILE_SEPARATOR + objectName);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}


	}

	/**
	 * 获取文件
	 *
	 * @param bucketName bucket名称
	 * @param objectName 文件名称
	 * @return 二进制流 API Documentation</a>
	 */
	@Override
	@SneakyThrows
	public S3Object getObject(String bucketName, String objectName) {
		String dir = properties.getLocal().getBasePath() + FileUtil.FILE_SEPARATOR + bucketName;
		S3Object s3Object = new S3Object();
		s3Object.setObjectContent(FileUtil.getInputStream(dir + FileUtil.FILE_SEPARATOR + objectName));
		return s3Object;
	}

	/**
	 * @param bucketName
	 * @param objectName
	 * @throws Exception
	 */
	@Override
	public void removeObject(String bucketName, String objectName) throws Exception {
		String dir = properties.getLocal().getBasePath() + FileUtil.FILE_SEPARATOR + bucketName;
		FileUtil.del(dir + FileUtil.FILE_SEPARATOR + objectName);
	}

	/**
	 * 上传文件
	 *
	 * @param bucketName bucket名称
	 * @param objectName 文件名称
	 * @param stream     文件流
	 * @throws Exception
	 */
	@Override
	public void putObject(String bucketName, String objectName, InputStream stream) throws Exception {
		putObject(bucketName, objectName, stream, null);
	}

	/**
	 * 根据文件前置查询文件
	 *
	 * @param bucketName bucket名称
	 * @param prefix     前缀
	 * @param recursive  是否递归查询
	 * @return S3ObjectSummary 列表
	 * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/s3-2006-03-01/ListObjects">AWS
	 * API Documentation</a>
	 */
	@Override
	public List<S3ObjectSummary> getAllObjectsByPrefix(String bucketName, String prefix, boolean recursive) {
		String dir = properties.getLocal().getBasePath() + FileUtil.FILE_SEPARATOR + bucketName;

		return Arrays.stream(FileUtil.ls(dir)).filter(file -> file.getName().startsWith(prefix)).map(file -> {
			S3ObjectSummary summary = new S3ObjectSummary();
			summary.setKey(file.getName());
			return summary;
		}).collect(Collectors.toList());
	}

	@Override
	public boolean exist(String bucketName, String objectName) {
		String path = properties.getLocal().getBasePath() + FileUtil.FILE_SEPARATOR + bucketName + FileUtil.FILE_SEPARATOR + objectName;
		return Files.exists(Path.of(path));
	}

	@Override
	public void amendBucketName(String srcDir, String newBucketName) {
		File file = new File(properties.getLocal().getBasePath() + FileUtil.FILE_SEPARATOR + srcDir);
		file.setWritable(true);  // 设置可写权限
		file.setReadable(true);  // 设置可读权限
		FileUtil.rename(file, newBucketName, true);
	}

	public void resizeFromStream(InputStream is, String outputPath) {
		try {
			BufferedImage originalImage = ImageIO.read(is);
			int scaledWidth = originalImage.getWidth();
			int scaledHeight = originalImage.getHeight();

			BufferedImage thumbnail = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = thumbnail.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
			g.dispose();

			ImageIO.write(thumbnail, "jpg", new File(outputPath));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}
}
