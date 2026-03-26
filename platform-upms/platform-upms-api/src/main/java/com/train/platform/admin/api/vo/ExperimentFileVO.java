package com.train.platform.admin.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author zj
 * @date 2025/9/30 listFile返回类型
 */
@Data
@Schema(description = "实验文件")
public class ExperimentFileVO implements Serializable {
	@Serial
	private static final long serialVersionUID = -7879323911466224614L;

	@Schema(description = "文件名称")
	private String fileName;

	@Schema(description = "是否是目录")
	private Boolean dir;
}
