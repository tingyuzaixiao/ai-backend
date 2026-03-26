package com.train.platform.admin.controller;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import com.train.platform.admin.api.entity.BizLabelGroup;
import com.train.platform.admin.service.BizLabelGroupService;
import com.train.platform.common.core.util.R;
import com.train.platform.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/labelGroup")
@Tag(description = "labelGroup", name = "标签组模块")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class BizLabelGroupController {

	private final BizLabelGroupService bizLabelGroupService;

	/**
	 * 简单分页查询
	 * @param page 分页对象
	 * @param bizLabelGroup 系统标签组
	 * @return
	 */
	@GetMapping("/page")
	public R getGroupPage(@ParameterObject Page page, @ParameterObject BizLabelGroup bizLabelGroup) {
		return R.ok(bizLabelGroupService.getGroupByPage(page, bizLabelGroup));
	}

	/**
	 * 通过ID查询
	 *
	 * @param id ID
	 * @return BizLabelGroup
	 */
	@Operation(summary = "通过ID查询标签组", description = "通过ID查询标签组")
	@GetMapping("/{id}")
	@SysLog("查询标签组")
	public R getById(@PathVariable Long id) {
		return R.ok(bizLabelGroupService.getLabelGroupById(id));
	}

	/**
	 * 查询模型信息
	 *
	 * @param query 查询条件
	 * @return 标签组信息
	 */
	@Operation(summary = "查询标签组信息", description = "查询标签组信息")
	@GetMapping("/details")
	public R getDetails(@ParameterObject BizLabelGroup query) {
		return R.ok(bizLabelGroupService.getOne(Wrappers.query(query), false));
	}

	/**
	 * 查询全部基础模型
	 */
	@Operation(summary = "查询全部标签组", description = "查询全部标签组")
	@GetMapping("/list")
	public R list(@ParameterObject BizLabelGroup bizLabelGroup) {
		return R.ok(bizLabelGroupService.listLabelGroup(bizLabelGroup));
	}

	/**
	 * 批量删除标签组
	 * @param ids ID
	 * @return success/false
	 */
	@DeleteMapping
	@SysLog("删除标签组")
//	@PreAuthorize("@pms.hasPermission('sys_label_group_del')")
	public R removeByIds(@RequestBody Long[] ids) {
		return bizLabelGroupService.removeLabelGroupByIds(CollUtil.toList(ids));
	}

	/**
	 * 插入标签组
	 * @param bizLabelGroup 标签组实体
	 * @return success/false
	 */
	@Operation(summary = "添加标签组", description = "添加标签组")
	@PostMapping("/save")
	@SysLog("添加标签组")
	public R save(@RequestBody BizLabelGroup bizLabelGroup) {
		return bizLabelGroupService.saveLabelGroup(bizLabelGroup);
	}

	/**
	 * 复制标签组
	 * @param params 标签组
	 * @return success/false
	 */
	@Operation(summary = "复制标签组", description = "复制标签组")
	@PostMapping("/copy")
	@SysLog("复制标签组")
	public R copy(@RequestBody Map<String,Object> params) {
		if (params == null) {
			return R.failed("参数错误");
		}
		bizLabelGroupService.copy(params);
		return R.ok();
	}

	/**
	 * 编辑
	 *
	 * @param bizLabelGroup 实体
	 * @return success/false
	 */
	@Operation(summary = "编辑修改标签组", description = "编辑修改标签组")
	@SysLog("编辑修改标签组")
	@PutMapping
//	@PreAuthorize("@pms.hasPermission('sys_label_group_edit')")
	public R update(@Valid @RequestBody BizLabelGroup bizLabelGroup) {
		return R.ok(bizLabelGroupService.updateById(bizLabelGroup));
	}

	/**
	 * 导出excel 表格
	 * @param bizLabelGroup 查询条件
	 * @return
	 */
	@ResponseExcel
	@GetMapping("/export")
//	@PreAuthorize("@pms.hasPermission('sys_label_group_export')")
	public List<BizLabelGroup> export(BizLabelGroup bizLabelGroup) {
		return bizLabelGroupService.list(Wrappers.lambdaQuery(bizLabelGroup));
	}
}
