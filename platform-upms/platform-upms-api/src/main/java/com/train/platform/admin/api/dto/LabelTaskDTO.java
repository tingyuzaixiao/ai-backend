package com.train.platform.admin.api.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class LabelTaskDTO implements Serializable {
	/**
	 * 标注任务id
	 */
	@Schema(description="标注任务id")
	@TableId(value = "task_id", type = IdType.AUTO)
	private Long taskId;

	/**
	 * 标签组ID
	 */
	@Schema(description = "标签组ID")
	private Long groupId;

	/**
	 * 标注任务名称
	 */
	@Size(max= 255,message="编码长度不能超过255")
	@Schema(description="标注任务名称")
	@Length(max= 255,message="编码长度不能超过255")
	private String taskName;

	/**
	 * 数据集id
	 */
	@Schema(description="数据集id")
	private Long datasetId;

	/**
	 * 基础数据集id
	 */
	@Schema(description="基础数据集id")
	private Long baseDatasetId;

	/**
	 * 基础数据集名称
	 */
	@Schema(description="基础数据集名称")
	private String baseDatasetName;

	/**
	 * 标注类型
	 */
	@Schema(description="任务类型（图像分类0、物体检测1、图像分割2）")
	private String labelType;

	/**
	 * 标注类型
	 */
	@Schema(description="子任务类型物体检测1（矩形框0、直线1、圆2）")
	private String subLabelType;

	/**
	 * 标签组
	 */
	@Size(max= 255,message="编码长度不能超过255")
	@Schema(description="标签组")
	@Length(max= 255,message="编码长度不能超过255")
	private String tagIds;


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
	 * 创建人
	 */
	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "创建人")
	private String createBy;

	/**
	 * 更新人
	 */
	@Size(max= 255,message="编码长度不能超过255")
	@Schema(description="更新人")
	@Length(max= 255,message="编码长度不能超过255")
	@TableField(fill = FieldFill.UPDATE)
	private String updateBy;
}
