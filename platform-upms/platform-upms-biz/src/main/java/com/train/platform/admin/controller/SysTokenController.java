package com.train.platform.admin.controller;

import com.train.platform.admin.api.feign.RemoteTokenService;
import com.train.platform.common.core.constant.SecurityConstants;
import com.train.platform.common.core.util.R;
import com.train.platform.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author lee
 * @date 2024/9/4 getTokenPage 管理
 */
@RestController
@AllArgsConstructor
@RequestMapping("/sys-token")
@Tag(description = "token", name = "令牌管理模块")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class SysTokenController {

	private final RemoteTokenService remoteTokenService;

	/**
	 * 分页token 信息
	 * @param params 参数集
	 * @return token集合
	 */
	@PostMapping("/page")
	public R getTokenPage(@RequestBody Map<String, Object> params) {
		return remoteTokenService.getTokenPage(params, SecurityConstants.FROM_IN);
	}

	/**
	 * 删除
	 * @param tokens tokens
	 * @return success/false
	 */
	@SysLog("删除用户token")
	@DeleteMapping("/delete")
	@PreAuthorize("@pms.hasPermission('sys_token_del')")
	public R removeById(@RequestBody String[] tokens) {
		for (String token : tokens) {
			remoteTokenService.removeTokenById(token, SecurityConstants.FROM_IN);
		}
		return R.ok();
	}

}
