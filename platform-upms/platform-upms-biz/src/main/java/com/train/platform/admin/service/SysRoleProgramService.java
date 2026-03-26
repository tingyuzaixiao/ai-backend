package com.train.platform.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.train.platform.admin.api.entity.SysRoleProgram;

/**
 * <p>
 * 角色菜单表 服务类
 * </p>
 *
 * @author lee
 * @since 2024-05-20
 */
public interface SysRoleProgramService extends IService<SysRoleProgram> {

	/**
	 * 更新角色项目
	 * @param roleId 角色ID
	 * @param programIds 项目ID拼成的字符串，每个id之间根据逗号分隔
	 * @return
	 */
	Boolean saveRolePrograms(Long roleId, String programIds);

    void removeByIds(Long[] ids);

	void removeById(Long id);
}
