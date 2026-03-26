package com.train.platform.admin.api.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

/**
 * GPU资源信息表
 *
 * @TableName aircas_gpu_resource
 */
@Data
@Schema(description = "GPU资源信息")
@EqualsAndHashCode(callSuper = true)
@TableName("aircas_gpu_resource")
public class BizGpuResource extends Model<BizGpuResource> {
	/**
	 * 逻辑主键id
	 */
	@Schema(description = "逻辑主键id")
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * gpu uuid
	 */
	@Schema(description = "gpu uuid")
	private String gpuUuid;

	/**
	 * gpu id
	 */
	@Schema(description = "gpu id")
	private Integer gpuId;

	/**
	 * gpu名称
	 */
	@Size(max = 64, message = "编码长度不能超过64")
	@Schema(description = "gpu名称")
	@Length(max = 64, message = "编码长度不能超过64")
	private String gpuName;

	/**
	 * 显存
	 */
	@Schema(description = "gpu显存")
	private Integer gpuMemory;

	/**
	 * ip
	 */
	@Size(max = 255, message = "编码长度不能超过64")
	@Schema(description = "ip")
	@Length(max = 255, message = "编码长度不能超过64")
	private String ip;

	/**
	 * 处理器状态 {@link com.train.platform.common.core.constant.enums.GpuStatus}
	 */
	@Size(max = 64, message = "编码长度不能超过64")
	@Schema(description = "处理器状态")
	@Length(max = 64, message = "编码长度不能超过64")
	private String state;

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
	 * 项目名称
	 */
	@Schema(description = "项目名称")
	private String programName;

	/**
	 * 实验名称
	 */
	@Schema(description = "实验名称")
	private String experimentName;

	/**
	 * 是否删除 1：已删除 0：正常
	 */
	@TableLogic
	@Schema(description = "删除标记,1:已删除,0:正常")
	@TableField(fill = FieldFill.INSERT)
	private String delFlag;

}
