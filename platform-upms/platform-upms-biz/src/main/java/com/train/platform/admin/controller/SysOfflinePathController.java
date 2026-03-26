package com.train.platform.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.train.platform.admin.api.entity.SysOfflinePath;
import com.train.platform.admin.service.SysOfflinePathService;
import com.train.platform.common.core.util.R;
import com.train.platform.common.log.annotation.SysLog;
import com.train.platform.common.security.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;


/**
 * 文件管理
 *
 * @author Lee
 * @date 2025-06-17 17:18:42
 */
@RestController
@AllArgsConstructor
@RequestMapping("/sys-file-offline")
@Tag(description = "sys-file-offline", name = "文件离线管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class SysOfflinePathController {

	private final SysOfflinePathService sysOfflinePathService;

	/**
	 * 分页查询
	 *
	 * @param page           分页对象
	 * @param sysOfflinePath 文件管理
	 * @return
	 */
	@Operation(summary = "分页查询", description = "分页查询")
	@GetMapping("/page")
	public R getSysFilePage(@ParameterObject Page page, @ParameterObject SysOfflinePath sysOfflinePath) {
		Long userId = SecurityUtils.getUser().getId();
		return R.ok(sysOfflinePathService.page(page, Wrappers.<SysOfflinePath>lambdaQuery()
				.eq(SysOfflinePath::getUserId, userId)
				.like(sysOfflinePath.getLocalPath() != null && !sysOfflinePath.getLocalPath().isEmpty(), SysOfflinePath::getLocalPath, sysOfflinePath.getLocalPath())));
	}

	@Operation(summary = "通过ids删除文件管理", description = "通过ids删除文件管理")
	@SysLog("批量删除文件")
	@DeleteMapping
	@PreAuthorize("@pms.hasPermission('sys_file_del')")
	public R removeById(@RequestBody Long[] ids) {
		for (Long id : ids) {
			sysOfflinePathService.removeById(id);
		}
		return R.ok();
	}

	@Operation(summary = "查询文件信息", description = "查询文件信息")
	@SneakyThrows
	@GetMapping("/details")
	public R getDetails(@ParameterObject SysOfflinePath query) {
		return R.ok(sysOfflinePathService.getOne(Wrappers.query(query), false));
	}

	/**
	 * 添加
	 *
	 * @param
	 * @return success/false
	 */
	@Operation(summary = "添加离线目录", description = "添加离线目录")
	@SysLog("添加离线目录")
	@PostMapping
	public R save(@RequestBody SysOfflinePath sysOfflinePath) {
		sysOfflinePath.setUserId(SecurityUtils.getUser().getId());
		return sysOfflinePathService.saveOfflinePath(sysOfflinePath);
	}

	@Operation(summary = "查询离线目录", description = "查询离线目录")
	@GetMapping("/{id}")
	public R save(@PathVariable Long id) {
		return R.ok(sysOfflinePathService.getById(id));
	}

	/**
	 * 编辑
	 *
	 * @param sysOfflinePath 实体
	 * @return success/false
	 */
	@Operation(summary = "编辑修改离线目录", description = "编辑修改离线目录")
	@SysLog("编辑修改离线目录")
	@PutMapping
	public R update(@RequestBody SysOfflinePath sysOfflinePath) {
		return R.ok(sysOfflinePathService.updateById(sysOfflinePath));
	}

	/**
	 * 查询当前用户离线目录
	 */
	@Operation(summary = "查询当前用户离线目录", description = "查询当前用户离线目录")
	@GetMapping("/list")
	public R list(@RequestParam Map<String, Object> params) {
		Long userId = SecurityUtils.getUser().getId();
		return R.ok(sysOfflinePathService.list(Wrappers.<SysOfflinePath>lambdaQuery()
				.eq(SysOfflinePath::getUserId, userId).like(SysOfflinePath::getLocalPath, params.get("localPath").toString())));
	}

	@Operation(summary = "根据类型查询当前用户离线目录", description = "根据类型查询当前用户离线目录")
	@GetMapping("/currentUserOfflinePath/{type}")
	public R getCurrentUserOfflinePath(@PathVariable String type) {
		Long userId = SecurityUtils.getUser().getId();
		return R.ok(sysOfflinePathService.getOne(Wrappers.<SysOfflinePath>lambdaQuery()
				.eq(SysOfflinePath::getUserId, userId).eq(SysOfflinePath::getType, type)));
	}

	@GetMapping("/download")
	public void download(@ParameterObject SysOfflinePath sysOfflinePath, HttpServletResponse response) {
		try {
			sysOfflinePathService.download(sysOfflinePath, response);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
