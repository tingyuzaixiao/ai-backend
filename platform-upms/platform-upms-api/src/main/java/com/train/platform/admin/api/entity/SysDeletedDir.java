package com.train.platform.admin.api.entity;


import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

/**
* 
* @TableName sys_deleted_dir
*/
@Data
@Schema(description = "待删除文件")
@EqualsAndHashCode(callSuper = true)
@TableName("sys_deleted_dir")
public class SysDeletedDir extends Model<SysDeletedDir> {

    /**
    * 主键
    */
    @Schema(description="主键")
	@TableId(value = "dir_id", type= IdType.AUTO)
    private Long dirId;

    /**
    * 待删除目录
    */
    @Schema(description="待删除目录")
    @Length(max= 255,message="编码长度不能超过255")
    private String dirPath;

    /**
    * 待删除文件名
    */
    @Schema(description="待删除文件名")
    @Length(max= 100,message="编码长度不能超过100")
    private String fileName;

    /**
    * 删除类型(0文件夹、1文件)
    */
    @Schema(description="删除类型(0文件夹、1文件)")
    private String fileType;

	/**
	 * 功能类型（0数据集、1模型、2项目、3实验）
	 */
	@Schema(description="功能类型（0数据集、1模型、2项目、3实验）")
	private String functionType;

    /**
    * 删除状态(0未删除、1已删除)
    */
    @Schema(description="删除状态(0未删除、1已删除)")
    private String status;

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
