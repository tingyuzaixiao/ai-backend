package com.train.platform.admin.api.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class LabelTagInfoVO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

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
	 * 标签id
	 */
	@Schema(description = "标注信息")
	private String tagInfo;

	private String confidence;

	private String score;
}
