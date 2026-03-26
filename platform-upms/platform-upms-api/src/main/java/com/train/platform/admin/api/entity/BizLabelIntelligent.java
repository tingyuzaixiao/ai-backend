package com.train.platform.admin.api.entity;

import java.io.Serializable;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 智能标注
 * @TableName aircas_label_intelligent
 */
@TableName(value ="aircas_label_intelligent")
@Data
public class BizLabelIntelligent extends Model<BizLabelIntelligent> implements Serializable {

	/**
	 * 主键
	 */
	@Schema(description = "主键")
	@TableId(value = "intelligent_task_id", type = IdType.AUTO)
	private Long intelligentTaskId;

	/**
	 * 智能标注任务名称
	 */
	@Schema(description = "编码长度不能超过255")
	private String intelligentTaskName;

	/**
	 * 智能标注任务类型
	 */
	@Schema(description = "智能标注任务类型")
	private String intelligentTaskType;

	/**
	 * 智能标注url
	 */
	@Schema(description = "编码长度不能超过255")
	private String intelligentUrl;

	/**
	 * 模型id
	 */
	@Schema(description = "模型id")
	private Long modelId;

	/**
	 * 基础数据集id
	 */
	@Schema(description = "基础数据集id")
	private Long baseDatasetId;

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
