package com.train.platform.admin.api.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class BaseModelVO implements Serializable {
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
	 * 模型列表
	 */
	List<ModelVO> modelList;
}
