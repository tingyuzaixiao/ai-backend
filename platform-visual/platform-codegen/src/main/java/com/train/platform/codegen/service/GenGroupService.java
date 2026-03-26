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

package com.train.platform.codegen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.train.platform.codegen.entity.GenGroupEntity;
import com.train.platform.codegen.util.vo.GroupVo;
import com.train.platform.codegen.util.vo.TemplateGroupDTO;

/**
 * 模板分组
 *
 * @author lee
 * @date 2024-02-21 20:01:53
 */
public interface GenGroupService extends IService<GenGroupEntity> {

	void saveGenGroup(TemplateGroupDTO genTemplateGroup);

	/**
	 * 删除分组关系
	 * @param ids
	 */
	void delGroupAndTemplate(Long[] ids);

	/**
	 * 查询group数据
	 * @param id
	 */
	GroupVo getGroupVoById(Long id);

	/**
	 * 更新group数据
	 * @param groupVo
	 */
	void updateGroupAndTemplateById(GroupVo groupVo);

}
