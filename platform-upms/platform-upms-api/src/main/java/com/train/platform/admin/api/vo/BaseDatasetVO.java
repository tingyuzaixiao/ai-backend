package com.train.platform.admin.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "基础数据集前端展示")
public class BaseDatasetVO implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 基础数据集ID
	 */
	@Schema(description = "基础数据集ID")
	private Long baseDatasetId;

	/**
	 * 基础数据集名称
	 */
	@Schema(description = "基础数据集名称")
	private String baseDatasetName;

	/**
	 * 图像数量
	 */
	@Schema(description = "图像数量")
	private Integer count;

	/**
	 * 数据类型
	 */
	@Schema(description = "数据类型")
	private String dataType;

	/**
	 * 基础数据集状态
	 */
	@Schema(description = "基础数据集状态")
	private String state;

	/**
	 * 基础数据集大小
	 */
	@Schema(description = "基础数据集大小")
	private String size;

	/**
	 * 标签json目录
	 */
	@Schema(description = "标签json目录")
	private String labelJson;

	/**
	 * 标注类型
	 */
	@Schema(description = "标注类型")
	private String labelType;

	/**
	 * 标注类型
	 */
	@Schema(description = "导入类型(0 本地，1 OSS)")
	private String importType;

	/**
	 * 描述
	 */
	@Schema(description = "描述")
	private String description;

	/**
	 * 创建人
	 */
	@Schema(description = "创建人")
	private String createBy;

	/**
	 * 修改人
	 */
	@Schema(description = "修改人")
	private String updateBy;

	/**
	 * 创建时间
	 */
	@Schema(description = "创建时间")
	private LocalDateTime createTime;

	/**
	 * 修改时间
	 */
	@Schema(description = "修改时间")
	private LocalDateTime updateTime;

	/**
	 * 数据集list
	 */
	private List<DatasetVO> datasetList;
}
