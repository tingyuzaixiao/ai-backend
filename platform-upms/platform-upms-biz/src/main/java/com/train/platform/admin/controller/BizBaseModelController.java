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

package com.train.platform.admin.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.aliyuncs.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.train.platform.admin.api.dto.BaseModelDTO;
import com.train.platform.admin.api.dto.ModelDTO;
import com.train.platform.admin.api.entity.BizBaseModel;
import com.train.platform.admin.api.entity.BizModel;
import com.train.platform.admin.service.BizBaseModelService;
import com.train.platform.common.core.util.FileUtils;
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
import java.util.HashMap;
import java.util.Map;

import static com.train.platform.common.core.constant.CommonConstants.*;
import static com.train.platform.common.core.constant.CommonConstants.TRAIN_CONFIG_JSON;

/**
 * <p>
 * 基础模型
 * </p>
 *
 * @author zhouzhipeng
 * @since 2024-01-20
 */
@RestController
@AllArgsConstructor
@RequestMapping("/baseModel")
@Tag(description = "baseModel", name = "基础模型模块")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class BizBaseModelController {

	private final BizBaseModelService bizBaseModelService;

	/**
	 * 通过ID查询
	 * @param id ID
	 * @return BizBaseModel
	 */
	@GetMapping("/{id}")
	public R getById(@PathVariable Long id) {
		return R.ok(bizBaseModelService.getById(id));
	}

	/**
	 * 查询全部基础模型
	 */
	@GetMapping("/list")
	public R list() {
		return R.ok(bizBaseModelService.list());
	}

	@Operation(summary = "分页查询", description = "分页查询")
	@GetMapping("/page")
	public R getBaseTrainModelPage(@ParameterObject Page page, @ParameterObject BizBaseModel bizBaseModel) {
		LambdaQueryWrapper<BizBaseModel> wrapper = Wrappers.<BizBaseModel>lambdaQuery()
			.like(StrUtil.isNotBlank(bizBaseModel.getBaseModelName()), BizBaseModel::getBaseModelName,
					bizBaseModel.getBaseModelName());
		return R.ok(bizBaseModelService.page(page, wrapper));
	}

	/**
	 * 添加
	 * @param
	 * @return success/false
	 */
	@Operation(summary = "添加基础模型", description = "添加基础模型")
	@SysLog("添加基础模型")
	@PostMapping
	@PreAuthorize("@pms.hasPermission('sys_basemodel_add')")
	public R save(@Valid @RequestBody BizBaseModel bizBaseModel) {
		bizBaseModelService.saveModel(bizBaseModel);
		return R.ok();
	}

	/**
	 * 删除
	 * @param id ID
	 * @return success/false
	 */
	@Operation(summary = "删除基础模型", description = "删除基础模型")
	@SysLog("删除基础模型")
	@DeleteMapping("/delete/{id}")
	@PreAuthorize("@pms.hasPermission('sys_basemodel_del')")
	public R removeById(@PathVariable Long id) {
		return bizBaseModelService.deleteBaseModelById(id);
	}


	@Operation(summary = "批量删除基础模型", description = "批量删除基础模型")
	@SysLog("批量删除基础模型")
	@DeleteMapping()
	@PreAuthorize("@pms.hasPermission('sys_basemodel_del')")
	public R removeByIds(@RequestBody Long[] ids) {
		return bizBaseModelService.deleteModelByIds(ids);
	}

	/**
	 * 编辑
	 * @param bizBaseModel 实体
	 * @return success/false
	 */
	@Operation(summary = "编辑基础模型", description = "编辑基础模型")
	@SysLog("编辑基础模型")
	@PutMapping
	@PreAuthorize("@pms.hasPermission('sys_basemodel_edit')")
	public R update(@Valid @RequestBody BizBaseModel bizBaseModel) {
		bizBaseModel.setUpdateTime(LocalDateTime.now());
		return bizBaseModelService.updateBaseModelById(bizBaseModel);
	}

	@Operation(summary = "模型分组列表", description = "模型分组列表")
	@PostMapping("/listGroup")
	public R listGroup(@RequestBody(required = false) BizBaseModel bizBaseModel) {
		return R.ok(bizBaseModelService.listGroup(bizBaseModel));
	}

	/**
	 * 查询模型信息
	 *
	 * @param query 查询条件
	 * @return 基础模型信息
	 */
	@Operation(summary = "查询基础模型信息", description = "查询基础模型信息")
	@GetMapping("/details")
	public R getDetails(@ParameterObject BizBaseModel query) {
		return R.ok(bizBaseModelService.getOne(Wrappers.query(query), false));
	}

	/**
	 * 更新基础模型配置文件
	 * @param bizBaseModel
	 * @return success/false
	 */
	@Operation(summary = "从算法服务器拉取最新基础模型配置文件", description = "从算法服务器拉取最新基础模型配置文件")
	@SysLog("从算法服务器拉取最新基础模型配置文件")
	@PostMapping("/updateBaseModelConfigFile")
	@PreAuthorize("@pms.hasPermission('sys_basemodel_online_update')")
	public R updateBaseModelConfigFile(@RequestBody(required = false) BizBaseModel bizBaseModel) {
		return R.ok(bizBaseModelService.updateBaseModelConfigFile(bizBaseModel));
	}

	/**
	 * 根据基础模型ID查询模型配置
	 * @param id 基础模型ID
	 * @return
	 */
	@Operation(summary = "根据基础模型ID查询模型配置参数", description = "根据基础模型ID查询模型配置参数")
	@SysLog("根据基础模型ID查询模型配置")
	@GetMapping("/getBaseModelConfig")
	@PreAuthorize("@pms.hasPermission('sys_basemodel_config')")
	public R getBaseModelConfig(@RequestParam String id) {
		return bizBaseModelService.getBaseModelConfig(Long.parseLong(id));
	}

	@Operation(summary = "修改基础模型配置参数", description = "修改基础模型配置参数")
	@SysLog("修改基础模型配置参数")
	@PostMapping("/updateBaseModelConfig")
	@PreAuthorize("@pms.hasPermission('sys_basemodel_cupdate')")
	public R updateBaseModelConfig(@RequestBody(required = false) BaseModelDTO baseModelDTO) {
		bizBaseModelService.updateBaseModelConfig(baseModelDTO);
		return R.ok();
	}
}
