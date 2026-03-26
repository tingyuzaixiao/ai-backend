package com.train.platform.admin.api.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.train.platform.admin.api.entity.BizLabel;
import com.train.platform.admin.api.entity.BizLabelTag;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;


@Data
@Schema(description = "标注任务列表前端展示")
public class LabelTaskVO implements Serializable {

	@Serial
	private final static long serialVersionUID = 1L;

	/**
	 * 标注任务id
	 */
	@Schema(description = "标注任务id")
	@TableId(value = "task_id", type = IdType.AUTO)
	private Long taskId;

	/**
	 * 标注任务名称
	 */
	@Schema(description = "标注任务名称")
	private String taskName;

	/**
	 * 数据集id
	 */
	@Schema(description = "数据集id")
	private Long datasetId;

	/**
	 * 基础数据集id
	 */
	@Schema(description = "基础数据集id")
	private Long baseDatasetId;

	/**
	 * 标注类型
	 */
	@Schema(description = "标注类型（图像分类0、物体检测1、图像分割2）")
	private String labelType;

	/**
	 * 标注类型
	 */
	@Schema(description = "数据类型")
	private String dataType;

	/**
	 * 描述（备注）
	 */
	@Schema(description = "描述（备注）")
	private String description;

	/**
	 * 智能标注url
	 */
	@Schema(description = "智能标注url")
	private String intelligentAnnotationUrl;

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
	@Size(max = 255, message = "编码长度不能超过255")
	@Schema(description = "更新人")
	@Length(max = 255, message = "编码长度不能超过255")
	@TableField(fill = FieldFill.UPDATE)
	private String updateBy;
	/**
	 * 标签组
	 */
	@Schema(description = "标签组")
	private String tagIds;

	/**
	 * 标签组列表
	 */
	@Schema(description = "标签组列表")
	private List<BizLabelTag> tagList;

//	@Schema(description = "标签列表")
//	private List<LabelVO> labelList;
}
