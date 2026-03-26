package com.train.platform.admin.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
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
@Schema(description = "模型前端展示")
public class ModelVO implements Serializable {

	private static final long serialVersionUID = 1L;

	@Schema(description = "模型id")
	private Long modelId;

	/**
	 * 关联的基础模型ID
	 */
	@Schema(description = "关联的基础模型ID")
	private Long baseModelId;

	/**
	 * 关联的基础模型ID
	 */
	@Schema(description = "关联的基础模型name")
	private String baseModelName;

	/**
	 * 模型名称
	 */
	@Schema(description = "模型名称")
	private String modelName;

	/**
	 * 模型版本
	 */
	@Schema(description = "模型版本")
	private String version;

	/**
	 * 模型类型
	 */
	@Schema(description = "模型类型")
	private String modelType;

	/**
	 * 模型类型
	 */
	@Schema(description = "数据类型")
	private String dataType;

	/**
	 * 预训练权重目录
	 */
	@Schema(description = "预训练权重目录")
	private String pretrainCheckpoint;

	/**
	 * 模型来源
	 */
	@Schema(description = "模型来源")
	private String resource;

	/**
	 * 行业领域
	 */
	@Schema(description = "行业领域")
	private String domain;

	/**
	 * 模型支持的下游操作
	 */
	@Schema(description = "模型支持的下游操作")
	private String supportOptions;

	/**
	 * 开原证书
	 */
	@Schema(description = "开原证书")
	private String license;

	/**
	 * 模型代码链接地址
	 */
	@Schema(description = "模型代码链接地址")
	private String downloadSource;

	/**
	 * 基础模型存放目录
	 */
	@Schema(description = "模型存放目录")
	private String catalogue;

	/**
	 * 模型运行conda名称
	 */
	@Schema(description = "模型运行conda名称")
	private String environment;

	/**
	 * 基础模型配置存放目录
	 */
	@Schema(description = "模型配置文件路径")
	private String config;

	/**
	 * 基础模型训练配置存放目录
	 */
	@Schema(description = "模型训练配置文件路径")
	private String trainConfig;

	/**
	 * 基础模型描述
	 */
	@Schema(description = "模型描述")
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

}
