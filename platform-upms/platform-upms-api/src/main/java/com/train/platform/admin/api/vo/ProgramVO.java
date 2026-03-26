package com.train.platform.admin.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "前端项目展示对象")
public class ProgramVO implements Serializable {
	/**
	 * 项目ID
	 */
	@Schema(description ="项目ID")
	private String programId;

	/**
	 * 项目名称
	 */
	@Schema(description ="项目名称")
	private String name;

	/**
	 * 数据类型
	 */
	@Schema(description ="数据类型")
	private String dataType;

	/**
	 * 任务类型
	 */
	@Schema(description ="任务类型")
	private String labelType;

	/**
	 * 项目描述
	 */
	@Schema(description ="项目描述")
	private String description;

	/**
	 * 创建人
	 */
	@Schema(description ="创建人")
	private String createBy;

	/**
	 * 修改人
	 */
	@Schema(description ="修改人")
	private String updateBy;

	/**
	 * 创建时间
	 */
	@Schema(description ="创建时间")
	private LocalDateTime createTime;

	/**
	 * 更新时间
	 */
	@Schema(description ="更新时间")
	private LocalDateTime updateTime;

	/**
	 * 实验
	 */
	@Schema(description ="实验列表")
	private List<ExperimentVO> experimentList;
}
