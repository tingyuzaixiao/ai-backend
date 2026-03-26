package com.train.platform.admin.api.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.train.platform.admin.api.entity.BizLabelTagInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class LabelVO implements Serializable {

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

	/**
	 * 标签名
	 */
//	@Size(max = 255, message = "编码长度不能超过255")
	@Schema(description = "描述（备注）")
//	@Length(max = 1000, message = "编码长度不能超过255")
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
	 * 标签信息list
	 */
	@Schema(description = "标签信息list")
	private List<BizLabelTagInfo> tagInfoList;
}
