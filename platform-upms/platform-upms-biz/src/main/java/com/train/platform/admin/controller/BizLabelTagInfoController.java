package com.train.platform.admin.controller;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import com.train.platform.admin.api.entity.BizLabelTagInfo;
import com.train.platform.admin.service.BizLabelTagInfoService;
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

@RestController
@AllArgsConstructor
@RequestMapping("/labelTagInfo")
@Tag(description = "labelTagInfo", name = "标签信息模块")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class BizLabelTagInfoController {
	
	
	private final BizLabelTagInfoService bizLabelTagInfoService;

	/**
	 * 简单分页查询
	 *
	 * @param page        分页对象
	 * @param bizLabelTagInfo 系统标签信息
	 * @return
	 */
	@GetMapping("/page")
	public R getLogPage(@ParameterObject Page page, @ParameterObject BizLabelTagInfo bizLabelTagInfo) {
		return R.ok(bizLabelTagInfoService.page(page, Wrappers.lambdaQuery(bizLabelTagInfo)));
	}

	/**
	 * 通过ID查询
	 *
	 * @param id ID
	 * @return BizLabelTagInfo
	 */
	@Operation(summary = "通过ID查询标签信息", description = "通过ID查询标签信息")
	@GetMapping("/{id}")
	public R getById(@PathVariable Long id) {
		return R.ok(bizLabelTagInfoService.getById(id));
	}

	/**
	 * 查询标签信息
	 *
	 * @param query 查询条件
	 * @return 标签信息
	 */
	@Operation(summary = "查询标签信息", description = "查询标签信息")
	@GetMapping("/details")
	public R getDetails(@ParameterObject BizLabelTagInfo query) {
		return R.ok(bizLabelTagInfoService.getOne(Wrappers.query(query), false));
	}

	/**
	 * 查询全部标签信息
	 */
	@Operation(summary = "查询全部标签信息", description = "查询全部标签信息")
	@GetMapping("/list")
	public R list() {
		return R.ok(bizLabelTagInfoService.list());
	}

	/**
	 * 批量删除标签信息
	 *
	 * @param ids ID
	 * @return success/false
	 */
	@DeleteMapping
	@SysLog("删除标签信息")
//	@PreAuthorize("@pms.hasPermission('sys_label_tag_info_del')")
	public R removeByIds(@RequestBody Long[] ids) {
		return R.ok(bizLabelTagInfoService.removeByIds(CollUtil.toList(ids)));
	}

	/**
	 * 插入标签信息
	 *
	 * @param bizLabelTagInfo 标签信息实体
	 * @return success/false
	 */
	@Operation(summary = "添加标签信息", description = "添加标签信息")
	@PostMapping("/save")
	@SysLog("添加标签信息")
	public R save(@RequestBody BizLabelTagInfo bizLabelTagInfo) {
		return R.ok(bizLabelTagInfoService.save(bizLabelTagInfo));
	}

	/**
	 * 编辑
	 *
	 * @param bizLabelTagInfo 实体
	 * @return success/false
	 */
	@Operation(summary = "编辑修改标签信息", description = "编辑修改标签信息")
	@SysLog("编辑修改标签信息")
	@PutMapping
//	@PreAuthorize("@pms.hasPermission('sys_label_tag_info_edit')")
	public R update(@Valid @RequestBody BizLabelTagInfo bizLabelTagInfo) {
		return R.ok(bizLabelTagInfoService.updateById(bizLabelTagInfo));
	}

	/**
	 * 导出excel 表格
	 *
	 * @param bizLabelTagInfo 查询条件
	 * @return
	 */
	@ResponseExcel
	@GetMapping("/export")
//	@PreAuthorize("@pms.hasPermission('sys_label_tag_info_export')")
	public List<BizLabelTagInfo> export(BizLabelTagInfo bizLabelTagInfo) {
		return bizLabelTagInfoService.list(Wrappers.lambdaQuery(bizLabelTagInfo));
	}
}
