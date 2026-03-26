package com.train.platform.admin.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * <p>
 * 基础模型
 * </p>
 *
 * @author zhouzhipeng
 * @since 2024-01-22
 */
@Data
@Schema(description = "基础模型")
@EqualsAndHashCode(callSuper = true)
@TableName("aircas_base_model")
public class BizBaseModel extends Model<BizBaseModel> {

	private static final long serialVersionUID = 1L;

	@TableId(value = "base_model_id", type = IdType.AUTO)
	@Schema(description = "基础模型id")
	private Long baseModelId;

	/**
	 * 基础模型名称
	 */
	@NotNull(message = "基础模型名称不能为空")
	@Schema(description = "基础模型名称")
	@TableField("base_model_name")
	private String baseModelName;

	/**
	 * 基础模型类型
	 */
	@NotNull(message = "基础模型类型不能为空")
	@Schema(description = "基础模型类型")
	@TableField("base_model_type")
	private String baseModelType;

	/**
	 * 数据类型
	 */
	@NotNull(message = "数据类型不能为空")
	@Schema(description = "数据类型")
	@TableField("data_type")
	private String dataType;

	/**
	 * 基础模型来源
	 */
	@Schema(description = "基础模型来源")
	@TableField("resource")
	private String resource;

	/**
	 * 基础模型存放目录
	 */
	@Schema(description = "基础模型存放目录")
	@TableField("catalogue")
	private String catalogue;

	/**
	 * 基础模型配置存放目录
	 */
	@Schema(description = "基础模型配置文件路径")
	@TableField("config")
	private String config;

	/**
	 * 基础模型训练配置存放目录
	 */
	@Schema(description = "基础模型训练配置文件路径")
	@TableField("train_config")
	private String trainConfig;

	/**
	 * 基础模型描述
	 */
	@Schema(description = "基础模型描述")
	@TableField("description")
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
