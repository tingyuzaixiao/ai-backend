package com.train.platform.admin.api.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.util.Date;

/**
 * 智能标注
 * @TableName aircas_label_intelligent
 */
@Data
public class LabelIntelligentDTO implements Serializable {

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
	 * 创建人
	 */
	@Schema(description = "创建人")
	@Length(max = 64, message = "编码长度不能超过64")
	private String createBy;

	/**
	 * 创建时间
	 */
	@Schema(description = "创建时间")
	private Date createTime;

	/**
	 * 更新人
	 */
	@Schema(description = "更新人")
	@Length(max = 64, message = "编码长度不能超过64")
	private String updateBy;

	/**
	 * 更新时间
	 */
	@Schema(description = "更新时间")
	private Date updateTime;

	/**
	 * 逻辑删除
	 */
	@Schema(description = "逻辑删除")
	private String delFlag;

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
}
