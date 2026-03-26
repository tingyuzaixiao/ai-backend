package com.train.platform.admin.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.train.platform.admin.api.entity.BizBaseModel;
import com.train.platform.admin.api.entity.BizGpuResource;
import com.train.platform.admin.service.BizGpuResourceService;
import com.train.platform.common.core.util.R;
import com.train.platform.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * <p>
 * gpu资源管理 控制层
 * </p>
 *
 * @author zhouzhipeng
 * @since 2024-06-14
 */
@RestController
@AllArgsConstructor
@RequestMapping("/gpuResource")
@Tag(description = "gpuResource", name = "gpu资源模块")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class BizGpuResourceController {

	private final BizGpuResourceService bizGpuResourceService;

	/**
	 * 通过ID查询
	 *
	 * @param id ID
	 * @return BizGpuResource
	 */
	@Operation(summary = "通过ID查询gpu资源", description = "通过ID查询gpu资源")
	@GetMapping("/{id}")
	public R getById(@PathVariable Long id) {
		return R.ok(bizGpuResourceService.getById(id));
	}

	/**
	 * 查询全部gpu资源
	 */
	@Operation(summary = "查询全部gpu资源", description = "查询全部gpu资源")
	@GetMapping("/list")
	public R list() {
		return R.ok(bizGpuResourceService.list());
	}

	@Operation(summary = "分页查询", description = "分页查询")
	@GetMapping("/page")
	public R getGpuResourcePage(@RequestParam("current") Integer current,
								@RequestParam("size") Integer size,
								@ParameterObject BizGpuResource bizGpuResource) {
		return R.ok(bizGpuResourceService.getGpuPage(current, size, bizGpuResource));
	}

	/**
	 * 添加
	 *
	 * @param bizGpuResource 实体
	 * @return success/false
	 */
	@Operation(summary = "创建gpu资源", description = "创建gpu资源")
	@SysLog("创建gpu资源")
	@PostMapping
	public R save(@Valid @RequestBody BizGpuResource bizGpuResource) {
		return R.ok(bizGpuResourceService.save(bizGpuResource));
	}

	/**
	 * 删除
	 *
	 * @param id ID
	 * @return success/false
	 */
	@Operation(summary = "删除gpu资源", description = "删除gpu资源")
	@SysLog("删除gpu资源")
	@DeleteMapping("/delete/{id}")
	public R removeById(@PathVariable Long id) {
		return R.ok(bizGpuResourceService.removeById(id));
	}

	@DeleteMapping
	@PreAuthorize("@pms.hasPermission('sys_gpu_del')")
	public R removeByIds(@RequestBody Long[] ids) {
		return R.ok(bizGpuResourceService.removeBatchByIds(CollUtil.toList(ids)));
	}


	/**
	 * 编辑
	 *
	 * @param bizGpuResource 实体
	 * @return success/false
	 */
	@Operation(summary = "编辑gpu资源", description = "编辑gpu资源")
	@SysLog("编辑gpu资源")
	@PutMapping
	public R update(@Valid @RequestBody BizGpuResource bizGpuResource) {
		bizGpuResource.setUpdateTime(LocalDateTime.now());
		return R.ok(bizGpuResourceService.updateById(bizGpuResource));
	}

	/**
	 * 查询GPU资源信息
	 *
	 * @param query 查询条件
	 * @return GPU资源信息
	 */
	@Operation(summary = "查询GPU资源信息", description = "查询GPU资源信息")
	@GetMapping("/details")
	public R getDetails(@ParameterObject BizGpuResource query) {
		return R.ok(bizGpuResourceService.getOne(Wrappers.query(query), false));
	}

}
