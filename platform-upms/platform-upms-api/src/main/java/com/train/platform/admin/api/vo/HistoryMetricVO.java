package com.train.platform.admin.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "指标历史值")
public class HistoryMetricVO {
	@Schema(description = "步")
	private List<Long> steps;

	@Schema(description = "指标值")
	private List<Double> values;

	@Schema(description = "时间戳")
	private List<Long> timestamps;
}
