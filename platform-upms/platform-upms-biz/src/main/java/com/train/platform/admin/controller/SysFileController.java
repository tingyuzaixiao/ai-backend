package com.train.platform.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.train.platform.admin.api.dto.FileDTO;
import com.train.platform.admin.api.entity.SysFile;
import com.train.platform.admin.api.vo.FileVO;
import com.train.platform.admin.service.SysFileService;
import com.train.platform.common.core.util.R;
import com.train.platform.common.log.annotation.SysLog;
import com.train.platform.common.security.annotation.Inner;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件管理
 *
 * @author Lee
 * @date 2024-06-18 17:18:42
 */
@RestController
@AllArgsConstructor
@RequestMapping("/sys-file")
@Tag(description = "sys-file", name = "文件管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class SysFileController {

	private final SysFileService sysFileService;

	/**
	 * 分页查询
	 *
	 * @param page 分页对象
	 * @param file 文件管理
	 * @return 分页对象
	 */
	@Operation(summary = "分页查询（新）", description = "分页查询（新）")
	@GetMapping("/pageNew")
	public R getSysFilePageNew(@ParameterObject Page page, @ParameterObject FileDTO file) {
		if (file.getTagId() != null || file.getTaskId() != null) {
			if (file.getBaseDatasetId() == null) {
				return R.failed("传参格式错误");
			}
		}
		return R.ok(sysFileService.getSysFilesPageNew(page, file));
	}

	/**
	 * 导出标签
	 *
	 * @param fileDTO 导出参数
	 * @return 导出文件
	 */
	@Operation(summary = "导出标签", description = "导出标签")
	@GetMapping("/exportLabel")
	@SysLog("导出标签")
	public List<FileVO> exportLabel(FileDTO fileDTO) {
		return sysFileService.exportLabel(fileDTO);
	}

	/**
	 * 通过fileName删除文件管理
	 *
	 * @param fileNames fileName 列表
	 * @return R
	 */
	@Operation(summary = "通过fileNames删除文件管理", description = "通过fileNames删除文件管理")
	@SysLog("删除文件")
	@PostMapping("/deleteByFileNames")
	@PreAuthorize("@pms.hasPermission('sys_file_del')")
	public R deleteByFileNames(@RequestBody String[] fileNames) {
		for (String fileName : fileNames) {
			sysFileService.deleteFileByFileName(fileName);
		}
		return R.ok();
	}

	@Operation(summary = "通过ids删除文件管理", description = "通过ids删除文件管理")
	@SysLog("批量删除文件")
	@DeleteMapping
	@PreAuthorize("@pms.hasPermission('sys_file_del')")
	public R removeById(@RequestBody Long[] ids) {
		for (Long id : ids) {
			sysFileService.deleteFile(id);
		}
		return R.ok();
	}

	/**
	 * 上传文件 文件名采用uuid,避免原始文件名中带"-"符号导致下载的时候解析出现异常
	 *
	 * @param file 资源
	 * @return R(/ admin / bucketName / filename)
	 */
	@Operation(summary = "通过bucketName上传文件", description = "通过bucketName上传文件")
	@PostMapping(value = "/upload")
	@SysLog("上传文件")
	public R upload(@RequestPart("file") MultipartFile file,
					@RequestParam(required = false) String params) {
		return sysFileService.uploadFile(file, params);
	}

	/**
	 * 分片上传文件
	 *
	 * @param file   资源
	 * @param params 上传参数
	 * @return R
	 */
	@PostMapping(value = "/upload/chunk")
//	@SysLog("分片上传文件")
	public R chunkFileUpload(@RequestPart("file") MultipartFile file,
							 @RequestParam Map<String, Object> params) {
		return sysFileService.chunkFileUpload(file, params);
	}

	/**
	 * 分片上传文件合并
	 *
	 * @param params 合并参数
	 * @return R
	 */
	@PostMapping(value = "/upload/chunk/merge")
	@SysLog("分片上传文件合并")
	public R chunkFileMerge(@RequestParam Map<String, Object> params) {
		return sysFileService.chunkFileMerge(params);
	}

	/**
	 * 导入标签
	 *
	 * @param file          资源
	 * @param bucketName    存储桶名称
	 * @param baseDatasetId 基础数据集ID
	 * @param taskId        任务ID
	 * @param labelType     标签类型(分类、检测、分割)
	 * @param subLabelType  子标签类型
	 * @param labelTaskType 标签任务类型(标签、参考标签)
	 * @return R
	 */
	@Operation(summary = "导入标签", description = "导入标签")
	@PostMapping(value = "/importLabels")
	@SysLog("导入标签")
	public R importLabels(@RequestPart("file") MultipartFile file,
						  @RequestParam(required = false) String bucketName,
						  @RequestParam(required = false) Long baseDatasetId,
						  @RequestParam(required = false) Long taskId,
						  @RequestParam(required = false) String labelType,
						  @RequestParam(required = false) String subLabelType,
						  @RequestParam(required = false) String labelTaskType) throws IOException {
		//TODO是否需要存标签文件
		return sysFileService.importLabels(file.getInputStream(), file.getOriginalFilename(), baseDatasetId, taskId, labelType, subLabelType);
	}

	/**
	 * 获取文件
	 *
	 * @param bucket   桶名称
	 * @param fileName 文件空间/名称
	 * @param response 响应
	 * @return
	 */
	@Operation(summary = "通过bucket、fileName获取文件管理", description = "通过bucket、fileName获取文件管理")
	@Inner(false)
	@GetMapping(value = {"/{bucket}/{fileName}",})
	public void file(@PathVariable String bucket, @PathVariable String fileName, HttpServletResponse response) {
		sysFileService.getFile(bucket, fileName, response);
	}

	/**
	 * 通过bucketName获取文件管理(多路径)
	 *
	 * @param params   参数
	 * @param response 响应
	 * @return
	 */
	@Operation(summary = "通过bucketName获取文件管理(多路径)", description = "通过bucketName获取文件管理(多路径)")
	@Inner(false)
	@GetMapping("/multiPath")
	public void multiPathFile(@RequestParam Map<String, String> params, HttpServletResponse response) {
		sysFileService.getFile(params.get("bucketName"), params.get("fileName"), response);
	}


	/**
	 * 查询文件信息
	 *
	 * @param query 查询参数
	 * @return R
	 */
	@Operation(summary = "查询文件信息", description = "查询文件信息")
	@SneakyThrows
	@GetMapping("/details")
	public R getDetails(@ParameterObject SysFile query) {
		return R.ok(sysFileService.getOne(Wrappers.query(query), false));
	}

	/**
	 * 创建bucket
	 *
	 * @param bucketName bucket名称
	 * @return R
	 */
	@Operation(summary = "创建bucket", description = "创建bucket")
	@PostMapping("/addBucket")
	@SysLog("创建bucket")
	public R addBucket(@RequestParam String bucketName) {
		sysFileService.createBucket(bucketName);
		return R.ok(bucketName + "存储桶创建成功");
	}

	/**
	 * 删除bucket
	 *
	 * @param bucketName bucket名称
	 * @return R
	 */
	@Operation(summary = "删除bucket", description = "删除bucket")
	@PostMapping("/delBucket")
	@SysLog("删除bucket")
	public R delBucket(@RequestParam String bucketName) {
		sysFileService.removeBucket(bucketName);
		return R.ok(bucketName + "存储桶删除成功");
	}

	/**
	 * 获取本地bucket下文件夹列表
	 *
	 * @param bucketName     bucket名称
	 * @param isSubDirectory 是否查询子目录
	 * @return R
	 */
	@Operation(summary = "获取本地bucket下文件夹列表", description = "获取本地bucket下目录列表")
	@Inner(false)
	@GetMapping("/dir")
	public R getRootDir(@RequestParam(required = false) String bucketName, @RequestParam(required = false) boolean isSubDirectory) {
		return R.ok(sysFileService.getRootDir(bucketName, isSubDirectory));
	}

	/**
	 * 获取本地bucket下文件夹树状列表
	 *
	 * @param bucketName bucket名称
	 * @return R
	 */
	@Operation(summary = "获取本地bucket下文件夹树状列表", description = "获取本地bucket下文件夹树状列表")
	@Inner(false)
	@GetMapping("/dirTree")
	public R getRootDirTree(@RequestParam(required = false) String bucketName) {
		return R.ok(sysFileService.getRootDirTree(bucketName));
	}

	/**
	 * 根据字符串创建文件
	 *
	 * @param bucketName bucket名称
	 * @param fileName   文件名
	 * @param content    内容
	 * @return R
	 */
	@Operation(summary = "根据字符串创建文件", description = "根据字符串创建文件")
	@Inner(value = false)
	@PostMapping("/createFile")
	@SysLog("创建文件")
	public R createFile(@RequestParam String bucketName, @RequestParam String fileName, @RequestParam String content) {
		sysFileService.createLocalFile(bucketName, fileName, content);
		return R.ok().setMsg(bucketName + "/" + fileName + "文件创建成功");
	}

	/**
	 * 异步复制本地目录文件
	 *
	 * @param sourcePath 源目录
	 * @param targetPath 目标目录
	 * @return R
	 */
	@Operation(summary = "异步复制本地目录文件", description = "异步复制本地目录文件")
	@PostMapping("/copyLocalFile")
	@SysLog("复制本地目录文件")
	public R copyLocalFile(@RequestParam String sourcePath, @RequestParam String targetPath) {
		sysFileService.copyLocalFile(sourcePath, targetPath);
		return R.ok().setMsg("拷贝成功");
	}

	/**
	 * 读取指定目录json文件
	 *
	 * @param path 目录
	 * @return R
	 */
	@Operation(summary = "读取指定目录json文件", description = "读取指定目录json文件")
	@PostMapping("/readJSONFile")
	public R readJSONFile(@RequestParam String path) {
		return R.ok(sysFileService.readJSONFile(path));
	}

	/**
	 * 分析压缩zip文件目录格式
	 *
	 * @param file 压缩文件
	 * @return R
	 */
	@Operation(summary = "分析压缩zip文件目录格式", description = "分析压缩zip文件目录格式")
	@PostMapping("diagZipFileFormat")
	public R diagZipFileFormat(@RequestPart("file") MultipartFile file) {
		return R.ok(sysFileService.diagZipFileFormat(file));
	}

	/**
	 * 下载指定目录下文件
	 *
	 * @param params   参数
	 * @param response 响应
	 */
	@Operation(summary = "下载指定目录下文件", description = "下载指定目录下文件")
	@GetMapping("/download")
	@SysLog("下载文件")
	public void download(@RequestParam Map<String, String> params, HttpServletResponse response) {
		sysFileService.download(params, response);
	}

	/**
	 * 分片下载指定目录下文件
	 *
	 * @param params   参数
	 * @param response 响应
	 */
	@Operation(summary = "分片下载指定目录下文件", description = "分片下载指定目录下文件")
	@GetMapping("/downloadChunk")
	@SysLog("分片下载文件")
	public void downloadChunk(@RequestParam Map<String, String> params, HttpServletResponse response) {
		sysFileService.downloadChunk(params, response);
	}

	/**
	 * 上传文件XFile
	 *
	 * @param file          上传文件
	 * @param platform      平台
	 * @param bucketName    存储桶名称
	 * @param baseDatasetId 数据集ID
	 * @param param         自定义参数
	 * @return R
	 */
	@Operation(summary = "上传文件XFile", description = "上传文件XFile")
	@PostMapping(value = "/uploadXFile")
	@Inner(value = false)
	public R uploadXFile(@RequestPart("file") MultipartFile file,
						 @RequestParam(required = false) String platform,
						 @RequestParam(required = false) String bucketName,
						 @RequestParam(required = false) Long baseDatasetId,
						 @RequestParam(required = false) String param) {
		return sysFileService.uploadXFile(file, platform, bucketName, baseDatasetId, param);
	}

	/**
	 * 通过bucketName上传文件
	 *
	 * @param request 请求
	 * @return R
	 */
	@Operation(summary = "通过bucketName上传文件", description = "通过bucketName上传文件")
	@PostMapping(value = "/httpUploadXFile")
	@Inner(value = false)
	public R httpUploadXFile(HttpServletRequest request) {
		return sysFileService.httpUploadXFile(request);
	}


	/**
	 * 测试上传文件并生成缩略图
	 *
	 * @param file 上传文件
	 * @return R
	 */
	@Operation(summary = "测试上传文件并生成缩略图", description = "测试上传文件并生成缩略图")
	@PostMapping("/testThumbnailFile")
	@Inner(value = false)
	public R testThumbnailFile(@RequestPart MultipartFile file) {
		return sysFileService.uploadFileAndThumbnail(file);
	}

	@GetMapping("/metadata")
	public R getMetadata(@RequestParam String bucketName) {
		File dataDir = new File(bucketName);
		Map<String, String> files = new HashMap<>();
		long totalSize = 0;

		if (!dataDir.exists()) {
			return R.failed("Bucket not found");
		}

		for (File file : Objects.requireNonNull(dataDir.listFiles())) {
			if (file.isFile()) {
				files.put(file.getName(), file.getAbsolutePath());
				totalSize += file.length();
			}
		}

		Map<String, Object> metadata = new HashMap<>();
		metadata.put("totalSize", totalSize);
		metadata.put("files", files);
		metadata.put("version", System.currentTimeMillis());

		return R.ok(metadata);
	}

	@GetMapping("/downloadBucket")
	public void downloadBucket(@RequestParam String bucketName, HttpServletResponse response) throws IOException {
		File dataDir = new File(bucketName);
		if (!dataDir.exists()) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
		response.setContentType("application/zip");
		response.setHeader("Content-Disposition", "attachment; filename=dataset.zip");

		try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
			for (File file : Objects.requireNonNull(dataDir.listFiles())) {
				if (file.isFile()) {
					zipOut.putNextEntry(new ZipEntry(file.getName()));
					Files.copy(file.toPath(), zipOut);
					zipOut.closeEntry();
				}
			}
		}
	}

	@PostMapping("/delta")
	public R getDeltaFiles(@RequestBody List<String> requestedFiles, @RequestParam String bucketName) {
		File datasetDir = new File(bucketName);
		Map<String, byte[]> deltaFiles = new HashMap<>();

		for (String fileName : requestedFiles) {
			File file = new File(datasetDir, fileName);
			if (file.exists() && file.isFile()) {
				try {
					deltaFiles.put(fileName, Files.readAllBytes(file.toPath()));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		return R.ok(deltaFiles);
	}

	/**
	 * 计算文件md5值
	 */
	@PostMapping("/md5")
	public R calculateFileMd5(@RequestPart("file") MultipartFile file) {
		try {
			byte[] fileBytes = IOUtils.toByteArray(file.getInputStream());
			String md5 = DigestUtils.md5DigestAsHex(new ByteArrayInputStream(fileBytes));
			Map<String, String> md5Files = new HashMap<>();
			md5Files.put(file.getOriginalFilename(), md5);
			return R.ok(md5Files);
		} catch (IOException e) {
			return R.failed(e.getMessage());
		}
	}
}