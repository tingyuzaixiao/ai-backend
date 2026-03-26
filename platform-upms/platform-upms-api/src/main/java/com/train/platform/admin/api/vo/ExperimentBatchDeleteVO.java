package com.train.platform.admin.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "批量删除试验")
public class ExperimentBatchDeleteVO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1504967614203095783L;

	/**
	 * 主键列表
	 */
	@Schema(description = "主键列表")
	@NotEmpty
	private List<Long> ids;
}
