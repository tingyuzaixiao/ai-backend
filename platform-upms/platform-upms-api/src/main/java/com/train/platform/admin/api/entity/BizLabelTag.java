package com.train.platform.admin.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

/**
 * 标签
 *
 * @TableName aircas_label_tag
 */
@Data
@Schema(description = "标签表")
@EqualsAndHashCode(callSuper = true)
@TableName("aircas_label_tag")
public class BizLabelTag extends Model<BizLabelTag> {

	/**
	 * 标签id
	 */
	@Schema(description = "标签id")
	@TableId(value = "tag_id", type = IdType.AUTO)
	private Long tagId;
	/**
	 * 标签组id
	 */
	@Schema(description = "标签组id")
	private Long groupId;

	/**
	 * 标注任务id
	 */
	@Schema(description = "标注任务id")
	private Long taskId;

	/**
	 * 标签名称
	 */
	@Schema(description = "标签名称")
	private String tagName;
	/**
	 * 标签颜色
	 */
	@Schema(description = "标签颜色")
	private String color;

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
	@JsonIgnore
	@TableLogic
	@Schema(description = "删除标记,1:已删除,0:正常")
	@TableField(fill = FieldFill.INSERT)
	private String delFlag;
}
