package com.train.platform.admin.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

@Data
@Schema(description = "算法服务资源")
@EqualsAndHashCode(callSuper = true)
@TableName("aircas_algo_server")
public class AlgoServerResource extends Model<AlgoServerResource> {
	@Serial
	private static final long serialVersionUID = -28835519571522798L;
	/**
	 * 主键
	 */
	@Schema(description = "主键")
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 服务
	 */
	@NotBlank(message = "服务不能为空")
	@Schema(description = "服务")
	private String server;

	/**
	 * 服务描述
	 */
	@NotBlank(message = "服务描述不能为空")
	@Schema(description = "服务描述")
	@Size(max = 255, message = "编码长度不能超过255")
	private String description;

	/**
	 * 服务参数
	 */
	@NotBlank(message = "服务参数不能为空")
	@Schema(description = "服务参数")
	@Size(max = 2048, message = "编码长度不能超过2048")
	private String params;

	/**
	 * 服务状态 {@link com.train.platform.common.core.constant.enums.GpuStatus}
	 */
	@Schema(description = "服务状态")
	private String status;

	/**
	 * 心跳时间
	 */
	@Schema(description = "心跳时间")
	private LocalDateTime heartbeatTime;

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
