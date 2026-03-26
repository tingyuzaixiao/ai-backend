package com.train.platform.admin.api.entity;


import java.time.LocalDateTime;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
* 
* @TableName aircas_offline_path
*/
@Data
public class SysOfflinePath{
    /**
    * 主键
    */
    private Long id;

    /**
    * 离线类型（数据集、模型）
    */
    private String type;

    /**
    * 离线数据ids
    */
    @Length(max= 255,message="编码长度不能超过255")
    private String offlineIds;

    /**
    * 用户id
    */
    @Length(max= 32,message="编码长度不能超过32")
    private Long userId;

	/**
	 * 文件服务器IP
	 */
	private String serverIp;

	/**
	 * 文件服务器端口
	 */
	private String serverPort;

	/**
	 * 文件服务器uri
	 */
	private String serverUri;

    /**
    * 外网ip
    */
    @Length(max= 32,message="编码长度不能超过32")
    private String publicIp;

    /**
    * 本地ip
    */
    @Length(max= 32,message="编码长度不能超过32")
    private String localIp;

    /**
    * 本地路径
    */
    @Length(max= 255,message="编码长度不能超过255")
    private String localPath;

    /**
    * 操作系统
    */
    @Length(max= 64,message="编码长度不能超过64")
    private String osName;

	/**
	 * 协议
	 */
	private String protocol;

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
	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "创建时间")
	private LocalDateTime createTime;

	/**
	 * 更新时间
	 */
	@TableField(fill = FieldFill.UPDATE)
	@Schema(description = "更新时间")
	private LocalDateTime updateTime;

	/**
	 * 0--正常 1--删除
	 */
	@TableLogic
	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "删除标记,1:已删除,0:正常")
	private String delFlag;

	@Schema(description = "描述")
	private String description;
}
