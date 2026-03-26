package com.train.platform.admin.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "数据集查询对象")
public class DatasetImgDTO {

	@Schema(description = "页面大小")
	private Integer pageSize;

	@Schema(description = "当前页")
	private Integer pageNum;

	@Schema(description = "数据集ID")
	private Long datasetId;

	@Schema(description = "原文件名")
	private String originalName;

	@Schema(description = "标签文件名称")
	private String labelFileName;
}
