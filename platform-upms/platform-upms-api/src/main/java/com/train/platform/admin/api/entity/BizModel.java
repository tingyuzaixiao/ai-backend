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
 * 训练模型，由基础模型创建
 * </p>
 *
 * @author zhouzhipeng
 * @since 2024-01-22
 */
@Data
@Schema(description = "模型")
@EqualsAndHashCode(callSuper = true)
@TableName("aircas_model")
public class BizModel extends Model<BizModel> {

	private static final long serialVersionUID = 1L;

	@TableId(value = "model_id", type = IdType.AUTO)
	@Schema(description = "模型id")
	private Long modelId;

	/**
	 * 关联的基础模型ID
	 */
	@NotNull(message = "关联的基础模型ID不能为空")
	@Schema(description = "关联的基础模型ID")
	@TableField("base_model_id")
	private Long baseModelId;

	/**
	 * 模型名称
	 */
	@NotNull(message = "模型名称不能为空")
	@Schema(description = "模型名称")
	@TableField("model_name")
	private String modelName;

	/**
	 * 模型版本
	 */
	@Schema(description = "模型版本")
	@TableField("version")
	private String version;

	/**
	 * 模型类型
	 */
	@NotNull(message = "模型类型不能为空")
	@Schema(description = "模型类型")
	@TableField("model_type")
	private String modelType;

	/**
	 * 模型类型
	 */
	@NotNull(message = "数据类型不能为空")
	@Schema(description = "数据类型")
	@TableField("data_type")
	private String dataType;

	/**
	 * 预训练权重目录
	 */
	@Schema(description = "预训练权重目录")
	@TableField("pretrain_checkpoint")
	private String pretrainCheckpoint;

	/**
	 * 模型来源
	 */
	@Schema(description = "模型来源")
	@TableField("resource")
	private String resource;

	/**
	 * 行业领域
	 */
	@Schema(description = "行业领域")
	@TableField("domain")
	private String domain;

	/**
	 * 模型支持的下游操作
	 */
	@Schema(description = "模型支持的下游操作")
	@TableField("support_options")
	private String supportOptions;

	/**
	 * 开原证书
	 */
	@Schema(description = "开原证书")
	@TableField("license")
	private String license;

	/**
	 * 模型代码链接地址
	 */
	@Schema(description = "模型代码链接地址")
	@TableField("download_source")
	private String downloadSource;

	/**
	 * 基础模型存放目录
	 */
	@Schema(description = "模型存放目录")
	@TableField("catalogue")
	private String catalogue;

	/**
	 * 模型运行conda名称
	 */
	@Schema(description = "模型运行conda名称")
	@TableField("environment")
	private String environment;

	/**
	 * 基础模型配置存放目录
	 */
	@Schema(description = "模型配置文件路径")
	@TableField("config")
	private String config;

	/**
	 * 基础模型训练配置存放目录
	 */
	@Schema(description = "模型训练配置文件路径")
	@TableField("train_config")
	private String trainConfig;

	/**
	 * 基础模型描述
	 */
	@Schema(description = "模型描述")
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
