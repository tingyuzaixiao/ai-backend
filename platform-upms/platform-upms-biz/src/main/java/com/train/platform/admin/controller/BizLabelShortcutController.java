package com.train.platform.admin.controller;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import com.train.platform.admin.api.entity.BizLabelShortcut;
import com.train.platform.admin.service.BizLabelShortcutService;
import com.train.platform.common.core.util.R;
import com.train.platform.common.log.annotation.SysLog;
import com.train.platform.common.security.util.SecurityUtils;
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
@RequestMapping("/labelShortcut")
@Tag(description = "labelShortcut", name = "标签快捷键模块")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class BizLabelShortcutController {

	private final BizLabelShortcutService bizLabelShortcutService;

	/**
	 * 简单分页查询
	 *
	 * @param page             分页对象
	 * @param bizLabelShortcut 系统标签标签快捷键
	 * @return
	 */
	@GetMapping("/page")
	public R getShortcutPage(@ParameterObject Page page, @ParameterObject BizLabelShortcut bizLabelShortcut) {
		return R.ok(bizLabelShortcutService.getShortcutByPage(page, bizLabelShortcut));
	}

	/**
	 * 获取当前用户标注快捷键
	 *
	 * @return BizLabelShortcut
	 */
	@Operation(summary = "获取当前用户标注快捷键", description = "获取当前用户标注快捷键")
	@GetMapping
	@SysLog("获取当前用户标注快捷键")
	public R getCurrentUserShortcut() {
		return R.ok(bizLabelShortcutService.getOne(Wrappers.lambdaQuery(BizLabelShortcut.class).eq(BizLabelShortcut::getUserId, SecurityUtils.getUser().getId())));
	}

	/**
	 * 通过ID查询
	 *
	 * @param id ID
	 * @return BizLabelShortcut
	 */
	@Operation(summary = "通过ID查询标签快捷键", description = "通过ID查询标签快捷键")
	@GetMapping("/{id}")
	@SysLog("通过ID查询标签快捷键")
	public R getById(@PathVariable Long id) {
		return R.ok(bizLabelShortcutService.getLabelShortcutById(id));
	}

	/**
	 * 查询模型信息
	 *
	 * @param query 查询条件
	 * @return 标签快捷键信息
	 */
	@Operation(summary = "查询标签标签快捷键信息", description = "查询标签标签快捷键信息")
	@GetMapping("/details")
	public R getDetails(@ParameterObject BizLabelShortcut query) {
		return R.ok(bizLabelShortcutService.getOne(Wrappers.query(query), false));
	}

	/**
	 * 查询全部标签标签快捷键
	 */
	@Operation(summary = "查询全部标签标签快捷键", description = "查询全部标签标签快捷键")
	@GetMapping("/list")
	public R list() {
		return R.ok(bizLabelShortcutService.list());
	}

	/**
	 * 批量删除标签标签快捷键
	 *
	 * @param ids ID
	 * @return success/false
	 */
	@DeleteMapping
	@SysLog("删除标签标签快捷键")
//	@PreAuthorize("@pms.hasPermission('sys_label_group_del')")
	public R removeByIds(@RequestBody Long[] ids) {
		return bizLabelShortcutService.removeLabelShortcutByIds(CollUtil.toList(ids));
	}

	/**
	 * 插入标签标签快捷键
	 *
	 * @param bizLabelShortcut 标签标签快捷键实体
	 * @return success/false
	 */
	@Operation(summary = "添加标签标签快捷键", description = "添加标签标签快捷键")
	@PostMapping("/save")
	@SysLog("添加标签标签快捷键")
	public R save(@RequestBody BizLabelShortcut bizLabelShortcut) {
		bizLabelShortcut.setUserId(SecurityUtils.getUser().getId());
		//查询用户是否有多个快捷键
		BizLabelShortcut exist = bizLabelShortcutService.getOne(Wrappers.lambdaQuery(BizLabelShortcut.class).eq(BizLabelShortcut::getUserId, bizLabelShortcut.getUserId()));
		if (exist != null) {
			return R.failed("该用户已存在标注快捷键");
		}
		return bizLabelShortcutService.saveLabelShortcut(bizLabelShortcut);
	}

	/**
	 * 编辑
	 *
	 * @param bizLabelShortcut 实体
	 * @return success/false
	 */
	@Operation(summary = "编辑修改标签标签快捷键", description = "编辑修改标签标签快捷键")
	@SysLog("编辑修改标签标签快捷键")
	@PutMapping
//	@PreAuthorize("@pms.hasPermission('sys_label_group_edit')")
	public R update(@Valid @RequestBody BizLabelShortcut bizLabelShortcut) {
		return R.ok(bizLabelShortcutService.updateById(bizLabelShortcut));
	}

	/**
	 * 导出excel 表格
	 *
	 * @param bizLabelShortcut 查询条件
	 * @return
	 */
	@ResponseExcel
	@GetMapping("/export")
//	@PreAuthorize("@pms.hasPermission('sys_label_group_export')")
	public List<BizLabelShortcut> export(BizLabelShortcut bizLabelShortcut) {
		return bizLabelShortcutService.list(Wrappers.lambdaQuery(bizLabelShortcut));
	}
}
