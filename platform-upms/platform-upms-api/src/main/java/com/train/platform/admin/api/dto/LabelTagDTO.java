package com.train.platform.admin.api.dto;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "标签")
public class LabelTagDTO implements Serializable {

	/**
	 * 标签id
	 */
	@Schema(description = "标签id")
	@TableId(value = "tag_id", type = IdType.AUTO)

	private Long tagId;

	/**
	 * 标签组id
	 */
	@Schema(description = "标签组id")
	private Long groupId;

	/**
	 * 标注类型
	 */
	@Schema(description = "标注类型")
	private String labelType;

	/**
	 * 标注任务id
	 */
	@Schema(description = "标注任务id")
	private Long taskId;

	/**
	 * 标签名称
	 */
	@Size(max = 60, message = "编码长度不能超过60")
	@Schema(description = "标签名称")
	@Length(max = 60, message = "编码长度不能超过60")
	private String tagName;
	/**
	 * 标签颜色
	 */
	@Size(max = 16, message = "编码长度不能超过16")
	@Schema(description = "标签颜色")
	@Length(max = 16, message = "编码长度不能超过16")
	private String color;

	/**
	 * 创建人
	 */
	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "创建人")
	private String createBy;

	/**
	 * 修改人
	 */
	@TableField(fill = FieldFill.UPDATE)
	@Schema(description = "修改人")
	private String updateBy;

	/**
	 * 创建时间
	 */
	@Schema(description = "创建时间")
	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	/**
	 * 修改时间
	 */
	@Schema(description = "修改时间")
	@TableField(fill = FieldFill.UPDATE)
	private LocalDateTime updateTime;


	/**
	 * 是否删除 1：已删除 0：正常
	 */
	@JsonIgnore
	@TableLogic
	@Schema(description = "删除标记,1:已删除,0:正常")
	@TableField(fill = FieldFill.INSERT)
	private String delFlag;
}
