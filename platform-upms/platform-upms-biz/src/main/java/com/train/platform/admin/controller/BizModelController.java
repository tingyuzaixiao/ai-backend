package com.train.platform.admin.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.train.platform.admin.api.dto.ModelDTO;
import com.train.platform.admin.api.entity.BizModel;
import com.train.platform.admin.service.BizModelService;
import com.train.platform.common.core.util.R;
import com.train.platform.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * <p>
 * 模型管理 控制层
 * </p>
 *
 * @author zhouzhipeng
 * @since 2024-01-20
 */
@RestController
@AllArgsConstructor
@RequestMapping("/model")
@Tag(description = "model", name = "模型模块")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class BizModelController {

	private final BizModelService bizModelService;

	/**
	 * 通过ID查询
	 *
	 * @param id ID
	 * @return BizModel
	 */
	@Operation(summary = "通过ID查询模型", description = "通过ID查询模型")
	@GetMapping("/{id}")
	public R getById(@PathVariable Long id) {
		return R.ok(bizModelService.getById(id));
	}

	/**
	 * 查询模型信息
	 *
	 * @param query 查询条件
	 * @return 模型信息
	 */
	@Operation(summary = "查询模型信息", description = "查询模型信息")
	@GetMapping("/details")
	public R getDetails(@ParameterObject BizModel query) {
		return R.ok(bizModelService.getOne(Wrappers.query(query), false));
	}

	/**
	 * 查询全部基础模型
	 */
	@Operation(summary = "查询全部模型", description = "查询全部模型")
	@GetMapping("/list")
	public R list() {
		return R.ok(bizModelService.list());
	}

	@Operation(summary = "分页查询", description = "分页查询")
	@GetMapping("/page")
	public R getTrainModelPage(@ParameterObject Page page, @ParameterObject BizModel bizModel) {
		return R.ok(bizModelService.getModelPage(page, bizModel));
	}

	/**
	 * 添加
	 *
	 * @param modelDTO 实体
	 * @return success/false
	 */
	@Operation(summary = "创建模型", description = "创建模型")
	@SysLog("创建模型")
	@PostMapping
	@PreAuthorize("@pms.hasPermission('sys_model_add')")
	public R save(@Valid @RequestBody ModelDTO modelDTO) {
		return bizModelService.saveModel(modelDTO);
	}

	/**
	 * 删除
	 *
	 * @param ids IDs
	 * @return success/false
	 */
	@Operation(summary = "删除模型", description = "删除模型")
	@SysLog("删除模型")
	@DeleteMapping()
	@PreAuthorize("@pms.hasPermission('sys_model_del')")
	public R removeByIds(@RequestBody Long[] ids) {
		return bizModelService.deleteModelByIds(ids);
	}

	/**
	 * 编辑
	 *
	 * @param bizModel 实体
	 * @return success/false
	 */
	@Operation(summary = "编辑修改模型", description = "编辑修改模型")
	@SysLog("编辑修改模型")
	@PutMapping
	@PreAuthorize("@pms.hasPermission('sys_model_edit')")
	public R update(@Valid @RequestBody BizModel bizModel) {
		bizModel.setUpdateTime(LocalDateTime.now());
		return bizModelService.updateModelById(bizModel);
	}

	@Operation(summary = "模型权重下载", description = "模型权重下载")
	@SysLog("模型权重下载")
	@GetMapping("/download/{filename}")
	public ResponseEntity<Resource> downloadFile(@PathVariable String filename, HttpServletRequest request) {
		return bizModelService.downloadFile(filename, request);
	}

	@Operation(summary = "根据模型ID查询模型配置", description = "根据模型ID查询模型配置")
	@GetMapping("/getModelConfig")
	@PreAuthorize("@pms.hasPermission('sys_model_config')")
	public R getModelConfig(@RequestParam Long id,
							@RequestParam(required = false) Boolean isBaseModel,
							@RequestParam String configType,
							@RequestParam(required = false) String modelClass) {
		return R.ok(bizModelService.getModelConfigByModelId(id, isBaseModel, configType, modelClass));
	}

	@Operation(summary = "根据模型ID修改模型配置", description = "根据模型ID修改模型配置")
	@PostMapping("/updateModelConfig")
	@PreAuthorize("@pms.hasPermission('sys_model_config')")
	public R updateModelConfig(@RequestBody ModelDTO modelDTO) {
		bizModelService.updateModelConfigByModelId(modelDTO);
		return R.ok();
	}
}
