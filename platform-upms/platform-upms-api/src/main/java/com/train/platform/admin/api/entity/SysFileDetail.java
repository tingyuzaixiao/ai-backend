package com.train.platform.admin.api.entity;


import java.util.Date;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件记录表
 *
 * @TableName file_detail
 */
@Data
@Schema(description = "文件详情")
@EqualsAndHashCode(callSuper = true)
public class SysFileDetail extends Model<SysFileDetail> {

	/**
	 * 文件id
	 */
	@Schema(description = "文件id")
	private String id;
	/**
	 * 文件访问地址
	 */
	@Schema(description = "文件访问地址")
	private String url;
	/**
	 * 文件大小，单位字节
	 */
	@Schema(description = "文件大小，单位字节")
	private Long size;
	/**
	 * 文件名称
	 */
	@Schema(description = "文件名称")
	private String filename;
	/**
	 * 原始文件名
	 */
	@Schema(description = "原始文件名")
	private String originalFilename;
	/**
	 * 基础存储路径
	 */
	@Schema(description = "基础存储路径")
	private String basePath;
	/**
	 * 存储路径
	 */
	@Schema(description = "存储路径")
	private String path;
	/**
	 * 文件扩展名
	 */
	@Schema(description = "文件扩展名")
	private String ext;
	/**
	 * MIME类型
	 */
	@Schema(description = "MIME类型")
	private String contentType;
	/**
	 * 存储平台
	 */
	@Schema(description = "存储平台")
	private String platform;
	/**
	 * 缩略图访问路径
	 */
	@Schema(description = "缩略图访问路径")
	private String thUrl;
	/**
	 * 缩略图名称
	 */
	@Schema(description = "缩略图名称")
	private String thFilename;
	/**
	 * 缩略图大小，单位字节
	 */
	@Schema(description = "缩略图大小，单位字节")
	private Long thSize;
	/**
	 * 缩略图MIME类型
	 */
	@Schema(description = "缩略图MIME类型")
	private String thContentType;
	/**
	 * 文件所属对象id
	 */
	@Schema(description = "文件所属对象id")
	private String objectId;
	/**
	 * 文件所属对象类型，例如用户头像，评价图片
	 */
	@Schema(description = "文件所属对象类型，例如用户头像，评价图片")
	private String objectType;
	/**
	 * 文件元数据
	 */
	@Schema(description = "文件元数据")
	private String metadata;
	/**
	 * 文件用户元数据
	 */
	@Schema(description = "文件用户元数据")
	private String userMetadata;
	/**
	 * 缩略图元数据
	 */
	@Schema(description = "缩略图元数据")
	private String thMetadata;
	/**
	 * 缩略图用户元数据
	 */
	@Schema(description = "缩略图用户元数据")
	private String thUserMetadata;
	/**
	 * 附加属性
	 */
	@Schema(description = "附加属性")
	private String attr;
	/**
	 * 文件ACL
	 */
	@Schema(description = "文件ACL")
	private String fileAcl;
	/**
	 * 缩略图文件ACL
	 */
	@Schema(description = "缩略图文件ACL")
	private String thFileAcl;
	/**
	 * 哈希信息
	 */
	@Schema(description = "哈希信息")
	private String hashInfo;
	/**
	 * 上传ID，仅在手动分片上传时使用
	 */
	@Schema(description = "上传ID，仅在手动分片上传时使用")
	private String uploadId;
	/**
	 * 上传状态，仅在手动分片上传时使用，1：初始化完成，2：上传完成
	 */
	@Schema(description = "上传状态，仅在手动分片上传时使用，1：初始化完成，2：上传完成")
	private Integer uploadStatus;
	/**
	 * 创建时间
	 */
	@Schema(description = "创建时间")
	private Date createTime;

	public static String COL_URL = "url";

	public static String COL_ID = "id";
}
