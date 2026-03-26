package com.train.platform.admin.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "开始试验")
public class ExperimentStartVO implements Serializable {
	@Serial
	private static final long serialVersionUID = -5990911715702673941L;
	/**
	 * 主键
	 */
	@Schema(description = "主键")
	@NotNull
	private Long id;

	/**
	 * 资源表主键
	 */
	@Schema(description = "资源id")
	@NotNull
	private Long resourceId;
}
