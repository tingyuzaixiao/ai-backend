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

package com.train.platform.admin.api.feign;

import com.train.platform.admin.api.dto.UserDTO;
import com.train.platform.admin.api.dto.UserInfo;
import com.train.platform.common.core.constant.SecurityConstants;
import com.train.platform.common.core.constant.ServiceNameConstants;
import com.train.platform.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * @author lee
 * @date 2024/6/22
 */
@FeignClient(contextId = "remoteUserService", value = ServiceNameConstants.UPMS_SERVICE)
public interface RemoteUserService {

	/**
	 * 通过用户名查询用户、角色信息
	 * @param user 用户查询对象
	 * @param from 调用标志
	 * @return R
	 */
	@GetMapping("/user/info/query")
	R<UserInfo> info(@SpringQueryMap UserDTO user, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 锁定用户
	 * @param username 用户名
	 * @param from 调用标识
	 * @return
	 */
	@PutMapping("/user/lock/{username}")
	R<Boolean> lockUser(@PathVariable("username") String username, @RequestHeader(SecurityConstants.FROM) String from);

}
