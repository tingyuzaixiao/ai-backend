package com.train.platform.admin.api.vo;

import com.baomidou.mybatisplus.annotation.*;
import com.train.platform.admin.api.entity.BizLabelTag;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class LabelGroupVO implements Serializable {
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

	@Schema(description = "标签列表")
	private List<BizLabelTag> tagList;
}
