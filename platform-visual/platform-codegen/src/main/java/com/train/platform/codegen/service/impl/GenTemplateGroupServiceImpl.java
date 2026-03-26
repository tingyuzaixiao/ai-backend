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
package com.train.platform.codegen.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.train.platform.codegen.entity.GenTemplateGroupEntity;
import com.train.platform.codegen.mapper.GenTemplateGroupMapper;
import com.train.platform.codegen.service.GenTemplateGroupService;
import org.springframework.stereotype.Service;

/**
 * 模板分组关联表
 *
 * @author lee
 * @date 2024-02-22 09:25:15
 */
@Service
public class GenTemplateGroupServiceImpl extends ServiceImpl<GenTemplateGroupMapper, GenTemplateGroupEntity>
		implements GenTemplateGroupService {

}
