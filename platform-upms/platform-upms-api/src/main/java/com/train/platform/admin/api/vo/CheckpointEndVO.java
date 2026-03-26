package com.train.platform.admin.api.vo;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权重信息表
 *
 * @TableName aircas_checkpoint
 */
@Data
@Schema(description = "权重")
public class CheckpointEndVO  implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 权重ID
	 */
	@Schema(description = "权重ID")
	private Long checkpointId;

	/**
	 * 关联的训练ID
	 */
	@Schema(description = "关联的训练ID")
	private Long experimentId;

	/**
	 * 关联的模型ID
	 */
	@Schema(description = "关联的模型ID")
	private Long modelId;

	/**
	 * 关联的项目ID
	 */
	@Schema(description = "关联的项目ID")
	private Long programId;

	/**
	 * 轮次
	 */
	@Schema(description = "轮次")
	private Long totalBatches;

	/**
	 * 权重对应的log路径
	 */
	@Size(max = 64, message = "编码长度不能超过64")
	@Schema(description = "权重对应的log路径")
	private String checkpointPath;

	/**
	 * 训练产生的权重文件名列表
	 */
	@Schema(description = "训练产生的权重文件名列表")
	private String checkpointNames;

	/**
	 * 状态
	 */
	@Schema(description = "状态")
	private String state;

	/**
	 * 权重对应的指标的json数据
	 */
	@Schema(description = "权重对应的指标的json数据")
	private String indexJson;

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
	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	/**
	 * 修改时间
	 */
	@Schema(description = "修改时间")
	private LocalDateTime updateTime;

	/**
	 * 是否删除 1：已删除 0：正常
	 */
	@Schema(description = "删除标记,1:已删除,0:正常")
	@TableField(fill = FieldFill.INSERT)
	private String delFlag;

}
