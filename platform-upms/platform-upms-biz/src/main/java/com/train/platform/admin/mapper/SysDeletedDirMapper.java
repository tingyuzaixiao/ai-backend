package com.train.platform.admin.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.train.platform.admin.api.entity.SysDeletedDir;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author li
 * @description 针对表【sys_deleted_dir】的数据库操作Mapper
 * @createDate 2024-07-02 17:07:35
 * @Entity com.train.platform.admin.SysDeletedDir
 */

@Mapper
public interface SysDeletedDirMapper extends BaseMapper<SysDeletedDir> {

	IPage<List<SysDeletedDir>> selectSysDelDirsPage(Page page, @Param("query") SysDeletedDir sysDeletedDir);

	List<SysDeletedDir> selectHistoryUnDeletedDir(String status, int delHistoryDirHours);

	void updateDeletedDirList(List<SysDeletedDir> list);
}




