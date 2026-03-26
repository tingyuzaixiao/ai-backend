/*
 *    Copyright (c) 2019-2025, lee All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the name of the platform4cloud.com developer nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Author: lee 
 */

package com.train.platform.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.train.platform.admin.api.entity.SysPost;
import com.train.platform.admin.api.vo.PostExcelVO;
import com.train.platform.common.core.util.R;
import org.springframework.validation.BindingResult;

import java.util.List;

/**
 * 岗位信息表
 *
 * @author fxz
 * @date 2024-03-26 12:50:43
 */
public interface SysPostService extends IService<SysPost> {

	/**
	 * 导出excel 表格
	 * @return
	 */
	List<PostExcelVO> listPost();

	/**
	 * 导入岗位
	 * @param excelVOList 岗位列表
	 * @param bindingResult 错误信息列表
	 * @return ok fail
	 */
	R importPost(List<PostExcelVO> excelVOList, BindingResult bindingResult);

}
