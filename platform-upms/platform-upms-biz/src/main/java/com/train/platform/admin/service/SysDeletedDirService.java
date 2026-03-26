package com.train.platform.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.train.platform.admin.api.entity.SysDeletedDir;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author li
 * @description 针对表【sys_deleted_dir】的数据库操作Service
 * @createDate 2024-07-02 17:07:35
 */
public interface SysDeletedDirService extends IService<SysDeletedDir> {

	SysDeletedDir selectSysDelDirById(Long id);

	Boolean deleteSysDelDirsByIds(Long[] ids);

	IPage<List<SysDeletedDir>> getSysDelDirsPage(Page page, SysDeletedDir sysDeletedDir);

	Boolean delHistoryDir();

	Boolean immediatelyDelHistoryDir(Long[] ids);

    int saveDeleteDir(SysDeletedDir sysDeletedDir);

	void saveDeletedDirs(List<SysDeletedDir> sysDeletedDirs);
}
