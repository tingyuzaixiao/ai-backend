package com.train.platform.admin.controller;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import com.train.platform.admin.api.dto.LabelTagDTO;
import com.train.platform.admin.api.entity.BizLabelTag;
import com.train.platform.admin.service.BizLabelTagService;
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

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/labelTag")
@Tag(description = "labelTag", name = "标签模块")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class BizLabelTagController {

	private final BizLabelTagService bizLabelTagService;

	/**
	 * 简单分页查询
	 *
	 * @param page        分页对象
	 * @param bizLabelTag 系统标签组
	 * @return
	 */
	@GetMapping("/page")
	public R getLogPage(@ParameterObject Page page, @ParameterObject BizLabelTag bizLabelTag) {
		return R.ok(bizLabelTagService.getLabelTagByPage(page, bizLabelTag));
	}

	/**
	 * 通过ID查询
	 *
	 * @param id ID
	 * @return BizLabelTag
	 */
	@Operation(summary = "通过ID查询标签", description = "通过ID查询标签")
	@GetMapping("/{id}")
	public R getById(@PathVariable Long id) {
		return R.ok(bizLabelTagService.getById(id));
	}

	/**
	 * 查询标签信息
	 *
	 * @param query 查询条件
	 * @return 标签信息
	 */
	@Operation(summary = "查询标签信息", description = "查询标签信息")
	@GetMapping("/details")
	public R getDetails(@ParameterObject BizLabelTag query) {
		return R.ok(bizLabelTagService.getOne(Wrappers.query(query), false));
	}

	/**
	 * 查询全部标签
	 */
	@Operation(summary = "查询全部标签", description = "查询全部标签")
	@GetMapping("/list")
	public R list(@ParameterObject BizLabelTag query) {
		return R.ok(bizLabelTagService.list(Wrappers.query(query)));
	}

	/**
	 * 批量删除标签组
	 *
	 * @param ids ID
	 * @return success/false
	 */
	@DeleteMapping
	@SysLog("删除标签")
//	@PreAuthorize("@pms.hasPermission('sys_label_del')")
	public R removeByIds(@RequestBody Long[] ids) {
		return bizLabelTagService.removeLabelTagByIds(CollUtil.toList(ids));
	}

	/**
	 * 插入标签组
	 *
	 * @param bizLabelTag 标签组实体
	 * @return success/false
	 */
	@Operation(summary = "添加标签", description = "添加标签")
	@PostMapping("/save")
	@SysLog("添加标签")
	public R save(@RequestBody BizLabelTag bizLabelTag) {
		return bizLabelTagService.saveLabelTag(bizLabelTag);
	}

	/**
	 * 编辑
	 *
	 * @param labelTagDTO 实体
	 * @return success/false
	 */
	@Operation(summary = "编辑修改标签", description = "编辑修改标签")
	@SysLog("编辑修改标签")
	@PutMapping
//	@PreAuthorize("@pms.hasPermission('sys_label_tag_edit')")
	public R update(@Valid @RequestBody LabelTagDTO labelTagDTO) {
		return R.ok(bizLabelTagService.updateLabelTagById(labelTagDTO));
	}

	/**
	 * 导出excel 表格
	 *
	 * @param bizLabelTag 查询条件
	 * @return
	 */
	@ResponseExcel
	@GetMapping("/export")
//	@PreAuthorize("@pms.hasPermission('sys_label_tag_export')")
	public List<BizLabelTag> export(BizLabelTag bizLabelTag) {
		return bizLabelTagService.list(Wrappers.lambdaQuery(bizLabelTag));
	}
}
