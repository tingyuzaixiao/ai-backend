package com.train.platform.admin.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.train.platform.admin.api.entity.SysDeletedDir;
import com.train.platform.admin.service.SysDeletedDirService;
import com.train.platform.admin.mapper.SysDeletedDirMapper;
import com.train.platform.admin.service.SysFileService;
import com.train.platform.admin.service.SysPublicParamService;
import com.train.platform.common.core.constant.CommonConstants;
import com.train.platform.common.core.util.TimeId;
import com.train.platform.common.file.core.FileTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.train.platform.common.core.constant.CommonConstants.*;

/**
 * @author li
 * @description 针对表【sys_deleted_dir】的数据库操作Service实现
 * @createDate 2024-07-02 17:07:35
 */
@Service
public class SysDeletedDirServiceImpl extends ServiceImpl<SysDeletedDirMapper, SysDeletedDir>
		implements SysDeletedDirService {

	@Autowired
	private SysPublicParamService sysPublicParamService;

	@Autowired
	private SysFileService fileService;

	@Override
	public SysDeletedDir selectSysDelDirById(Long id) {
		return baseMapper.selectById(id);
	}

	@Override
	public Boolean deleteSysDelDirsByIds(Long[] ids) {
		List<Long> delDirIds = Arrays.asList(ids);
		return baseMapper.deleteBatchIds(delDirIds) > 0;
	}

	@Override
	public IPage<List<SysDeletedDir>> getSysDelDirsPage(Page page, SysDeletedDir sysDeletedDir) {
		return baseMapper.selectSysDelDirsPage(page, sysDeletedDir);
	}

	@Override
	public Boolean delHistoryDir() {
		//查询14天前未删除文件
		int delHistoryDirHours = Integer.parseInt(sysPublicParamService.getSysPublicParamKeyToValue("DEL_HISTORY_DIR_HOURS"));
		List<SysDeletedDir> sysDeletedDirs = baseMapper.selectHistoryUnDeletedDir(CommonConstants.STATUS_NOT_DEL, delHistoryDirHours);
		if (sysDeletedDirs != null && !sysDeletedDirs.isEmpty()) {
			sysDeletedDirs.forEach(item -> {
				if (item.getFileType().equals(CommonConstants.STATUS_NOT_DEL)) {
					fileService.removeBucket(item.getDirPath());
				} else {
					fileService.removeFile(item.getDirPath(), item.getFileName());
				}
				item.setStatus(CommonConstants.STATUS_DEL);
				item.setUpdateTime(LocalDateTime.now());
			});
			baseMapper.updateDeletedDirList(sysDeletedDirs);
		}
		return true;
	}

	@Override
	public Boolean immediatelyDelHistoryDir(Long[] ids) {
		List<SysDeletedDir> sysDeletedDirs = baseMapper.selectList(Wrappers.<SysDeletedDir>lambdaQuery().in(SysDeletedDir::getDirId, ids));
		if (sysDeletedDirs != null && !sysDeletedDirs.isEmpty()) {
			sysDeletedDirs.forEach(item -> {
				if (item.getFileType().equals(CommonConstants.STATUS_NOT_DEL)) {
					fileService.removeBucket(item.getDirPath());
				} else {
					fileService.removeFile(item.getDirPath(), item.getFileName());
				}
				item.setStatus(CommonConstants.STATUS_DEL);
				item.setUpdateTime(LocalDateTime.now());
			});
			baseMapper.updateDeletedDirList(sysDeletedDirs);
		}
		return true;
	}

	@Autowired
	FileTemplate fileTemplate;

	@Override
	public int saveDeleteDir(SysDeletedDir sysDeletedDir) {
		//修改目录,避免误删除
		String newPath = sysDeletedDir.getDirPath() + "-" + TimeId.getPkStrMMddHHmmss();

		String prefixPath = "";
		String separator = FileUtil.FILE_SEPARATOR;
		switch (sysDeletedDir.getFunctionType()) {
			case FILE_FUNCTION_DATASET:
				prefixPath = DATASET_FOLDER + separator;
				break;
			case FILE_FUNCTION_MODEL:
				prefixPath = MODEL_FOLDER + separator;
				break;
			case FILE_FUNCTION_PROGRAM:
				prefixPath = PROGRAM_FOLDER + separator;
				break;
			default:
		}
		fileTemplate.amendBucketName(prefixPath + sysDeletedDir.getDirPath(), newPath);
		sysDeletedDir.setDirPath(prefixPath + newPath);

		return baseMapper.insert(sysDeletedDir);
	}

	@Override
	public void saveDeletedDirs(List<SysDeletedDir> sysDeletedDirs) {
		if (sysDeletedDirs!= null &&!sysDeletedDirs.isEmpty()) {
			sysDeletedDirs.forEach(this::saveDeleteDir);
		}
	}
}




