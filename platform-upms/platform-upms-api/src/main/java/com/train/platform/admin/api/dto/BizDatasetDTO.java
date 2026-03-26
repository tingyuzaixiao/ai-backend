package com.train.platform.admin.api.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;


@Data
public class BizDatasetDTO implements Serializable {
	private static final long serialVersionUID = 1L;

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
	@NotNull(message = "数据类型不能为空")
	@Schema(description = "数据类型")
	@TableField("data_type")
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
	@Schema(description = "标注类型")
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
	private Object labelJson;

	/**
	 * 所选数据集imageIds
	 */
	@Schema(description = "所选数据集imageIds")
	private String imageIds;

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
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updateTime;

	@Schema(description = "继承ID")
	private Long inheritId;
}
