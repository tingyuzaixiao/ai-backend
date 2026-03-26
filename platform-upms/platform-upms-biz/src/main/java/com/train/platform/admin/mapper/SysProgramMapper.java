package com.train.platform.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.train.platform.admin.api.entity.SysProgram;
import com.train.platform.admin.api.vo.ProgramVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件管理
 *
 * @author Lee
 * @date 2024-05-17 17:11:42
 */
@Mapper
public interface SysProgramMapper extends BaseMapper<SysProgram> {

	List<SysProgram> listProgramsByRoleId(Long roleId);

    IPage<List<SysProgram>> getProgramVOsWithRolePage(Page page, @Param("query")SysProgram sysProgram, List<Long> roleIds);

	SysProgram selectProgramVOById(@Param("id") Long id);

	Boolean hasRepeatName(@Param("name") String name);

	void logicDelete(@Param("id") Long id, @Param("updateTime") LocalDateTime updateTime);
}
