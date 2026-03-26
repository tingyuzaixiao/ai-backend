package com.train.platform.admin.api.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "GPU前端展示")
public class GpuResourceVO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 逻辑主键id
	 */
	@Schema(description = "逻辑主键id")
	private Long id;

	/**
	 * 用户输入的处理器ID
	 */
	@Schema(description = "用户输入的处理器ID")
	private String gpuId;

	/**
	 * 正在占用的项目ID
	 */
	@Schema(description = "正在占用资源的项目ID")
	private Long programId;

	/**
	 * 正在占用的项目ID
	 */
	@Schema(description = "正在占用资源的项目名称")
	private String programName;

	/**
	 * gpu资源对应的训练id或者校验id
	 */
	@Schema(description = "gpu资源对应的训练id或者校验id")
	private Long experimentId;

	/**
	 * gpu资源对应的训练id或者校验id
	 */
	@Schema(description = "gpu资源对应的训练名称或者校验名称")
	private String experimentName;

	/**
	 * 处理器名称
	 */
	@Schema(description = "处理器名称")
	private String gpuName;

	/**
	 * 处理器状态(0未占用，1占用)
	 */
	@Schema(description = "处理器状态(0未占用，1占用)")
	private String state;


	/**
	 * 进程ID
	 */
	@Schema(description = "进程ID")
	private Long processId;

	/**
	 * 进程状态(0未运行，1运行中)
	 */
	@Schema(description = "进程状态(0未运行，1运行中)")
	private String processState;

	/**
	 * 实验类型
	 */
	@Schema(description = "实验类型")
	private String experimentType;

	/**
	 * ip
	 */
	@Schema(description = "ip")
	private String ipAddr;

	/**
	 * card number
	 */
	@Schema(description = "card number")
	private String cardNum;

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
	 * 开始执行时间
	 */
	@Schema(description = "开始执行时间")
	private LocalDateTime startExecutionTime;
}
