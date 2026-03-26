package com.train.platform.admin.api.vo;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "数据集前端展示")
public class DatasetVO implements Serializable {
	/**
	 * 数据集ID
	 */
	@Schema(description = "数据集ID")
	private Long datasetId;

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
	 * 数据集名称
	 */
	@Schema(description = "数据集名称")
	private String datasetName;

	/**
	 * 标注进度
	 */
	@Schema(description = "标注进度")
	private String progress;

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
	 * 数据集大小
	 */
	@Schema(description = "数据集大小")
	private Long size;

	/**
	 * 版本
	 */
	@Schema(description = "版本")
	private String version;

	/**
	 * 标注类型
	 */
	@Schema(description = "任务类型")
	private String labelType;

	/**
	 * 数据集首图
	 */
	@Schema(description = "数据集首图")
	private String firstPicture;

	/**
	 * 标注json文件目录，url存在多个
	 */
	@Schema(description = "标注json文件目录，url存在多个")
	private String labelJson;

	/**
	 * 创建人
	 */
	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "创建人")
	private String createBy;

	/**
	 * 修改人
	 */
	@TableField(fill = FieldFill.UPDATE)
	@Schema(description = "修改人")
	private String updateBy;

	/**
	 * 创建时间
	 */
	@Schema(description = "创建时间")
	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	/**
	 * 修改时间
	 */
	@Schema(description = "修改时间")
	@TableField(fill = FieldFill.UPDATE)
	private LocalDateTime updateTime;
}
