/*
 *
 *      Copyright (c) 2019-2025, lee All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the platform4cloud.com developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: lee 
 *
 */

package com.train.platform.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.train.platform.admin.api.entity.BizBaseDataset;
import com.train.platform.admin.api.entity.BizBaseModel;
import com.train.platform.admin.api.vo.BaseModelVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 基础模型管理 Mapper 接口
 * </p>
 *
 * @author zhouzhipeng
 * @since 2024-01-20
 */
@Mapper
public interface BizBaseModelMapper extends BaseMapper<BizBaseModel> {

	Long insertBizBaseModel(BizBaseModel bizBaseModel);

	List<BaseModelVO> selectListGroup(@Param("query") BizBaseModel bizBaseModel);

}
