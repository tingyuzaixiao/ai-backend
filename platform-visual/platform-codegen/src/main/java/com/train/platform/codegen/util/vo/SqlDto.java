package com.train.platform.codegen.util.vo;

import lombok.Data;

/**
 * @author lee
 * @date 2024/5/2
 */
@Data
public class SqlDto {

	/**
	 * 数据源ID
	 */
	private String dsName;

	/**
	 * sql脚本
	 */
	private String sql;

}
