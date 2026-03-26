package com.train.platform.admin.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;


@Data
public class LabelFileVO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "标注任务ID")
//	@ExcelProperty("标注任务ID")
	private Long labelId;


	/**
	 * 编号
	 */
	@Schema(description = "文件编号")
//	@ExcelProperty("文件编号")
	private Long fileId;

	/**
	 * 文件名
	 */
	@Schema(description = "文件名")
//	@ExcelProperty("文件名")
	private String fileName;

	/**
	 * 原文件名
	 */
	@Schema(description = "原文件名")
//	@ExcelProperty("原文件名")
	private String original;

	/**
	 * 容器名称
	 */
	@Schema(description = "存储桶名称")
//	@ExcelProperty("存储桶名称")
	private String bucketName;

	/**
	 * 文件名
	 */
	@Schema(description = "md5")
//	@ExcelProperty("md5")
	private String md5;

	/**
	 * 文件类型
	 */
	@Schema(description = "文件类型")
//	@ExcelProperty("文件类型")
	private String type;

	/**
	 * 文件大小
	 */
	@Schema(description = "文件大小")
//	@ExcelProperty("文件大小")
	private Long fileSize;

	/**
	 * 图像宽
	 */
	@Schema(description = "图像高")
//	@ExcelProperty("文件大小")
	private Integer height;


	/**
	 * 图像宽
	 */
	@Schema(description = "图像宽")
//	@ExcelProperty("文件大小")
	private Integer width;

	/**
	 * 标签
	 */
	@Schema(description = "标签ID")
//	@ExcelProperty("标签ID")
	private String tagId;

	/**
	 * 标签
	 */
//	@Schema(description = "标签信息")
//	@ExcelProperty("标签信息")
//	private String tagInfo;


	/**
	 * 上传时间
	 */
	@Schema(description = "标注时间")
//	@ExcelProperty("标注时间")
	private Date createTime;

	/**
	 * 上传时间
	 */
	@Schema(description = "标注人")
//	@ExcelProperty("标注人")
	private String createBy;

	/**
	 * 更新时间
	 */
	@Schema(description = "更新时间")
//	@ExcelProperty("更新时间")
	private Date updateTime;

	/**
	 * 上传时间
	 */
	@Schema(description = "更新人")
//	@ExcelProperty("更新人")
	private String updateBy;

	@Schema(description = "标签信息")
	private List<LabelTagInfoVO> tagInfoList;
}
