
package com.train.platform.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.train.platform.admin.api.entity.SysRoleProgram;
import com.train.platform.admin.mapper.SysRoleProgramMapper;
import com.train.platform.admin.service.SysRoleProgramService;
import com.train.platform.common.core.constant.CacheConstants;
import lombok.AllArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 角色项目表 服务实现类
 * </p>
 *
 * @author lee
 * @since 2024-05-20
 */
@Service
@AllArgsConstructor
public class SysRoleProgramServiceImpl extends ServiceImpl<SysRoleProgramMapper, SysRoleProgram>
		implements SysRoleProgramService {

	private final CacheManager cacheManager;

	/**
	 * @param roleId 角色
	 * @param programIds 项目ID拼成的字符串，每个id之间根据逗号分隔
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(value = CacheConstants.PROGRAM_DETAILS, key = "#roleId")
	public Boolean saveRolePrograms(Long roleId, String programIds) {
		this.remove(Wrappers.<SysRoleProgram>query().lambda().eq(SysRoleProgram::getRoleId, roleId));

		if (StrUtil.isBlank(programIds)) {
			return Boolean.TRUE;
		}
		List<SysRoleProgram> roleProgramList = Arrays.stream(programIds.split(StrUtil.COMMA)).map(menuId -> {
			SysRoleProgram roleProgram = new SysRoleProgram();
			roleProgram.setRoleId(roleId);
			roleProgram.setProgramId(Long.valueOf(menuId));
			return roleProgram;
		}).collect(Collectors.toList());

		// 清空userinfo
		cacheManager.getCache(CacheConstants.USER_DETAILS).clear();
		this.saveBatch(roleProgramList);
		return Boolean.TRUE;
	}

	@Override
	public void removeByIds(Long[] ids) {
		baseMapper.deleteByIds(ids);
	}

	@Override
	public void removeById(Long id) {
		baseMapper.deleteById(id);
	}
}
