package com.train.platform.admin.api.entity;


import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

/**
* 标注任务表
* @TableName aircas_label_task
*/
@Data
@Schema(description = "标注任务表")
@EqualsAndHashCode(callSuper = true)
@TableName("aircas_label_task")
public class BizLabelTask extends Model<BizLabelTask> {

    /**
    * 标注任务id
    */
    @Schema(description="标注任务id")
	@TableId(value = "task_id", type = IdType.AUTO)
    private Long taskId;

	/**
    * 标注任务名称
    */
    @Size(max= 255,message="编码长度不能超过255")
    @Schema(description="标注任务名称")
    @Length(max= 255,message="编码长度不能超过255")
    private String taskName;

	/**
    * 数据集id
    */
    @Schema(description="数据集id")
    private Long datasetId;

	/**
    * 基础数据集id
    */
    @Schema(description="基础数据集id")
    private Long baseDatasetId;

	/**
    * 任务类型
    */
    @Schema(description="任务类型（图像分类0、物体检测1、图像分割2）")
    private String labelType;

	/**
	 * 子任务类型
	 */
	@Schema(description="子任务类型物体检测1（0矩形框、1直线、2圆））")
	private String subLabelType;

	/**
	 * 数据类型
	 */
	@Schema(description="数据类型")
	private String dataType;

	/**
    * 标签组
    */
    @Size(max= 255,message="编码长度不能超过255")
    @Schema(description="标签组")
    @Length(max= 255,message="编码长度不能超过255")
    private String tagIds;

	/**
	 * 智能标注url
	 */
	private String intelligentAnnotationUrl;

	/**
	 * 描述（备注）
	 */
	@Schema(description="描述（备注）")
	private String description;

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
	 * 创建人
	 */
	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "创建人")
	private String createBy;

	/**
	 * 更新人
	 */
	@Size(max= 255,message="编码长度不能超过255")
	@Schema(description="更新人")
	@Length(max= 255,message="编码长度不能超过255")
	@TableField(fill = FieldFill.UPDATE)
	private String updateBy;

	/**
	 * 是否删除 1：已删除 0：正常
	 */
	@TableLogic
	@JsonIgnore
	@Schema(description = "删除标记,1:已删除,0:正常")
	@TableField(fill = FieldFill.INSERT)
	private String delFlag;
}
