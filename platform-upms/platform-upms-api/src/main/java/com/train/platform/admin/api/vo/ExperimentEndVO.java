package com.train.platform.admin.api.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "实验前端展示")
public class ExperimentEndVO implements Serializable {
	/**
	 * 主键
	 */
	@Schema(description = "主键")
	private Long experimentId;

	/**
	 * 实验名称
	 */
	@Schema(description = "实验名称")
	private String experimentName;

	/**
	 * 模型ID
	 */
	@Schema(description = "模型ID")
	private Long modelId;

	/**
	 * 模型名称
	 */
	@Schema(description = "模型名称")
	private String modelName;

	/**
	 * 项目ID
	 */
	@Schema(description = "项目ID")
	private Long programId;

	/**
	 * 项目名称
	 */
	@Schema(description = "项目名称")
	private String programName;

	/**
	 * 实验类型（0训练，1校验，2预测）
	 */
	@Schema(description = "实验类型（0训练，1校验，2预测）")
	private String type;

	/**
	 * 起始checkpoint
	 */
	@Schema(description = "起始checkpoint路径")
	private String beginCheckpointPath;

	/**
	 * 暂停时checkpoint
	 */
	@Schema(description = "暂停时checkpoint路径")
	private String pauseCheckpointPath;

	/**
	 * 标签描述（校验）
	 */
	@Schema(description = "标签描述（校验）")
	private String labelDesc;

	/**
	 * 模型预测的可能类别极其对应置信度列表（校验）
	 */
	@Schema(description = "模型预测的可能类别极其对应置信度列表（校验）")
	private String topPredictions;


	/**
	 * 实验训练所产生的权重信息
	 */
	@Schema(description = "实验训练所产生的权重信息")
	private List<CheckpointEndVO> checkpointList;

	/**
	 * 模型预测的可能类别极其对应置信度列表（校验）
	 */
	@Schema(description = "权重对应的log路径")
	private String checkpointPath;

	/**
	 * 实验状态
	 */
	@Schema(description = "实验状态")
	private String status;

}
