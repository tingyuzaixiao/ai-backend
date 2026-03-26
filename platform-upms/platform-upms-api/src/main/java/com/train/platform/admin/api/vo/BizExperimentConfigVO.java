package com.train.platform.admin.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "实验配置")
public class BizExperimentConfigVO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1018639763326513644L;
	/**
	 * 实验名称
	 */
	@Schema(description = "实验名称")
	private String experimentName;

	/**
	 * 引用权重的项目名称
	 */
	@Schema(description = "引用权重的项目名称")
	private String refProgramName;

	/**
	 * 引用权重的实验名称
	 */
	@Schema(description = "引用权重的实验名称")
	private String refExperimentName;

	/**
	 * 实验描述
	 */
	@Schema(description = "实验描述")
	private String description;

	/**
	 * 训练算法
	 */
	@Schema(description = "训练算法")
	private String algorithm;

	/**
	 * python的requirements
	 */
	@Schema(description = "python的requirements")
	private String pyRequirements;

	/**
	 * 模型文件
	 */
	@Schema(description = "模型文件")
	private String modelFile;

	/**
	 * 权重文件
	 */
	@Schema(description = "权重文件")
	private String weightsFile;

	/**
	 * 数据集ID
	 */
	@Schema(description = "数据集ID")
	private Long datasetId;

	/**
	 * 数据标注id
	 */
	@Schema(description = "数据标注id")
	private Long taskId;

	/**
	 * 超参数
	 */
	@Schema(description = "超参数")
	private String hyperParameters;

	/**
	 * 数据增强参数
	 */
	@Schema(description = "数据增强参数")
	private String augmentationParams;

	/**
	 * 指标
	 */
	@Schema(description = "指标")
	private String metrics;
}
