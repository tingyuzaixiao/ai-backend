package com.train.platform.admin.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "停止试验")
public class ExperimentStopVO implements Serializable {
	@Serial
	private static final long serialVersionUID = 5177812623949608788L;

	/**
	 * 主键
	 */
	@Schema(description = "主键")
	@NotNull
	private Long id;

	/**
	 * 停止类型：experimentStopType {@link com.train.platform.common.core.constant.enums.ExperimentStopType}
	 */
	@Schema(description = "停止类型")
	private Integer stopType;
}
