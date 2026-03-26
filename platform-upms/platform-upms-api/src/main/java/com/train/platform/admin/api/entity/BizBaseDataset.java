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
 * @TableName aircas_base_dataset
 */
@Data
@Schema(description = "基础数据集")
@EqualsAndHashCode(callSuper = true)
@TableName("aircas_base_dataset")
public class BizBaseDataset extends Model<BizBaseDataset> {

	private static final long serialVersionUID = 1L;

	/**
	 * 基础数据集ID
	 */
	@TableId(type = IdType.AUTO)
	@Schema(description = "基础数据集ID")
	private Long baseDatasetId;

	/**
	 * 基础数据集名称
	 */
	@Size(max = 50, message = "编码长度不能超过50")
	@Schema(description = "基础数据集名称")
	@Length(max = 50, message = "编码长度不能超过50")
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
	 * 基础数据大小
	 */
	@Schema(description = "基础数据大小")
	private Long size = 0L;

	/**
	 * 基础数据集状态
	 */
	@Size(max = 50, message = "编码长度不能超过50")
	@Schema(description = "基础数据集状态")
	@Length(max = 50, message = "编码长度不能超过50")
	private String state;

	/**
	 * 标签json目录
	 */
	@Schema(description = "标签json目录")
	private String labelJson;

	/**
	 * 任务类型
	 */
	@Schema(description = "任务类型")
	private String labelType;

	/**
	 * 标注类型
	 */
	@Schema(description = "导入类型(0 本地，1 OSS)")
	private String importType;

	/**
	 * 描述
	 */
	@Size(max = 255, message = "编码长度不能超过255")
	@Schema(description = "描述")
	@Length(max = 255, message = "编码长度不能超过255")
	private String description;

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
