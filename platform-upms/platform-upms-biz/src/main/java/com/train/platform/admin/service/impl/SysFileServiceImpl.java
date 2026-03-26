package com.train.platform.admin.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.amazonaws.services.s3.model.S3Object;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.train.platform.admin.api.dto.FileDTO;
import com.train.platform.admin.api.entity.*;
import com.train.platform.admin.api.vo.FileVO;
import com.train.platform.admin.api.vo.LabelVO;
import com.train.platform.admin.mapper.*;
import com.train.platform.admin.service.*;
import com.train.platform.common.core.exception.CheckedException;
import com.train.platform.common.core.util.FileUtils;
import com.train.platform.common.core.util.R;
import com.train.platform.common.core.util.StringUtils;
import com.train.platform.common.core.util.TimeId;
import com.train.platform.common.file.core.FileProperties;
import com.train.platform.common.file.core.FileTemplate;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageProperties;
import org.dromara.x.file.storage.core.FileStorageService;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.train.platform.common.core.constant.CommonConstants.*;
import static com.train.platform.common.core.constant.PlatformConstants.*;

/**
 * 文件管理
 *
 * @author Luckly
 * @date 2024-06-18 17:18:42
 */
@Slf4j
@Service
@AllArgsConstructor
public class SysFileServiceImpl extends ServiceImpl<SysFileMapper, SysFile> implements SysFileService {

	private final FileTemplate fileTemplate;

	private final FileProperties properties;

	private final SqlSessionTemplate sqlSessionTemplate;

	private final BizLabelService bizLabelService;

	private final BizLabelTaskService bizLabelTaskService;

	private final BizLabelTaskMapper bizLabelTaskMapper;

	private final BizLabelTagMapper bizLabelTagMapper;

	private final BizBaseDatasetMapper bizBaseDatasetMapper;

	private final BizLabelGroupMapper bizLabelGroupMapper;

	@Resource
	private final FileStorageService fileStorageService;
	@Autowired
	private BizLabelTagInfoService bizLabelTagInfoService;


	/**
	 * 上传文件
	 *
	 * @param file          文件
	 * @param baseDatasetId 基础数据集ID
	 * @param params        参数
	 * @return 返回值
	 */
	@Override
	public R uploadFile(MultipartFile file, String bucketName, Long baseDatasetId, Long taskId, Map<String, Object> params) {
		return upload(file, bucketName, baseDatasetId, taskId, params);
	}

	@Override
	public R uploadFile(MultipartFile file, String paramsStr) {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> params = new HashMap<>();
		try {
			params = mapper.readValue(paramsStr, Map.class);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		Long baseDatasetId = params.getOrDefault(BASE_DATASET_ID, null) != null ? Long.parseLong(params.get(BASE_DATASET_ID).toString()) : null;
		String bucketName = params.getOrDefault(BUCKET_NAME, "").toString();
		Long taskId = params.getOrDefault(TASK_ID, null) != null ? Long.parseLong(params.get(TASK_ID).toString()) : null;
		return uploadFile(file, bucketName, baseDatasetId, taskId, params);
	}

	@Override
	public R chunkFileUpload(MultipartFile file, Map<String, Object> params) {
		String bucketName = params.getOrDefault(BUCKET_NAME, "").toString();
		String fileMd5 = params.getOrDefault(FILE_MD5, "").toString();
		int chunkIndex = Integer.parseInt(params.getOrDefault(CHUNK_INDEX, "").toString());

		try {
			String filePath = String.format("%s/%s/%s/", properties.getLocal().getBasePath(), bucketName, fileMd5);
			File dir = new File(filePath);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			String chunkPath = filePath + chunkIndex;
			file.transferTo(new File(chunkPath));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return R.ok("分片上传成功");
	}

	@Override
	public R chunkFileMerge(Map<String, Object> params) {
		String bucketName = params.getOrDefault(BUCKET_NAME, "").toString();
		String fileName = params.getOrDefault(FILE_NAME, "").toString();
		String fileMd5 = params.getOrDefault(FILE_MD5, "").toString();
		int totalChunks = Integer.parseInt(params.getOrDefault(TOTAL_CHUNKS, "").toString());

		Long baseDatasetId = !Objects.equals(params.getOrDefault(BASE_DATASET_ID, "").toString(), "") ? Long.parseLong(params.getOrDefault(BASE_DATASET_ID, "").toString()) : null;
		Long taskId = !Objects.equals(params.getOrDefault(TASK_ID, "").toString(), "") ? Long.parseLong(params.getOrDefault(TASK_ID, "").toString()) : null;

		String chunkDir = String.format("%s/%s/%s/", properties.getLocal().getBasePath(), bucketName, fileMd5);
		String destPath = String.format("%s/%s/%s", properties.getLocal().getBasePath(), bucketName, fileName);

		// 1. 创建目标文件的File对象
		File destFile = new File(destPath);

		try {
			// 2. 确保父目录存在
			File parentDir = destFile.getParentFile();
			if (!parentDir.exists()) {
				boolean mkdirs = parentDir.mkdirs();
				if (!mkdirs) {
					throw new IOException("创建父目录失败: " + parentDir.getAbsolutePath());
				}
			}

			// 3. 使用try-with-resources确保资源关闭
			try (FileOutputStream fos = new FileOutputStream(destFile)) {
				for (int i = 0; i < totalChunks; i++) {
					String chunkPath = chunkDir + i;
					File chunkFile = new File(chunkPath);

					// 4. 增加分块文件存在性检查
					if (!chunkFile.exists()) {
						throw new FileNotFoundException("分块文件不存在: " + chunkPath);
					}

					// 5. 使用Files.copy合并文件内容
					Files.copy(chunkFile.toPath(), fos);
				}
			}

			// 6. 合并完成后生成File对象并验证
			if (!destFile.exists() || destFile.length() == 0) {
				throw new IOException("文件合并失败: 目标文件未正确生成");
			}

			// 7. 延迟删除分块：确保合并成功后再清理
			for (int i = 0; i < totalChunks; i++) {
				new File(chunkDir + i).delete();
			}
			new File(chunkDir).delete();

			if (baseDatasetId != null) {
				return upload(destFile, bucketName, baseDatasetId, taskId, params);
			} else {
				// 8. 返回包含文件信息的响应
				return R.ok("合并成功");
			}

		} catch (IOException e) {
			// 9. 异常处理时保留分块文件以便调试
			throw new RuntimeException("文件合并失败: " + e.getMessage(), e);
		}
	}

	@Override
	public R importLabels(InputStream inputStream, String fileName, Long baseDatasetId, Long taskId, String labelTaskType, String subLabelTaskType) {
		try (inputStream) {
			JSONObject labelDataMap = JSONObject.parseObject(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));

			Map<String, Object> analysisLabelJsonData = analysisLabelJsonData(labelTaskType, subLabelTaskType, taskId, fileName, labelDataMap);
			Map<String, Object> labelData = (Map<String, Object>) analysisLabelJsonData.get(ANNOTATIONS);
			Map<String, Long> labelTagMap = (Map<String, Long>) analysisLabelJsonData.getOrDefault(LABEL_TAG, null);

			//解析标签文件并插入BizLabel表中
			List<SysFile> fileList = baseMapper.selectList(Wrappers.<SysFile>lambdaQuery().
					eq(SysFile::getBaseDatasetId, baseDatasetId));
			List<LabelVO> bizLabelList = new ArrayList<>();
			List<BizLabelTagInfo> bizLabelTagInfos = new ArrayList<>();

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("path", fileName);
			jsonObject.put(TASK_ID, taskId);
			AtomicInteger labelCount = new AtomicInteger();

			fileList.forEach(item -> {
				String separator = FileUtil.FILE_SEPARATOR;
				String[] split = item.getBucketName().split(separator);
				String key = separator + split[split.length - 1] + separator + item.getOriginal();
				if (labelData.containsKey(key)) {
					LabelVO labelVO = (LabelVO) labelData.get(key);
					labelVO.setFileId(item.getId());
					bizLabelList.add(labelVO);
					List<BizLabelTagInfo> tagInfoList = labelVO.getTagInfoList();
					if (tagInfoList != null && !tagInfoList.isEmpty()) {
						bizLabelTagInfos.addAll(tagInfoList);
					}
					labelCount.getAndIncrement();
				}
			});

			if (!fileList.isEmpty()) {
				jsonObject.put("progress", (labelCount.get() / fileList.size()) * 100);
			}

			List<Object> jsonArray = new ArrayList<>();

			// 更新基础数据集label_json
			BizBaseDataset bizBaseDataset = bizBaseDatasetMapper.selectById(baseDatasetId);
			String labelJson = bizBaseDataset.getLabelJson();
			if (labelJson != null && !labelJson.isEmpty()) {
				jsonArray = JSONObject.parseArray(labelJson);
			}
			jsonArray.add(jsonObject);
			bizBaseDataset.setLabelJson(JSONArray.toJSONString(jsonArray));
			bizBaseDatasetMapper.updateById(bizBaseDataset);

			BizLabelTask bizLabelTask = new BizLabelTask();
			bizLabelTask.setTaskId(taskId);
			bizLabelTask.setTagIds(String.join(",", labelTagMap.values().toString()));
			bizLabelTaskMapper.updateById(bizLabelTask);

			bizLabelService.insertLabelVOBatches(bizLabelList);
			bizLabelService.insertTagInfoBatches(bizLabelTagInfos);
		} catch (Exception e) {
			log.error("上传失败", e);
			return R.failed(e.getLocalizedMessage());
		}
		return R.ok();
	}

	/**
	 * 读取文件
	 *
	 * @param bucket
	 * @param fileName
	 * @param response
	 */
	@Override
	public void getFile(String bucket, String fileName, HttpServletResponse response) {
		try (S3Object s3Object = fileTemplate.getObject(bucket, fileName)) {
			response.setContentType("application/octet-stream; charset=UTF-8");
			response.setHeader("Access-Control-Allow-Methods", "*");
			IoUtil.copy(s3Object.getObjectContent(), response.getOutputStream());
		} catch (Exception e) {
			log.error("文件读取异常: {}", e.getLocalizedMessage());
		}
	}

	/**
	 * 删除文件
	 *
	 * @param id
	 * @return
	 */
	@Override
	@SneakyThrows
	@Transactional(rollbackFor = Exception.class)
	public Boolean deleteFile(Long id) {
		SysFile file = this.getById(id);
		if (Objects.isNull(file)) {
			return Boolean.FALSE;
		}
		fileTemplate.removeObject(properties.getBucketName(), file.getFileName());
		return this.removeById(id);
	}

	@Override
	@SneakyThrows
	@Transactional(rollbackFor = Exception.class)
	public Boolean deleteFileByFileName(String fileName) {
		LambdaQueryWrapper<SysFile> wrapper = Wrappers.<SysFile>lambdaQuery().eq(SysFile::getFileName, fileName);
		SysFile file = this.getOne(wrapper);
		if (Objects.isNull(file)) {
			return Boolean.FALSE;
		}
		fileTemplate.removeObject(properties.getBucketName(), file.getFileName());
		return this.remove(wrapper);
	}

	@Override
	public Set<String> getRootDir(String bucketName, boolean isSubDirectory) {
		Set<String> res = new HashSet<>();
		String basePath = properties.getLocal().getBasePath() + (bucketName != null ? "/" + bucketName : "");
		try {
			walk(new File(basePath), isSubDirectory, res);
		} catch (Exception e) {
			log.error("获取根目录失败", e);
			throw new RuntimeException("获取根目录失败或该目录下暂无目录");
		}

		res = res.stream().map(item -> item.replace(basePath + "/", "")).collect(Collectors.toSet());
		return res;
	}

	@Override
	public List<TreeNode> getRootDirTree(String bucketName) {
		List<TreeNode> res = new ArrayList<>();
		String basePath = properties.getLocal().getBasePath() + (bucketName != null ? "/" + bucketName : "");
		Path rootDir = Paths.get(basePath);
		try {
			res = buildTree(rootDir, properties.getLocal().getBasePath());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return res;
	}

	public static List<TreeNode> buildTree(Path currentDir, String rootDirPath) throws IOException {
		List<TreeNode> nodes = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentDir)) {
			for (Path path : stream) {
				if (Files.isDirectory(path)) { // 只处理目录，跳过文件
					String fullPath = path.toString();
					String relativePath = fullPath.replace(rootDirPath, "").substring(1); // 裁掉根目录路径
					String label = path.getFileName().toString();
					TreeNode node = new TreeNode(relativePath, label, null);

					// 递归构建子目录
					List<TreeNode> children = buildTree(path, rootDirPath);
					if (!children.isEmpty()) { // 如果有子目录，才添加到当前节点
						node.setChildren(children);
					}
					nodes.add(node);
				}
			}
		}
		return nodes;
	}

	@Override
	public void createBucket(String bucketName) {
		fileTemplate.createBucket(bucketName);
	}

	@Override
	public void createFolder(String bucketName, String folderPath) {
		fileTemplate.createFolder(bucketName, folderPath);
	}

	@Override
	public Path getFilePath(String bucketName, String... paths) {
		return fileTemplate.getFilePath(bucketName, paths);
	}

	@Override
	public void removeBucket(String bucketName) {
		fileTemplate.removeBucket(bucketName);
	}

	@SneakyThrows
	@Override
	public void removeFile(String bucketName, String fileName) {
		fileTemplate.removeObject(bucketName, fileName);
	}

	@Override
	@SneakyThrows
	public void createLocalFile(String bucketName, String fileName, String content) {
		String path = properties.getLocal().getBasePath() + FileUtil.FILE_SEPARATOR + bucketName + FileUtil.FILE_SEPARATOR;
		fileTemplate.createBucket(bucketName);
		File file = FileUtil.file(path + fileName);
		content = StringEscapeUtils.unescapeJava(content);
		Writer write = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
		write.write(content);
		write.flush();
		write.close();
	}

	@SneakyThrows
	@Override
	@Async
	public void copyLocalFile(String sourcePath, String targetPath) {
		String basePath = properties.getLocal().getBasePath() + FileUtil.FILE_SEPARATOR;
		if (sourcePath.split("\\.").length >= 2) {
			FileUtils.copyFile(basePath + sourcePath, basePath + targetPath);
		} else {
			FileUtils.copyDir(basePath + sourcePath, basePath + targetPath);
		}
	}

	@Override
	public String readJSONFile(String path) {
		String basePath = properties.getLocal().getBasePath() + FileUtil.FILE_SEPARATOR;
		String content = FileUtils.readFile(basePath + path);
		content = StringEscapeUtils.unescapeJava(content);
		JSONObject jsonObject = JSONObject.parseObject(content);
		return jsonObject == null ? null : jsonObject.toString();
	}

	@Override
	public JSONObject readLabelConfigFile(String path) {
		String content = FileUtils.readFile(path);
		content = StringEscapeUtils.unescapeJava(content);
		return JSONObject.parseObject(content);
	}

	public void walk(File dir, boolean isSubDirectory, Set<String> res) {
		for (File file : Objects.requireNonNull(dir.listFiles())) {
			if (file.isDirectory()) {
				res.add(file.getPath());
				if (isSubDirectory) {
					walk(file, true, res);
				}
			}
		}
	}

	/**
	 * 文件管理数据记录,收集管理追踪文件
	 *
	 * @param file     上传文件格式
	 * @param fileName 文件名
	 */

	@SneakyThrows
	private long fileLog(MultipartFile file, String bucketName, String fileName, Long baseDatasetId) {
		SysFile sysFile = new SysFile();

		try (InputStream imageInputStream = file.getInputStream()) {
			BufferedImage bufferedImage = ImageIO.read(imageInputStream);
			sysFile.setHeight(bufferedImage.getHeight());
			sysFile.setWidth(bufferedImage.getWidth());
		}

		sysFile.setBaseDatasetId(baseDatasetId);
		sysFile.setFileName(fileName);
		String name = file.getResource().getFilename();
		sysFile.setOriginal(name.split("/")[name.split("/").length - 1]);
		sysFile.setFileSize(file.getSize());
		sysFile.setType(FileUtil.extName(file.getOriginalFilename()));
		sysFile.setBucketName(bucketName);
		sysFile.setMd5(DigestUtils.md5DigestAsHex(file.getInputStream()));
		this.save(sysFile);
		return sysFile.getId();
	}

	@Async("asyncPoolTaskExecutor")
	@Override
	public Future<Map<String, Boolean>> asyncUnCompressFiles(Object fileSource, String bucketName, Long baseDatasetId, String labelType, String subLabelType) {
		// 统一文件源处理
		final String originalFilename;
		final InputStream inputStream;
		final File sourceFile;

		if (fileSource instanceof MultipartFile multipartFile) {
			originalFilename = multipartFile.getOriginalFilename();
			try {
				inputStream = multipartFile.getInputStream();
				sourceFile = null; // 非文件类型
			} catch (IOException e) {
				throw new RuntimeException("获取MultipartFile输入流失败", e);
			}
		} else if (fileSource instanceof File file) {
			originalFilename = file.getName();
			try {
				inputStream = new FileInputStream(file);
				sourceFile = file; // 保留文件引用
			} catch (FileNotFoundException e) {
				throw new RuntimeException("文件未找到: " + file.getAbsolutePath(), e);
			}
		} else {
			throw new IllegalArgumentException("不支持的文件类型: " + fileSource.getClass().getName());
		}

		String basePath = properties.getLocal().getBasePath() + FileUtil.FILE_SEPARATOR;
		File targetDir = new File(basePath + bucketName);
		List<File> files = new ArrayList<>();
		Map<String, Object> fileList = new HashMap<>(3);
		List<File> labelConfigFiles = new ArrayList<>();
		boolean imagesStandardFormatPath = false;

		try {
			String zipExtName = FileUtil.extName(originalFilename);
			if (ZIP.equals(zipExtName)) {
				// 使用优化后的大文件处理方法
				fileList = FileUtils.unzipLargeFile(inputStream, targetDir);
				files = (List<File>) fileList.get(FILE_LIST);
			} else if (TAR.equals(zipExtName)) {
				// 使用文件源直接处理（避免重复流转换）
				if (sourceFile != null) {
					fileList = FileUtils.unTarFile(sourceFile, targetDir.getAbsolutePath());
				} else {
					fileList = FileUtils.unTarInputStream((MultipartFile) fileSource, targetDir.getAbsolutePath());
				}
				files = (List<File>) fileList.get(FILE_LIST);
			} else if (TAR_GZ.equals(zipExtName)) {
				// 处理gzip压缩文件
				File unGzipped = null;
				if (sourceFile != null) {
					unGzipped = FileUtils.unGzip(sourceFile, targetDir);
					fileList = FileUtils.unTarFile(unGzipped, targetDir.getAbsolutePath());
					files = (List<File>) fileList.get(FILE_LIST);
				}
			} else {
				files = new ArrayList<>();
			}

			imagesStandardFormatPath = (boolean) fileList.get(IMAGES_STANDARD_FORMAT_PATH);
			labelConfigFiles = (List<File>) fileList.get(LABEL_CONFIG_FILES);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(inputStream); // 确保流关闭
		}

		Map<String, Boolean> res = new HashMap<>(2);
		res.put(IMAGES_STANDARD_FORMAT_PATH, imagesStandardFormatPath);
//		res.put("labelStandardFormatPath", labelStandardFormatPath);

		if (!imagesStandardFormatPath || files.isEmpty()) {
			return new AsyncResult<>(res);
		}

		Map<Long, Map<String, LabelVO>> annotationsMap = new HashMap<>();
		List<LabelVO> labelVOS = new ArrayList<>();

		List<BizLabelTask> bizLabelTasks = new ArrayList<>();

		if (labelConfigFiles != null) {
			for (File labelConfigFile : labelConfigFiles) {
				Long taskId = IdUtil.getSnowflake(19, 19).nextId();

				Map<String, Object> annotationsByJsonFile = getAnnotationsByJsonFile(taskId, labelConfigFile, labelType, subLabelType);
				if (annotationsByJsonFile == null) {
					continue;
				}

				BizLabelTask bizLabelTask = new BizLabelTask();
				bizLabelTask.setBaseDatasetId(baseDatasetId);
				bizLabelTask.setTaskName(TimeId.getNextPkStr() + "-" + labelConfigFile.getName().split("\\.")[0]);
				bizLabelTask.setLabelType(labelType);
				bizLabelTask.setTaskId(taskId);

				Map<String, LabelVO> annotationMap = (Map<String, LabelVO>) annotationsByJsonFile.getOrDefault(ANNOTATIONS, null);
				Map<String, Long> labelTagMap = (Map<String, Long>) annotationsByJsonFile.getOrDefault(LABEL_TAG, null);

				//根据tag_id,baseDatasetId,创建label_task
				List<Long> labelTagIds = new ArrayList<>(labelTagMap.values());
				bizLabelTask.setTagIds(labelTagIds.toString());
				bizLabelTasks.add(bizLabelTask);
				annotationsMap.put(taskId, annotationMap);
			}
			bizLabelTaskService.saveBatch(bizLabelTasks);
		}

		long fileSize = 0L;
		int fileCount = 0, labelCount = 0;
		List<SysFile> sysFiles = new ArrayList<>();
		List<BizLabelTagInfo> bizLabelTagInfos = new ArrayList<>();

		for (File file : files) {
			String extName = FileUtil.extName(file.getName());
			if (IMG_TYPE.contains(extName)) {
				StringBuilder filePathStr = new StringBuilder(file.getPath());
				SysFile sysFile = new SysFile();

				try {
					BufferedImage bufferedImage = ImageIO.read(new FileInputStream(file));
					sysFile.setWidth(bufferedImage.getWidth());
					sysFile.setHeight(bufferedImage.getHeight());
					sysFile.setMd5(DigestUtils.md5DigestAsHex(new FileInputStream(file)));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				sysFile.setImportType(COMPRESS_PACKAGE_TYPE);
				sysFile.setId(IdUtil.getSnowflake(19, 19).nextId());
				sysFile.setBaseDatasetId(baseDatasetId);
				sysFile.setFileName(file.getName());
				sysFile.setOriginal(file.getName());
				sysFile.setFileSize(file.length());
				sysFile.setType(extName);
				StringBuilder bucket = filePathStr.delete(0, basePath.length()).delete(filePathStr.length() - file.getName().length() - 1, filePathStr.length());
				sysFile.setBucketName(bucket.toString());

				if (!bizLabelTasks.isEmpty()) {
					for (BizLabelTask bizLabelTask : bizLabelTasks) {
						Map<String, LabelVO> annotationMap = annotationsMap.get(bizLabelTask.getTaskId());
						String imgPath = StringUtils.truncateAfter(filePathStr.toString(), IMG_FOLDER) + FileUtil.FILE_SEPARATOR + file.getName();
						if (labelType.equals(CLASSIFY_MODE) || labelType.equals(DETECTION_MODE)) {
							LabelVO labelVO = annotationMap.getOrDefault(imgPath, null);

							if (labelVO != null) {
								labelVO.setFileId(sysFile.getId());
								labelCount++;
								labelVOS.add(labelVO);
								bizLabelTagInfos.addAll(labelVO.getTagInfoList());
							}
						}

						//TODO 分割待完成
					}
				}

				sysFiles.add(sysFile);
				fileSize += file.length();
				fileCount++;
			}
		}

		// 批量导入
		this.insertBatches(sysFiles);

		BizBaseDataset bizBaseDataset = bizBaseDatasetMapper.selectById(baseDatasetId);
		if (!bizLabelTasks.isEmpty()) {
			bizLabelService.insertTagInfoBatches(bizLabelTagInfos);
			bizLabelService.insertLabelVOBatches(labelVOS);
			for (BizLabelTask bizLabelTask : bizLabelTasks) {
				// 更新基础数据集label_json
				JSONObject labelJsonObject = new JSONObject();
				String labelJson = bizBaseDataset.getLabelJson();

				//labelJsonObject.put("name", labelConfigFile.getName().split("\\.")[0]);
				labelJsonObject.put(TASK_ID, bizLabelTask.getTaskId());
				//labelJsonObject.put("path", labelPathSb.delete(0, basePath.length()));
				labelJsonObject.put("progress", (labelCount / fileCount) * 100);

				List<Object> labelJsonArray = new JSONArray();
				if (labelJson != null && !labelJson.isEmpty()) {
					labelJsonArray = JSONObject.parseArray(labelJson);
					labelJsonArray.add(labelJsonObject);
				} else {
					labelJsonArray = new ArrayList<>();
					labelJsonArray.add(labelJsonObject);
				}
				bizBaseDataset.setLabelJson(JSONArray.toJSONString(labelJsonArray));
			}
		}

		bizBaseDataset.setCount(bizBaseDataset.getCount() + fileCount);
		bizBaseDataset.setSize(bizBaseDataset.getSize() + fileSize);
		bizBaseDatasetMapper.updateById(bizBaseDataset);
		return new AsyncResult<>(res);
	}

	public Map<String, Object> getAnnotationsByJsonFile(Long taskId, File labelConfigFile, String labelType, String subLabelType) {
		Map<String, Object> dataListMap = new HashMap<>();
		//解析JSON标签
		JSONObject labelJSON = readLabelConfigFile(labelConfigFile.getPath());
		if (labelJSON != null) {
			dataListMap = analysisLabelJsonData(labelType, subLabelType, taskId, labelConfigFile.getName(), labelJSON);
		}
		return dataListMap;
	}

	public Map<String, Object> analysisLabelJsonData(String labelType, String subLabelType, Long taskId, String fileName, JSONObject labelJSON) {
		Map<String, Object> resMap = new HashMap<>();
		Map<String, Object> annotationsListMap = new HashMap<>();
		Map<String, Long> labelTagMap = new HashMap<>();
		JSONObject metainfo = labelJSON.getJSONObject(METAINFO);
		JSONObject classesIndices = metainfo.getJSONObject(CLASSES_INDICES);
		JSONArray dataList = labelJSON.getJSONArray(DATA_LIST);
		Map<String, Integer> classesIndicesMap = classesIndices.toJavaObject(Map.class);
		Map<Integer, String> swapClassesIndicesMap = new HashMap<>();

		if (metainfo.get(CLASSES_INDICES) == null) {
			throw new RuntimeException("标签文件格式错误");
		}
		if (taskId == null) {
			//新建标签组
			BizLabelGroup bizLabelGroup = new BizLabelGroup();
			bizLabelGroup.setGroupName(TimeId.getNextPkStr() + "-" + fileName.split("\\.")[0]);
			bizLabelGroup.setDescription("文件导入");
			bizLabelGroupMapper.insert(bizLabelGroup);

			classesIndicesMap.forEach((k, v) -> {
				swapClassesIndicesMap.put(v, k);
				BizLabelTag labelTag = new BizLabelTag();
				labelTag.setGroupId(bizLabelGroup.getGroupId());
				labelTag.setTagName(k);
				labelTag.setColor(FileUtils.generateRandomHexColor());
				bizLabelTagMapper.insert(labelTag);
				labelTagMap.put(k, labelTag.getTagId());
			});
		} else {
			//新建标签组
			classesIndicesMap = classesIndicesMap.entrySet().stream()
					.sorted(Map.Entry.comparingByValue()) // 按 value 升序
					.collect(Collectors.toMap(
							Map.Entry::getKey,
							Map.Entry::getValue,
							(oldVal, newVal) -> oldVal, // 合并冲突规则
							LinkedHashMap::new // 保持顺序
					));
			classesIndicesMap.forEach((k, v) -> {
				swapClassesIndicesMap.put(v, k);
				BizLabelTag labelTag = new BizLabelTag();
				labelTag.setTaskId(taskId);
				labelTag.setTagName(k);
				labelTag.setColor(FileUtils.generateRandomHexColor());
				bizLabelTagMapper.insert(labelTag);
				labelTagMap.put(k, labelTag.getTagId());
			});
		}

		if (labelType.equals(CLASSIFY_MODE)) {
			dataList.forEach(item -> {
				JSONObject data = (JSONObject) item;

				LabelVO label = new LabelVO();
				label.setLabelId(IdUtil.getSnowflake(19, 19).nextId());
				label.setTaskId(taskId);
				try {
					label.setTagId(String.valueOf(labelTagMap.get(swapClassesIndicesMap.get(data.get(IMG_LABEL)))));
				} catch (Exception e) {
					//抛异常，上传标签时类型选择错误
					throw new RuntimeException("数据集上传时标签类型选择错误");
				}

				BizLabelTagInfo bizLabelTagInfo = new BizLabelTagInfo();
				bizLabelTagInfo.setLabelId(label.getLabelId());
				bizLabelTagInfo.setTagId(Long.valueOf(label.getTagId()));
				bizLabelTagInfo.setTagInfo(data.get(IMG_LABEL).toString());

				label.setTagInfoList(Collections.singletonList(bizLabelTagInfo));
				annotationsListMap.put(data.get(IMG_PATH).toString(), label);
			});
		}

		if (labelType.equals(DETECTION_MODE)) {
			String labelStr = switch (subLabelType) {
				case DETECTION_LINE_MODE -> LINE_LABEL;
				case DETECTION_CIRCLE_MODE -> CIRCLE_LABEL;
				default -> BBOX_LABEL;
			};

			dataList.forEach(item -> {
				JSONObject data = (JSONObject) item;
				JSONArray instances = data.getJSONArray(INSTANCES);
				if (instances != null && !instances.isEmpty()) {
					LabelVO bizLabel = new LabelVO();
					List<BizLabelTagInfo> labelTagInfoVOS = new ArrayList<>();
					bizLabel.setTaskId(taskId);
					Set<Long> tagIds = new HashSet<>();
					bizLabel.setLabelFileName(fileName.split("\\.")[0]);
					bizLabel.setLabelId(IdUtil.getSnowflake(19, 19).nextId());

					instances.forEach(label -> {
						JSONObject instance = (JSONObject) label;
						int coordinate = Integer.parseInt(instance.get(labelStr).toString());
						String className = swapClassesIndicesMap.get(coordinate);
						Long tagId = labelTagMap.get(className);
						instance.put(labelStr, tagId);
						tagIds.add(tagId);
						labelTagInfoVOS.add(new BizLabelTagInfo(bizLabel.getLabelId(), tagId, instance.toJSONString()));
					});
					bizLabel.setTagInfoList(labelTagInfoVOS);
					bizLabel.setTagId(StringUtils.join(tagIds, ","));
					annotationsListMap.put(data.get(IMG_PATH).toString(), bizLabel);
				}
			});
		}

		if (labelType.equals(SEGMENTATION_MODE)) {
			//todo 待完成
		}

		resMap.put(ANNOTATIONS, annotationsListMap);
		resMap.put(LABEL_TAG, labelTagMap);
		return resMap;
	}

	@Async("batchTaskExecutor")
	public CompletableFuture<Integer> insertBatchesAsyncFuture(List<SysFile> sysFiles) {
		try {
			int count = insertBatches(sysFiles); // 调用同步方法
			return CompletableFuture.completedFuture(count);
		} catch (Exception e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	@Async("batchTaskExecutor")
	public void insertBatchesAsync(List<SysFile> sysFiles) {
		// 分片处理（避免大事务）
		List<List<SysFile>> batches = Lists.partition(sysFiles, 400);

		batches.forEach(batch -> {
			try (SqlSession session = sqlSessionTemplate.getSqlSessionFactory()
					.openSession(ExecutorType.BATCH, false)) {

				SysFileMapper mapper = session.getMapper(SysFileMapper.class);
				batch.forEach(mapper::insert);

				session.commit(); // 整批提交
			} catch (Exception e) {
				throw new RuntimeException("批次插入失败", e);
			}
		});
	}


	@Override
	public int insertBatches(List<SysFile> sysFiles) {
		// 空集合直接返回
		if (sysFiles == null || sysFiles.isEmpty()) {
			return 0;
		}

		try (SqlSession session = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false)) {
			SysFileMapper mapper = session.getMapper(SysFileMapper.class);
			for (int i = 0; i < sysFiles.size(); i++) {
				mapper.insert(sysFiles.get(i));

				// 优化提交点：每400条或最后一条提交
				if ((i + 1) % 400 == 0 || i == sysFiles.size() - 1) {
					session.commit();
					session.clearCache();  // 清除缓存释放内存
				}
			}
			return sysFiles.size();  // 返回实际插入数量
		} catch (Exception e) {
			// 使用日志框架记录异常（例如SLF4J）
			throw new CheckedException("批量插入失败", e);  // 抛出自定义异常
		}
	}

	@Override
	public List<SysFile> listFiles(SysFile sysFile) {
		return baseMapper.selectList(Wrappers.query(sysFile));
	}

	@Override
	public List<SysFile> listFilesByFileIds(List<Long> fileIds) {
		return baseMapper.selectList(Wrappers.query(new SysFile()).in("id", fileIds));
	}

	@Override
	public List<SysFile> listFilesByFileIdStr(String fileIdStr) {
		return baseMapper.selectListInFileIdStr(fileIdStr);
	}

	@Override
	public IPage<FileVO> getSysFilesPageNew(Page page, FileDTO file) {
		if (file.getIsLabeled() != null && file.getIsLabeled()) {
			IPage<FileVO> pageFileVOS = baseMapper.selectSysFilesLabeledIdsPage0721(page, file);
			List<FileVO> fileVOS = pageFileVOS.getRecords();
			if (fileVOS == null) {
				return pageFileVOS;
			}

			List<Long> fileIds = fileVOS.stream().map(FileVO::getFileId).collect(Collectors.toList());
			if (!fileIds.isEmpty()) {
				pageFileVOS.setRecords(baseMapper.selectNewSysFilesPage0512(file, fileIds));
			}

			return pageFileVOS;
		} else if (file.getIsLabeled() != null) {
			return baseMapper.selectNewSysFilesUnLabeledPage0512(page, file);
		} else {
			if (file.getBaseDatasetId() != null) {
				Page<FileVO> pages = new Page<>();
				IPage<Long> fileOrderIds = baseMapper.selectFileOrderIds(page, file);
				List<FileVO> fileVOList = new ArrayList<>();
				if (!fileOrderIds.getRecords().isEmpty()) {
					fileVOList = baseMapper.selectNewSysFilesPage0512(file, fileOrderIds.getRecords());
					pages.setRecords(fileVOList);
				}
				pages.setCurrent(page.getCurrent());
				pages.setSize(page.getSize());
				if (fileVOList.size() < page.getSize() && page.getCurrent() == 1) {
					pages.setTotal(Math.min(fileOrderIds.getTotal(), fileVOList.size()));
				} else {
					pages.setTotal(fileOrderIds.getTotal());
				}

				return pages;
			} else {
				return baseMapper.selectSysFilesPage(page, file);
			}
		}
	}

	@Override
	public Boolean diagZipFileFormat(MultipartFile file) {
		return FileUtils.diagStandardFormatPath(file);
	}

	@Override
	public void download(Map<String, String> params, HttpServletResponse response) {
		String separator = FileUtil.FILE_SEPARATOR;
		String sourcePath = properties.getLocal().getBasePath() + separator;
		if (params.getOrDefault("exportType", "").equals("baseDataset")) {
			sourcePath += DATASET_FOLDER + separator + params.get(FILE_NAME);
		}

		if (params.getOrDefault("exportType", "").equals("mask")) {
			Map<String, String> map = new HashMap<>() {{
				put("type", "dataset");
				put(BASE_DATASET_NAME, params.get(BASE_DATASET_NAME));
			}};
			sourcePath += FileUtils.getPath(map) + separator + IMG_FOLDER + separator + params.get(FILE_NAME) + separator + MASK_FOLDER;
		}

		FileUtils.downloadZip(sourcePath, params.get(FILE_NAME) + params.get("fileExt"), response);
	}

	@Override
	public void downloadChunk(Map<String, String> params, HttpServletResponse response) {
		Integer chunkIndex = Integer.parseInt(params.getOrDefault(CHUNK_INDEX, null));
		Integer chunkTotal = Integer.parseInt(params.getOrDefault(CHUNK_TOTAL, null));
		Integer chunkSize = Integer.parseInt(params.getOrDefault(CHUNK_SIZE, null));
		String fileName = params.get(FILE_NAME);
		String bucketName = params.get("bucketName");

		String sourcePath = properties.getLocal().getBasePath() + FileUtil.FILE_SEPARATOR;
		if (params.getOrDefault("exportType", "").equals("baseDataset")) {
			sourcePath += DATASET_FOLDER + FileUtil.FILE_SEPARATOR + params.get(FILE_NAME);
		}

		FileUtils.downloadChunk(sourcePath + FileUtil.FILE_SEPARATOR + bucketName, fileName, chunkIndex, chunkTotal, chunkSize, response);
	}

	@Override
	public List<SysFile> getFileList(List<Long> fileIds) {
		return baseMapper.selectList(Wrappers.<SysFile>lambdaQuery().in(SysFile::getId, fileIds));
	}

	@Override
	public R uploadXFile(MultipartFile file, String platform, String bucketName, Long baseDatasetId, String param) {
//		File thumbnail = FileUtils.getThumbnail(file, file.getOriginalFilename());

//		UploadPretreatment thumbnail = fileStorageService.of(file)
//				.setThumbnailSuffix(".th.jpg") //指定缩略图后缀，必须是 thumbnailator 支持的图片格式，默认使用全局的
//				.thumbnail(th -> th.size(1000, 1000));
//		byte[] thumbnailBytes = thumbnail.getThumbnailBytes();

		long startTime = System.currentTimeMillis();
		FileInfo upload = fileStorageService
				.of(file)
				.setPath(bucketName + "/")
				.setPlatform(platform)
				.setThumbnailSuffix(".th.jpg") //指定缩略图后缀，必须是 thumbnailator 支持的图片格式，默认使用全局的
				.thumbnail(th -> th.size(1000, 1000))  //再生成一张 200*200 的缩略图
				.upload();
		long endTime = System.currentTimeMillis();
		System.out.println("Execution time: " + (endTime - startTime) / 1000 + " seconds");
		return R.ok(upload);
	}

	@Override
	public R httpUploadXFile(HttpServletRequest request) {
		FileStorageProperties fileStorageProperties = fileStorageService.getProperties();
		fileStorageProperties.setDefaultPlatform("minio-1");

		FileInfo upload = fileStorageService
				.of(request)
				.setPath("dataset")
//				.setThumbnailSuffix(".th.jpg") //指定缩略图后缀，必须是 thumbnailator 支持的图片格式，默认使用全局的
//				.thumbnail(th -> th.size(1000, 1000))
				.upload();

		return R.ok(upload);
	}

	@Override
	public R uploadFileAndThumbnail(MultipartFile file) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			BufferedImage originalImage = ImageIO.read(file.getInputStream());
			BufferedImage thumbnail = new BufferedImage(200, 300, BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics2D = thumbnail.createGraphics();
			graphics2D.drawImage(originalImage.getScaledInstance(200, 300, Image.SCALE_SMOOTH), 0, 0, null);
			graphics2D.dispose();
			ImageIO.write(thumbnail, "jpg", outputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}

		FileInfo upload = fileStorageService.of(outputStream.toByteArray()).setPath("dataset/").upload();
		return R.ok(upload);
	}

	@Override
	public List<FileVO> exportLabel(FileDTO fileDTO) {
		return baseMapper.selectFileLabelList(fileDTO);
	}

	public R upload(Object fileSource, String bucketName, Long baseDatasetId, Long taskId, Map<String, Object> params) {
		// 统一文件源处理
		final String originalFilename;
		final InputStream inputStream;
		final long fileSize;
		final String contentType;

		if (fileSource instanceof MultipartFile multipartFile) {
			originalFilename = multipartFile.getOriginalFilename();
			try {
				inputStream = multipartFile.getInputStream();
				fileSize = multipartFile.getSize();
				contentType = multipartFile.getContentType();
			} catch (IOException e) {
				return R.failed("获取文件流失败: " + e.getMessage());
			}
		} else if (fileSource instanceof File file) {
			originalFilename = file.getName();
			try {
				inputStream = new FileInputStream(file);
				fileSize = file.length();
				contentType = Files.probeContentType(file.toPath());
			} catch (IOException e) {
				return R.failed("读取文件失败: " + e.getMessage());
			}
		} else {
			return R.failed("不支持的文件类型: " +
					(fileSource != null ? fileSource.getClass().getName() : "null"));
		}

		Map<String, String> resultMap = new HashMap<>(4);
		bucketName = bucketName == null || bucketName.isEmpty() ? DEFAULT_FOLDER : bucketName;
		resultMap.put(BUCKET_NAME, bucketName);

		String uploadType = params.getOrDefault(UPLOAD_TYPE, null) != null ? params.get(UPLOAD_TYPE).toString() : null;
		String labelType = params.getOrDefault(LABELTYPE, null) != null ? params.get(LABELTYPE).toString() : null;
		String dataType = params.getOrDefault(DATA_TYPE, null) != null ? params.get(DATA_TYPE).toString() : null;
		String subLabelType = params.getOrDefault(SUB_LABEL_TYPE, null) != null ? params.get(SUB_LABEL_TYPE).toString() : null;

		try { // 使用try-with-resources确保流关闭
			String extName = FileUtil.extName(originalFilename);
			if (extName != null && ZIP_TYPE.contains(extName) && labelType != null) {
				// 压缩文件处理
				bizBaseDatasetMapper.updateState(baseDatasetId, IMPORT_PARSING);
				Future<Map<String, Boolean>> future = asyncUnCompressFiles(fileSource, bucketName, baseDatasetId, labelType, subLabelType);
				Map<String, Boolean> zipResult = future.get();

				if (!zipResult.get(IMAGES_STANDARD_FORMAT_PATH)) {
					bizBaseDatasetMapper.updateState(baseDatasetId, IMPORT_FAILED);
					return R.failed("图像目录格式错误");
				}
				bizBaseDatasetMapper.updateState(baseDatasetId, IMPORTED);
			} else {
				if (dataType != null) {
					return switch (dataType) {
						case IMAGE_TYPE -> handleImageUpload(inputStream, bucketName, baseDatasetId, taskId,
								uploadType, labelType, subLabelType, originalFilename, contentType, fileSize, resultMap);
						case TEXT_TYPE, AUDIO_TYPE, VIDEO_TYPE, POINT_CLOUD_TYPE, SLICING_TYPE ->
								handleGeneralUpload(inputStream, bucketName, originalFilename, contentType, resultMap);
						default -> R.failed("数据类型错误");
					};
				} else {
					// 通用无指定上传类型
					return handleGeneralUpload(inputStream, bucketName, originalFilename, contentType, resultMap);
				}
			}
		} catch (Exception e) {
			log.error("上传失败", e);
			return R.failed(e.getLocalizedMessage());
		}
		return R.ok(resultMap);
	}

	/**
	 * 处理图像类型文件上传
	 */
	private R handleImageUpload(InputStream inputStream, String bucketName, Long baseDatasetId, Long taskId,
								String uploadType, String labelType, String subLabelType, String originalFileName, String contentType,
								long fileSize, Map<String, String> resultMap) {
		if (uploadType != null && !uploadType.isEmpty()) {
			// 数据集图像上传
			if (baseDatasetId != null) {
				return switch (uploadType) {
					case UPLOAD_IMAGE ->
							handleDatasetImageUpload(inputStream, bucketName, baseDatasetId, originalFileName, contentType, fileSize, resultMap);
					case UPLOAD_BASE_LABEL, UPLOAD_LABEL ->
							handleImageLabelUpload(inputStream, bucketName, baseDatasetId, taskId, labelType, subLabelType, originalFileName);
					case UPLOAD_MASK_IMAGE ->
							handleMaskImageUpload(inputStream, bucketName, contentType, resultMap, originalFileName);
					default -> R.failed("上传类型错误");
				};
			} else {
				// 普通图像上传
				return handleGeneralImageUpload(inputStream, bucketName, originalFileName, contentType, resultMap);
			}
		} else {
			// 无指定上传类型
			return handleGeneralImageUpload(inputStream, bucketName, originalFileName, contentType, resultMap);
		}
	}

	/**
	 * 处理数据集图像上传
	 */
	private R handleDatasetImageUpload(InputStream inputStream, String bucketName, Long baseDatasetId,
									   String originalFilename, String contentType, long fileSize, Map<String, String> resultMap) {
		try {
			// 非数据集或其他压缩文件处理
			String fileName = IdUtil.simpleUUID() + StrUtil.DOT + FileUtil.extName(originalFilename);

			// 更新数据集状态为导入中
			bizBaseDatasetMapper.updateState(baseDatasetId, IMPORT_UPLOADING);

			// 缓存文件内容到字节数组
			byte[] fileBytes = IOUtils.toByteArray(inputStream); // 使用Apache Commons I/O

			// 使用缓存的字节数组创建新流
			try (InputStream stream1 = new ByteArrayInputStream(fileBytes);
				 InputStream stream2 = new ByteArrayInputStream(fileBytes)) {

				// 文件日志使用第一个流
				long fileId = fileLog(stream1, originalFilename, fileName, bucketName, baseDatasetId, fileSize);

				// 文件存储使用第二个流
				fileTemplate.putObject(bucketName, fileName, stream2, contentType);

				resultMap.put("fileId", String.valueOf(fileId));
				resultMap.put(FILE_NAME, fileName);
				resultMap.put("url", String.format("/admin/sys-file/%s/%s", bucketName, fileName));

				// 更新数据集信息
				UpdateWrapper<BizBaseDataset> updateWrapper = new UpdateWrapper<>();
				updateWrapper.eq("base_dataset_id", baseDatasetId)
						.setSql("`count`=`count` + 1")
						.setSql("`size` = `size` + " + fileSize)
						.set("state", IMPORTED);
				bizBaseDatasetMapper.update(updateWrapper);
			}
			return R.ok(resultMap);
		} catch (Exception e) {
			bizBaseDatasetMapper.updateState(baseDatasetId, IMPORT_FAILED);
			return R.failed("图像上传失败: " + e.getMessage());
		}
	}

	/**
	 * 处理标签文件上传
	 */
	private R handleImageLabelUpload(InputStream inputStream, String bucketName, Long baseDatasetId, Long taskId,
								String labelType, String subLabelType, String originalFileName) {
		try {
			// 创建或获取标注任务
			Long finalTaskId = createOrGetLabelTask(baseDatasetId, taskId, labelType, subLabelType, originalFileName);

			// 导入标签
			return importLabels(inputStream, bucketName, baseDatasetId, finalTaskId, labelType, subLabelType);
		} catch (Exception e) {
			return R.failed("标签处理失败: " + e.getMessage());
		}
	}

	/**
	 * 处理掩码图像上传
	 */
	private R handleMaskImageUpload(InputStream inputStream, String bucketName,
									String contentType, Map<String, String> resultMap, String originalFileName) {
		try {
			// 使用原始文件名，存储文件
			fileTemplate.putObject(bucketName, originalFileName, inputStream, contentType);

			resultMap.put(FILE_NAME, originalFileName);
			resultMap.put("url", String.format("/admin/sys-file/%s/%s", bucketName, originalFileName));
			return R.ok(resultMap);
		} catch (Exception e) {
			return R.failed("掩码图像上传失败: " + e.getMessage());
		}
	}

	/**
	 * 通用文件上传处理
	 */
	private R handleGeneralUpload(InputStream inputStream, String bucketName,
								  String originalFileName, String contentType, Map<String, String> resultMap) {
		try {
			String fileName = IdUtil.simpleUUID() + StrUtil.DOT + FileUtil.extName(originalFileName);
			fileTemplate.putObject(bucketName, fileName, inputStream, contentType);
			resultMap.put(FILE_NAME, fileName);
			resultMap.put("url", String.format("/admin/sys-file/%s/%s", bucketName, fileName));
			return R.ok(resultMap);
		} catch (Exception e) {
			return R.failed("文件上传失败: " + e.getMessage());
		}
	}

	/**
	 * 通用图像上传处理
	 */
	private R handleGeneralImageUpload(InputStream inputStream, String bucketName,
									   String originalFilename, String contentType, Map<String, String> resultMap) {
		try {
			// 非数据集或其他压缩文件处理
			String fileName = IdUtil.simpleUUID() + StrUtil.DOT + FileUtil.extName(originalFilename);
			fileTemplate.putObject(bucketName, fileName, inputStream, contentType);
			resultMap.put(FILE_NAME, fileName);
			resultMap.put("url", String.format("/admin/sys-file/%s/%s", bucketName, fileName));
			return R.ok(resultMap);
		} catch (Exception e) {
			return R.failed("图像上传失败: " + e.getMessage());
		}
	}

	/**
	 * 创建或获取标注任务
	 */
	private Long createOrGetLabelTask(Long baseDatasetId, Long taskId,
									  String labelType, String subLabelType, String originalFilename) {
		if (taskId != null) {
			return taskId;
		}

		// 新建标注任务
		BizLabelTask bizLabelTask = new BizLabelTask();

		// 处理任务名称
		String taskName = TimeId.getNextPkStr() + "-" +
				(originalFilename != null ?
						originalFilename.substring(0, originalFilename.lastIndexOf(".")) :
						"unnamed_task");

		bizLabelTask.setBaseDatasetId(baseDatasetId);
		bizLabelTask.setTaskName(taskName);
		bizLabelTask.setLabelType(labelType);
		bizLabelTask.setSubLabelType(subLabelType);
		bizLabelTask.setDataType(IMAGE_TYPE);
		bizLabelTaskService.save(bizLabelTask);

		return bizLabelTask.getTaskId();
	}

	private long fileLog(InputStream inputStream, String originalFileName, String bucketName, Long baseDatasetId, long fileSize) {
		return fileLog(inputStream, originalFileName, originalFileName, bucketName, baseDatasetId, fileSize);
	}

	/**
	 * 文件日志记录（适配两种文件类型）
	 */
	private long fileLog(InputStream inputStream, String originalFileName, String fileName, String bucketName, Long baseDatasetId, long fileSize) {
		SysFile sysFile = new SysFile();
		long fileId = IdUtil.getSnowflake(19, 19).nextId();

		byte[] fileBytes = null; // 使用Apache Commons I/O
		try {
			fileBytes = IOUtils.toByteArray(inputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		try (InputStream stream1 = new ByteArrayInputStream(fileBytes);
			 InputStream stream2 = new ByteArrayInputStream(fileBytes)) {
			//流存在bug
			sysFile.setMd5(DigestUtils.md5DigestAsHex(stream1));
			BufferedImage bufferedImage = ImageIO.read(stream2);
			sysFile.setHeight(bufferedImage.getHeight());
			sysFile.setWidth(bufferedImage.getWidth());

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		sysFile.setId(fileId);
		sysFile.setBaseDatasetId(baseDatasetId);
		sysFile.setFileName(fileName);
		sysFile.setOriginal(originalFileName);
		sysFile.setFileSize(fileSize);
		sysFile.setType(FileUtil.extName(fileName));
		sysFile.setBucketName(bucketName);

		// 保存文件记录
		this.save(sysFile);
		return sysFile.getId();
	}
}
