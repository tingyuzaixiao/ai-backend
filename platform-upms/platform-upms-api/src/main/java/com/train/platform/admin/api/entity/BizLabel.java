package com.train.platform.admin.api.entity;


import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @TableName aircas_label
 */

@Data
@Schema(description = "标签表")
@EqualsAndHashCode(callSuper = true)
@TableName("aircas_label")
@AllArgsConstructor
@NoArgsConstructor
public class BizLabel extends Model<BizLabel> {

	/**
	 * 主键
	 */
	@Schema(description = "主键")
	@TableId(type = IdType.AUTO, value = "label_id")
	private Long labelId;

	/**
	 * 文件ID
	 */
	@Schema(description = "文件ID")
	private Long fileId;

	/**
	 * 标签文件名
	 */
	@Size(max = 64, message = "编码长度不能超过64")
	@Schema(description = "标签文件名")
	@Length(max = 64, message = "编码长度不能超过64")
	private String labelFileName;

	/**
	 * 任务ID
	 */
	@Schema(description = "任务ID")
	private Long taskId;

	/**
	 * 标签ID
	 */
	@Schema(description = "标签IDs")
	private String tagId;

	/**
	 * 标签名
	 */
//	@Size(max = 255, message = "编码长度不能超过255")
//	@Schema(description = "标签名")
//	@Length(max = 1000, message = "编码长度不能超过255")
//	private String tagInfo;

	@Schema(description = "标签状态")
	private String state;

	/**
	 * 标签备注（描述）
	 */
	@Schema(description = "标签备注")
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
	@TableLogic
	@JsonIgnore
	@Schema(description = "删除标记,1:已删除,0:正常")
	@TableField(fill = FieldFill.INSERT)
	private String delFlag;

	public BizLabel(Long taskId, String tagId, String labelFileName) {
		this.taskId = taskId;
		this.tagId = tagId;
		this.labelFileName = labelFileName;
	}
}
