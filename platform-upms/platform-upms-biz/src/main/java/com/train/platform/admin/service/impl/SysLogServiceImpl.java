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

package com.train.platform.admin.service.impl;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.train.platform.admin.api.dto.SysLogDTO;
import com.train.platform.admin.api.entity.SysLog;
import com.train.platform.admin.api.entity.SysUser;
import com.train.platform.admin.mapper.SysLogMapper;
import com.train.platform.admin.mapper.SysUserMapper;
import com.train.platform.admin.service.SysLogService;
import com.train.platform.common.log.util.LogTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 日志表 服务实现类
 * </p>
 *
 * @author lee
 * @since 2024-11-20
 */
@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements SysLogService {

	@Autowired
	private SysUserMapper sysUserMapper;

	@Override
	public Page getLogByPage(Page page, SysLogDTO sysLog) {
		return baseMapper.selectPage(page, buildQuery(sysLog));
	}

	/**
	 * 插入日志
	 * @param sysLog 日志对象
	 * @return true/false
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean saveLog(SysLog sysLog) {
		baseMapper.insert(sysLog);
		if (sysLog.getLogType().equals(LogTypeEnum.LOGIN.getType())) {
			sysUserMapper.update(Wrappers.<SysUser>lambdaUpdate().
					eq(SysUser::getUsername,sysLog.getCreateBy()).
					set(SysUser::getLastLoginTime, LocalDateTime.now()));
		}
		return Boolean.TRUE;
	}

	/**
	 * 查询日志列表
	 * @param sysLog 查询条件
	 * @return List<SysLog>
	 */
	@Override
	public List<SysLog> getList(SysLogDTO sysLog) {
		return baseMapper.selectList(buildQuery(sysLog));
	}

	/**
	 * 构建查询条件
	 * @param sysLog 前端条件
	 * @return LambdaQueryWrapper
	 */
	private LambdaQueryWrapper buildQuery(SysLogDTO sysLog) {
		LambdaQueryWrapper<SysLog> wrapper = Wrappers.lambdaQuery();
		if (StrUtil.isNotBlank(sysLog.getLogType())) {
			wrapper.eq(SysLog::getLogType, sysLog.getLogType());
		}

		if (ArrayUtil.isNotEmpty(sysLog.getCreateTime())) {
			wrapper.ge(SysLog::getCreateTime, sysLog.getCreateTime()[0])
				.le(SysLog::getCreateTime, sysLog.getCreateTime()[1]);
		}

		return wrapper;
	}

}
