package com.train.platform.admin.api.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "标注任务列表前端展示")
public class BaseDatasetLabelTaskVO implements Serializable {

	@Serial
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
	 * 数据类型
	 */
	@Schema(description = "数据类型")
	private String dataType;

	/**
	 * 任务类型
	 */
	@Schema(description = "任务类型")
	private String labelType;

	/**
	 * 基础数据集图像数量
	 */
	@Schema
	private Integer count;

	/**
	 * 标注任务列表
	 */
	private List<LabelTaskListVO> labelTaskList;
}
