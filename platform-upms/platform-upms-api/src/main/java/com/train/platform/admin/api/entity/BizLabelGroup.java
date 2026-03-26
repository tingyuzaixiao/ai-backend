package com.train.platform.admin.api.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

/**
 * 标签组
 *
 * @TableName aircas_label_group
 */
@Data
@Schema(description = "标签组")
@EqualsAndHashCode(callSuper = true)
@TableName("aircas_label_group")
public class BizLabelGroup extends Model<BizLabelGroup> {

	/**
	 * 主键
	 */
	@Schema(description = "主键")
	@TableId(value = "group_id", type = IdType.AUTO)
	private Long groupId;
	/**
	 * 标签组名称
	 */
	@Size(max = 255, message = "编码长度不能超过255")
	@Schema(description = "标签组名称")
	@Length(max = 255, message = "编码长度不能超过255")
	private String groupName;
	/**
	 * 描述
	 */
	@Size(max = 255, message = "编码长度不能超过255")
	@Schema(description = "描述")
	@Length(max = 255, message = "编码长度不能超过255")
	private String description;

	/**
	 * 创建时间
	 */
	@Schema(description = "创建时间")
	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;
	/**
	 * 创建人
	 */
	@Size(max = 255, message = "编码长度不能超过255")
	@Schema(description = "创建人")
	@Length(max = 255, message = "编码长度不能超过255")
	@TableField(fill = FieldFill.INSERT)
	private String createBy;
	/**
	 * 更新时间
	 */
	@Schema(description = "更新时间")
	@TableField(fill = FieldFill.UPDATE)
	private LocalDateTime updateTime;
	/**
	 * 更新人
	 */
	@Size(max = 255, message = "编码长度不能超过255")
	@Schema(description = "更新人")
	@Length(max = 255, message = "编码长度不能超过255")
	@TableField(fill = FieldFill.UPDATE)
	private String updateBy;

	/**
	 * 逻辑删除
	 */
	@JsonIgnore
	@TableLogic
	@Schema(description = "删除标记,1:已删除,0:正常")
	@TableField(fill = FieldFill.INSERT)
	private String delFlag;

}
