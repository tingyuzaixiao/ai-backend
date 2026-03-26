package com.train.platform.admin.api.entity;


import java.util.Date;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件分片信息表，仅在手动分片上传时使用
 *
 * @TableName file_part_detail
 */
@Data
@Schema(description = "文件分片信息表，仅在手动分片上传时使用")
@EqualsAndHashCode(callSuper = true)
public class SysFilePartDetail extends Model<SysFilePartDetail> {

	/**
	 * 分片id
	 */
	@Schema(description = "分片id")
	private String id;
	/**
	 * 存储平台
	 */
	@Schema(description = "存储平台")
	private String platform;
	/**
	 * 上传ID，仅在手动分片上传时使用
	 */
	@Schema(description = "上传ID，仅在手动分片上传时使用")
	private String uploadId;
	/**
	 * 分片 ETag
	 */
	@Schema(description = "分片 ETag")
	private String eTag;
	/**
	 * 分片号。每一个上传的分片都有一个分片号，一般情况下取值范围是1~10000
	 */
	@Schema(description = "分片号。每一个上传的分片都有一个分片号，一般情况下取值范围是1~10000")
	private Integer partNumber;
	/**
	 * 文件大小，单位字节
	 */
	@Schema(description = "文件大小，单位字节")
	private Long partSize;
	/**
	 * 哈希信息
	 */
	@Schema(description = "哈希信息")
	private String hashInfo;
	/**
	 * 创建时间
	 */
	@Schema(description = "创建时间")
	private Date createTime;

	public static String COL_UPLOAD_ID = "upload_id";

}
