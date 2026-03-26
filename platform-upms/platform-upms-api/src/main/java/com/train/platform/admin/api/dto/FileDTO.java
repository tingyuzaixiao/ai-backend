package com.train.platform.admin.api.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class FileDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 编号
	 */
	@TableId(type = IdType.AUTO)
	@Schema(description = "文件编号")
	private Long fileId;


	@Schema(description = "文件编号s")
	private List<Long> fileIds;

	/**
	 * 基础数据ID
	 */
	@Schema(description = "基础数据ID")
	private Long baseDatasetId;

	/**
	 * 文件名
	 */
	@Schema(description = "文件名")
	private String fileName;

	/**
	 * 原文件名
	 */
	@Schema(description = "原始文件名")
	private String original;

	/**
	 * 容器名称
	 */
	@Schema(description = "存储桶名称")
	private String bucketName;

	/**
	 * 文件类型
	 */
	@Schema(description = "文件类型")
	private String type;

	/**
	 * 标签文件名称
	 */
	@Schema(description = "标签文件名称")
	private String labelFileName;

	/**
	 * 标签id
	 */
	@Schema(description = "标签id")
	private Long tagId;

	/**
	 * 标签id
	 */
	@Schema(description = "标签任务id")
	private Long taskId;

	/**
	 * 标签状态
	 */
	@Schema(description = "标签状态")
	private String labelState;

	/**
	 * 是否标注
	 */
	@Schema(description = "是否标注")
	private Boolean isLabeled;

	/**
	 * 描述(备注)
	 */
	@Schema(description = "描述(备注)")
	private String description;
}
