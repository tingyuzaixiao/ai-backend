package com.train.platform.admin.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@Schema(description = "内部服务停止运行")
public class InnerServerStopVO implements Serializable {
	@Serial
	private static final long serialVersionUID = -8501981295450494280L;

	/**
	 * {@link com.train.platform.common.core.constant.enums.ExperimentStopType}
	 */
	@Schema(description = "停止类型")
	private Integer stopType;
}
