package com.train.platform.admin.api.entity;


import java.io.Serial;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @TableName aircas_experiment
 */
@Data
@Schema(description = "实验")
@EqualsAndHashCode(callSuper = true)
@TableName("aircas_experiment")
public class BizExperiment extends Model<BizExperiment> {
	@Serial
	private static final long serialVersionUID = 3505749643692180020L;
	/**
	 * 主键
	 */
	@Schema(description = "主键")
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 实验id
	 */
	@Schema(description = "实验id")
	@Size(max = 64, message = "编码长度不能超过64")
	private String experimentId;

	/**
	 * 实验名称
	 */
	@NotBlank(message = "实验名称不能为空")
	@Size(max = 64, message = "编码长度不能超过64(唯一约束)")
	@Schema(description = "实验名称")
	private String experimentName;

	/**
	 * 项目ID
	 */
	@NotBlank(message = "项目集ID不能为空")
	@Size(max = 64, message = "编码长度不能超过64")
	@Schema(description = "项目ID")
	private String programId;

	/**
	 * 实验描述
	 */
	@NotBlank(message = "实验描述不能为空")
	@Size(max = 255, message = "编码长度不能超过255")
	@Schema(description = "实验描述")
	private String description;

	/**
	* 训练算法
	*/
	@Size(max = 64, message = "编码长度不能超过64")
	@Schema(description = "训练算法")
	private String algorithm;


	/**
	 * python的requirements
	 */
	@Size(max = 1024, message = "编码长度不能超过1024")
	@Schema(description = "python的requirements")
	private String pyRequirements;

	/**
	 * 模型文件
	 */
	@Size(max = 255, message = "编码长度不能超过255")
	@Schema(description = "模型文件")
	private String modelFile;

	/**
	 * 权重文件
	 */
	@Schema(description = "权重文件")
	@Size(max = 255, message = "编码长度不能超过255")
	private String weightsFile;

	/**
	 * 引用权重
	 */
	@Schema(description = "引用权重")
	@Size(max = 64, message = "编码长度不能超过64")
	private String refWeights;

	/**
	 * 数据集ID
	 */
	@NotNull(message = "数据集ID不能为空")
	@Schema(description = "数据集ID")
	private Long datasetId;

	/**
	 * 数据标注ID
	 */
	@NotNull(message = "数据标注ID不能为空")
	@Schema(description = "数据标注ID")
	private Long taskId;

	/**
	 * 超参数
	 */
	@NotBlank(message = "超参数不能为空")
	@Size(max = 2048, message = "编码长度不能超过2048")
	@Schema(description = "超参数")
	private String hyperParameters;

	/**
	 * 数据增强参数
	 */
	@Size(max = 255, message = "编码长度不能超过255")
	@Schema(description = "数据增强参数")
	private String augmentationParams;

	/**
	 * 指标
	 */
//	@NotBlank(message = "指标不能为空")
	@Size(max = 1024, message = "编码长度不能超过255")
	@Schema(description = "指标")
	private String metrics;

	/**
	 * 实验状态 {@link com.train.platform.common.core.constant.enums.ExperimentStatus}
	 */
	@Schema(description = "实验状态")
	@Size(max = 16, message = "编码长度不能超过16")
	private String status;

	/**
	 * 实验状态 {@link com.train.platform.common.core.constant.enums.ExperimentStopType}
	 */
	@Schema(description = "停止类型")
	private Integer stopType;

	/**
	 * 开始时间
	 */
	@Schema(description = "开始时间")
	private LocalDateTime startTime;

	/**
	 * 实验运行时长
	 */
	@Schema(description = "实验运行时长")
	private Long duration;

	/**
	 * 输出模型
	 */
	@Schema(description = "输出模型")
	@Size(max = 64, message = "编码长度不能超过64")
	private String outputModel;

	/**
	 * mlflow是否删除
	 */
	@Schema(description = "删除标记,1:已删除,0:正常")
	private Integer mlflowDel;

	/**
	 * 进度
	 */
	@Schema(description = "进度")
	private Double progress;

	/**
	 * 资源
	 */
	@Schema(description = "运行资源")
	private Long resourceId;

	/**
	 * 创建人
	 */
	@TableField(fill = FieldFill.INSERT)
	@Size(max = 64, message = "编码长度不能超过64")
	@Schema(description = "创建人")
	private String createBy;

	/**
	 * 修改人
	 */
	@TableField(fill = FieldFill.UPDATE)
	@Size(max = 64, message = "编码长度不能超过64")
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
	@TableLogic
	@Schema(description = "删除标记,1:已删除,0:正常")
	@TableField(fill = FieldFill.INSERT)
	private String delFlag;
}
