package com.train.platform.admin.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Schema(description = "标签表")
@EqualsAndHashCode(callSuper = true)
@TableName("aircas_label_tag_info")
@NoArgsConstructor
@AllArgsConstructor
public class BizLabelTagInfo extends Model<BizLabelTagInfo> {

	public BizLabelTagInfo(Long labelId, Long tagId, String tagInfo) {
		this.labelId = labelId;
		this.tagId = tagId;
		this.tagInfo = tagInfo;
	}

	/**
	 * 标签id
	 */
	@Schema(description = "标签id")
	@TableId(value = "tag_id", type = IdType.AUTO)
	private Long tagInfoId;

	/**
	 * 标注id
	 */
	@Schema(description = "标注id")
	private Long labelId;

	/**
	 * 标签id
	 */
	@Schema(description = "标签id")
	private Long tagId;

	/**
	 * 置信度
	 */
	@Schema(description = "置信度")
	private String confidence;

	/**
	 * 评分
	 */
	@Schema(description = "评分")
	private String score;

	/**
	 * 标签id
	 */
	@Schema(description = "标注信息")
	private String tagInfo;

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
