package com.train.platform.admin.api.entity;


import java.time.LocalDateTime;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

/**
* 用户标注快捷键表
* @TableName aircas_label_shortcut
*/
@Data
@Schema(description = "用户标注快捷键表")
@EqualsAndHashCode(callSuper = true)
@TableName("aircas_label_shortcut")
public class BizLabelShortcut extends Model<BizLabelShortcut> {

    /**
    * 快捷键id
    */
	@TableId(value = "shortcut_id", type = IdType.AUTO)
    @Schema(description = "快捷键id")
    private Long shortcutId;

    /**
    * 用户id
    */
    @Schema(description = "用户id")
    private Long userId;

    /**
    * 通用快捷键
    */
    @Size(max= 255,message="编码长度不能超过255")
    @Schema(description = "通用快捷键")
    @Length(max= 255,message="编码长度不能超过255")
    private String generalShortcut;

    /**
    * 分类快捷键
    */
    @Size(max= 255,message="编码长度不能超过255")
    @Schema(description = "分类快捷键")
    @Length(max= 255,message="编码长度不能超过255")
    private String classifyShortcut;

	/**
	 * 检测快捷键
	 */
	@Size(max= 255,message="编码长度不能超过255")
	@Schema(description = "检测快捷键")
	@Length(max= 255,message="编码长度不能超过255")
	private String detectShortcut;

    /**
    * 分割快捷键
    */
    @Size(max= 255,message="编码长度不能超过255")
    @Schema(description = "分割快捷键")
    @Length(max= 255,message="编码长度不能超过255")
    private String segmentShortcut;

	/**
	 * 创建时间
	 */
	@Schema(description = "创建时间")
	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	/**
	 * 创建人
	 */
	@Size(max = 255, message = "编码长度不能超过255")
	@Schema(description = "创建人")
	@Length(max = 255, message = "编码长度不能超过255")
	@TableField(fill = FieldFill.INSERT)
	private String createBy;

	/**
	 * 更新时间
	 */
	@Schema(description = "更新时间")
	@TableField(fill = FieldFill.UPDATE)
	private LocalDateTime updateTime;

	/**
	 * 更新人
	 */
	@Size(max = 255, message = "编码长度不能超过255")
	@Schema(description = "更新人")
	@Length(max = 255, message = "编码长度不能超过255")
	@TableField(fill = FieldFill.UPDATE)
	private String updateBy;

	/**
	 * 逻辑删除
	 */
	@JsonIgnore
	@TableLogic
	@Schema(description = "删除标记,1:已删除,0:正常")
	@TableField(fill = FieldFill.INSERT)
	private String delFlag;
}
