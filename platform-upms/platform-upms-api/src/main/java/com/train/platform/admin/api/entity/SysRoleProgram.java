package com.train.platform.admin.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 角色项目表
 * </p>
 *
 * @author lee
 * @since 2024-5-20
 */
@Data
@Schema(description = "角色项目")
@EqualsAndHashCode(callSuper = true)
public class SysRoleProgram extends Model<SysRoleProgram> {
	/**
	 * 主键
	 */
	@Schema(description = "主键")
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 角色ID
	 */
	@Schema(description = "角色id")
	private Long roleId;

	/**
	 * 菜单ID
	 */
	@Schema(description = "项目id")
	private Long programId;
}
