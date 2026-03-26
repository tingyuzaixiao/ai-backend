package com.train.platform.admin.api.vo;


import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class FileVO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 编号
	 */
	@TableId(type = IdType.AUTO)
	@Schema(description = "文件编号")
	@ExcelProperty("文件编号")
	private Long fileId;

	/**
	 * 基础数据ID
	 */
	@Schema(description = "基础数据ID")
	@ExcelProperty("基础数据ID")
	private Long baseDatasetId;

	/**
	 * 文件名
	 */
	@Schema(description = "文件名")
	@ExcelProperty("文件名")
	private String fileName;

	/**
	 * 文件名
	 */
	@Schema(description = "md5")
	@ExcelProperty("md5")
	private String md5;

	/**
	 * 原文件名
	 */
	@Schema(description = "原始文件名")
	@ExcelProperty("原始文件名")
	private String original;

	/**
	 * 容器名称
	 */
	@Schema(description = "存储桶名称")
	@ExcelProperty("存储桶名称")
	private String bucketName;

	/**
	 * 文件类型
	 */
	@Schema(description = "文件类型")
	@ExcelProperty("文件类型")
	private String type;

	/**
	 * 文件大小
	 */
	@Schema(description = "文件大小")
	@ExcelProperty("文件大小")
	private Long fileSize;

	/**
	 * 文件大小
	 */
	@Schema(description = "文件长")
	@ExcelProperty("文件长")
	private Integer height;

	/**
	 * 文件大小
	 */
	@Schema(description = "文件宽")
	@ExcelProperty("文件宽")
	private Integer width;
	
	/**
	 * 上传时间
	 */
	@Schema(description = "上传时间")
	@ExcelProperty("上传时间")
	private String createTime;

	/**
	 * 标注ID
	 */
	@Schema(description = "标注ID")
	private Long labelId;

	/**
	 * 标注状态
	 */
	@Schema(description = "标注状态")
	private String state;

	/**
	 * 标签ID
	 */
	@Schema(description = "标签ID")
	private String tagId;

//	private String tagInfo;

	/**
	 * 标签信息列表
	 */
	@Schema(description = "标签信息列表")
	private List<LabelTagInfoVO> tagInfoList;

}
