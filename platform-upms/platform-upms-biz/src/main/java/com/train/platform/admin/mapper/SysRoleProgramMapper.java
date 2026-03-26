package com.train.platform.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.train.platform.admin.api.entity.SysRoleProgram;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 项目菜单表 Mapper 接口
 * </p>
 *
 * @author lee
 * @since 2024-05-20
 */
@Mapper
public interface SysRoleProgramMapper extends BaseMapper<SysRoleProgram> {

    void deleteByIds(Long[] ids);

	void deleteById(Long id);
}
