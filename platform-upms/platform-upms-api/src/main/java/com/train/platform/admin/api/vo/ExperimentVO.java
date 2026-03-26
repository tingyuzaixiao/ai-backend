package com.train.platform.admin.api.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Schema(description = "实验前端展示")
public class ExperimentVO implements Serializable {
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
	 * 数据集ID
	 */
	@Schema(description = "数据集ID")
	private Long datasetId;

	/**
	 * 模型ID
	 */
	@Schema(description = "数据集名称")
	private String datasetName;

	/**
	 * 项目ID
	 */
	@Schema(description = "项目ID")
	private Long programId;

	/**
	 * 项目ID
	 */
	@Schema(description = "项目名称")
	private String programName;

	/**
	 * 实验状态
	 */
	@Schema(description = "实验状态")
	private String status;

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
	 * gpuID
	 */
	@Schema(description = "gpuID")
	private String gpuId;

	/**
	 * gpuName
	 */
	@Schema(description = "gpuName")
	private String gpuName;

	/**
	 * card_num
	 */
	@Schema(description = "cardNum")
	private String cardNum;

	/**
	 * 实验类型（0训练，1校验，2预测）
	 */
	@Schema(description = "实验类型（0训练，1校验，2预测）")
	private String type;

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
	private Date startTime;

	/**
	 * 结束时间
	 */
	@Schema(description = "结束时间")
	private Date endTime;

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
	private String separate;

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

	@Schema(description = "权重路径")
	private String checkpointPath;
}
