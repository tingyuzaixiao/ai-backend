package com.train.platform.admin.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
@AllArgsConstructor
@Schema(description = "内部服务开始运行")
public class InnerServerStartVO implements Serializable {
	@Serial
	private static final long serialVersionUID = -7554002365976427178L;

	@Schema(description = "实验名称")
	private Long taskId;

	@Schema(description = "实验名称")
	private String runName;

	@Schema(description = "项目名称")
	private String experimentName;

	@Size(max = 2048, message = "编码长度不能超过2048")
	@Schema(description = "参数")
	private String params;

	@Schema(description = "数据集id")
	private Long datasetId;

	@Schema(description = "数据标注id")
	private Long labelId;

	@Schema(description = "实验tags")
	private Map<String, String> tags;
}
