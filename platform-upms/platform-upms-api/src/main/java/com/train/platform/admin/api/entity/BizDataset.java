package com.train.platform.admin.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

/**
 * @TableName aircas_dataset
 */
@Data
@Schema(description = "数据集")
@EqualsAndHashCode(callSuper = true)
@TableName("aircas_dataset")
public class BizDataset extends Model<BizDataset> {

	private static final long serialVersionUID = 1L;

	/**
	 * 数据集ID
	 */
	@Schema(description = "数据集ID")
	@TableId(type = IdType.AUTO)
	private Long datasetId;

	/**
	 * 基础数据集ID
	 */
	@Schema(description = "基础数据集ID")
	private Long baseDatasetId;

	/**
	 * 数据集名称
	 */
	@Size(max = 50, message = "编码长度不能超过50")
	@Schema(description = "数据集名称")
	@Length(max = 50, message = "编码长度不能超过50")
	private String datasetName;

	/**
	 * 标注进度
	 */
	@Size(max = 255, message = "编码长度不能超过255")
	@Schema(description = "标注进度")
	@Length(max = 255, message = "编码长度不能超过255")
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
	@Size(max = 20, message = "编码长度不能超过20")
	@Schema(description = "版本")
	@Length(max = 20, message = "编码长度不能超过20")
	private String version;

	/**
	 * 标注类型
	 */
	@Schema(description = "标注类型")
	private String labelType;

	/**
	 * 数据集首图
	 */
	@Size(max = 255, message = "编码长度不能超过255")
	@Schema(description = "数据集首图")
	@Length(max = 255, message = "编码长度不能超过255")
	private String firstPicture;

	/**
	 * 标注json文件目录，url存在多个
	 */
	@Schema(description = "标注json文件目录，url存在多个")
	private String labelJson;

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
	@TableField(fill = FieldFill.UPDATE)
	private LocalDateTime updateTime;

	/**
	 * 是否删除 1：已删除 0：正常
	 */
	@TableLogic
	@Schema(description = "删除标记,1:已删除,0:正常")
	@TableField(fill = FieldFill.INSERT)
	private String delFlag;

}
