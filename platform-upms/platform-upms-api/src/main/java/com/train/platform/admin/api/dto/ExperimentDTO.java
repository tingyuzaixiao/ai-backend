package com.train.platform.admin.api.dto;

import java.time.LocalDateTime;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;


@Data
@Schema(description = "实验")
public class ExperimentDTO  {
	/**
	 * 实验名称
	 */
	@NotBlank(message = "实验名称不能为空")
	@Schema(description = "实验名称")
	private String experimentName;

	/**
	 * 模型文件
	 */
	@NotBlank(message = "模型文件不能为空")
	@Schema(description = "模型文件")
	private String modelFile;

	/**
	 * 数据集ID
	 */
	@NotNull(message = "数据集ID不能为空")
	@Schema(description = "数据集ID")
	private Long datasetId;

	/**
	 * 项目ID
	 */
	@NotBlank(message = "项目集ID不能为空")
	@Schema(description = "项目ID")
	private String programId;

	/**
	 * 权重文件
	 */
	@Schema(description = "权重文件")
	private String weightsFile;

	/**
	 * 引用权重
	 */
	@Schema(description = "引用权重")
	private String refWeights;

	/**
	 * 起始选择checkpointPath
	 */
	@Schema(description = "起始选择checkpointPath")
	private String beginCheckpointPath;

	/**
	 * 暂停时最后checkpointPath
	 */
	@Schema(description = "暂停时checkpoint路径")
	private String pauseCheckpointPath;

	/**
	 * 指标json
	 */
	@Size(max = 255, message = "编码长度不能超过255")
	@Schema(description = "指标json")
	@Length(max = 255, message = "编码长度不能超过255")
	private String metrics;

	/**
	 * 进度
	 */
	@Schema(description = "进度")
	private Double progress;

	/**
	 * 开始时间
	 */
	@Schema(description = "开始时间")
	private LocalDateTime startTime;

	/**
	 * 结束时间
	 */
	@Schema(description = "结束时间")
	private LocalDateTime endTime;

	/**
	 * 实验标识
	 */
	@Schema(description = "实验标识(0开始训练、1暂停训练、2重新开始、3暂停后开始、4完成)")
	private String experimentTag;

	/**
	 * 实验描述
	 */
	@Size(max = 255, message = "编码长度不能超过255")
	@Schema(description = "实验描述")
	@Length(max = 255, message = "编码长度不能超过255")
	private String description;

	/**
	 * 实验时间（训练、校验）
	 */
	@Schema(description = "实验时间（训练、校验）")
	private Long duration;

	/**
	 * 标签描述（校验）
	 */
	@Size(max = 255, message = "编码长度不能超过255")
	@Schema(description = "标签描述（校验）")
	@Length(max = 255, message = "编码长度不能超过255")
	private String labelDesc;

	/**
	 * 模型预测的可能类别极其对应置信度列表（校验）
	 */
	@Size(max = 255, message = "编码长度不能超过255")
	@Schema(description = "模型预测的可能类别极其对应置信度列表（校验）")
	@Length(max = 255, message = "编码长度不能超过255")
	private String topPredictions;

	/**
	 * 训练算法（训练）
	 */
	@Size(max = 64, message = "编码长度不能超过64")
	@Schema(description = "训练算法（训练）")
	@Length(max = 64, message = "编码长度不能超过64")
	private String algorithms;

	/**
	 * 超参数（训练）
	 */
	@Size(max = 255, message = "编码长度不能超过255")
	@Schema(description = "超参数（训练）")
	@Length(max = 255, message = "编码长度不能超过255")
	private String hyperParameters;

	/**
	 * 划分（训练）
	 */
	@Schema(description = "划分（训练）")
	private Object separate;

	/**
	 * 训练参数
	 */
	@Schema(description = "训练参数")
	private Object params;

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

}

