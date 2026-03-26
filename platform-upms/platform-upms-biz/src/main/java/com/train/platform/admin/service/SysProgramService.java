
package com.train.platform.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.train.platform.admin.api.entity.SysProgram;
import com.train.platform.admin.api.vo.ProgramVO;
import com.train.platform.common.core.util.R;

import java.util.List;

/**
 * 项目管理
 *
 * @author fxz
 * @date 2024-05-17 17:11:43
 */
public interface SysProgramService {

	Integer updateById(SysProgram sysProgram);

	SysProgram getDetails(Long id);

	List<SysProgram> findProgramByRoleId(Long roleId);

	Boolean removeProgramById(Long id);

	Boolean addRoleProgram(Long roleId, Long id);

	IPage<List<SysProgram>> getProgramVOsWithRolePage(Page page, SysProgram sysProgram, List<Long> roles);

	SysProgram getSysProgram(Long id, String programId, String name);

	SysProgram saveProgram(SysProgram sysProgram);

	Boolean checkProgramNameAvailable(String programName);
}
