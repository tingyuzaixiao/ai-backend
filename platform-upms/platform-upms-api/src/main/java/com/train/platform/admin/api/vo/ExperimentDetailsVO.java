package com.train.platform.admin.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author zj
 * @date 2025/9/30 实验详情
 */
@Data
@Schema(description = "实验详情")
public class ExperimentDetailsVO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1412868376333658997L;

	@Schema(description = "指标名称")
	private List<String> metricNames;

	@Schema(description = "指标值")
	private List<Double> metricVals;

	@Schema(description = "参数名称")
	private List<String> paramNames;

	@Schema(description = "参数值")
	private List<String> paramVals;

	@Schema(description = "tag名称")
	private List<String> tagNames;

	@Schema(description = "tag值")
	private List<String> tagVals;

	@Schema(description = "项目id")
	private String programId;

	@Schema(description = "实验id")
	private String experimentId;

	@Schema(description = "实验名称")
	private String experimentName;

	@Schema(description = "实验状态")
	private String status;

	@Schema(description = "开始时间")
	private LocalDateTime startTime;

	@Schema(description = "实验运行时长")
	private Long duration;

	@Schema(description = "资源id")
	private Long resourceId;

	@Schema(description = "算法服务地址")
	private String server;

	@Schema(description = "算法服务描述")
	private String serverDescription;

	@Schema(description = "算法参数配置")
	private String serverParams;

//	@Schema(description = "gpu uuid")
//	private String gpuUuid;
//
//	@Schema(description = "gpu id")
//	private Integer gpuId;
//
//	@Schema(description = "gpu名称")
//	private String gpuName;
//
//	@Schema(description = "gpu显存")
//	private Integer gpuMemory;
//
//	@Schema(description = "ip")
//	private String ip;

	@Schema(description = "进度")
	private Double progress;

	@Schema(description = "数据集ID")
	private Long datasetId;

	@Schema(description = "数据标注id")
	private Long taskId;

	@Schema(description = "模型文件")
	private String modelFile;

	@Schema(description = "权重文件")
	private String weightsFile;

	@Schema(description = "引用权重的项目名称")
	private String refProgramName;

	@Schema(description = "引用权重的实验名称")
	private String refExperimentName;

	@Schema(description = "训练算法")
	private String algorithm;

	@Schema(description = "实验描述")
	private String description;

	@Schema(description = "实验输出的模型")
	private String outputModel;

	@Schema(description = "数据增强参数")
	private String augmentationParams;
}
