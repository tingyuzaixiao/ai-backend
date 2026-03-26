package com.train.platform.admin.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.train.platform.admin.api.dto.FileDTO;
import com.train.platform.admin.api.entity.SysFile;
import com.train.platform.admin.api.entity.TreeNode;
import com.train.platform.admin.api.vo.FileVO;
import com.train.platform.common.core.util.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * 文件管理
 *
 * @author Luckly
 * @date 2024-06-18 17:18:42
 */
public interface SysFileService extends IService<SysFile> {

	/**
	 * 上传文件
	 *
	 * @param file
	 * @param path
	 * @param baseDatasetId
	 * @param params
	 * @return
	 */
	R uploadFile(MultipartFile file, String path, Long baseDatasetId,  Long taskId, Map<String,Object> params);

	R uploadFile(MultipartFile file, String params);

	R chunkFileUpload(MultipartFile file, Map<String, Object> params);

	R chunkFileMerge(Map<String, Object> params);

	/**
	 * 读取文件
	 *
	 * @param bucket   桶名称
	 * @param fileName 文件名称
	 * @param response 输出流
	 */
	void getFile(String bucket, String fileName, HttpServletResponse response);

	/**
	 * 删除文件
	 *
	 * @param id
	 * @return
	 */
	Boolean deleteFile(Long id);

	/**
	 * 删除文件
	 *
	 * @param fileName
	 * @return
	 */
	Boolean deleteFileByFileName(String fileName);

	Set<String> getRootDir(String bucketName, boolean isSubDirectory);

	List<TreeNode> getRootDirTree(String bucketName);

	void createBucket(String bucketName);

	void createFolder(String bucketName, String folderPath);

	Path getFilePath(String bucketName, String... paths);

	void removeFile(String bucketName, String fileName);

	void removeBucket(String bucketName);

	void createLocalFile(String bucketName, String fileName, String content);

	void copyLocalFile(String sourcePath, String targetPath);

	String readJSONFile(String path);

	JSONObject readLabelConfigFile(String path);

	@Async
	Future<Map<String,Boolean>> asyncUnCompressFiles(Object fileSource, String bucketName, Long baseDatasetId, String labelType, String subLabelType);

	int insertBatches(List<SysFile> sysFiles);

	List<SysFile> listFiles(SysFile sysFile);

	List<SysFile> listFilesByFileIds(List<Long> fileIds);

	List<SysFile> listFilesByFileIdStr(String fileIdStr);

	IPage getSysFilesPageNew(Page page, FileDTO file);

	Boolean diagZipFileFormat(MultipartFile file);

	void download(Map<String, String> params, HttpServletResponse response);

	List<SysFile> getFileList(List<Long> fileIds);

	R uploadXFile(MultipartFile file, String platform, String bucketName, Long baseDatasetId, String param);

	R httpUploadXFile(HttpServletRequest request);

	R uploadFileAndThumbnail(MultipartFile file);

	List<FileVO> exportLabel(FileDTO fileDTO);

//	R importLabels(MultipartFile file, String bucketName, Long baseDatasetId, Long taskId, String labelTaskType, String subLabelTaskType, String labelType);

	R importLabels(InputStream inputStream, String fileName, Long baseDatasetId, Long taskId, String labelTaskType, String subLabelTaskType);

	void downloadChunk(Map<String, String> params, HttpServletResponse response);
}
