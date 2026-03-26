package com.train.platform.admin.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class FilePageVO implements Serializable {

	private static final long serialVersionUID = 1L;

	@Schema(description = "该数据集总图像数量")
	private Integer totals;

	@Schema(description = "图像list")
	private List<FileVO> fileList;

	@Schema(description = "页面大小")
	private Integer pageSize;

	@Schema(description = "当前页")
	private Integer pageNum;
}
