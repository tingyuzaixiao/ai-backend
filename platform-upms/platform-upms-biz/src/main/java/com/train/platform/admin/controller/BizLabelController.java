package com.train.platform.admin.controller;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import com.train.platform.admin.api.dto.LabelDTO;
import com.train.platform.admin.api.entity.BizLabel;
import com.train.platform.admin.service.BizLabelService;
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
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/label")
@Tag(description = "label", name = "在线标注模块")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class BizLabelController {

	private final BizLabelService bizLabelService;

	/**
	 * 简单分页查询
	 *
	 * @param page     分页对象
	 * @param bizLabel 系统在线标注
	 * @return
	 */
	@GetMapping("/page")
	public R getLogPage(@ParameterObject Page page, @ParameterObject BizLabel bizLabel) {
		return R.ok(bizLabelService.getByPage(page, bizLabel));
	}

	/**
	 * 通过ID查询
	 *
	 * @param id ID
	 * @return BizLabel
	 */
	@Operation(summary = "通过ID查询在线标注", description = "通过ID查询在线标注")
	@GetMapping("/{id}")
	public R getById(@PathVariable Long id) {
		return R.ok(bizLabelService.getLabelById(id));
	}

	/**
	 * 查询在线标注信息
	 *
	 * @param query 查询条件
	 * @return 在线标注信息
	 */
	@Operation(summary = "查询在线标注信息", description = "查询在线标注信息")
	@GetMapping("/details")
	public R getDetails(@ParameterObject BizLabel query) {
		return R.ok(bizLabelService.getDetails(query));
	}

	/**
	 * 查询全部在线标注
	 */
	@Operation(summary = "查询全部在线标注", description = "查询全部在线标注")
	@GetMapping("/list")
	public R list() {
		return R.ok(bizLabelService.list());
	}

	/**
	 * 批量删除在线标注
	 *
	 * @param ids ID
	 * @return success/false
	 */
	@Operation(summary = "批量删除在线标注", description = "批量删除在线标注")
	@DeleteMapping
	@SysLog("删除在线标注")
//	@PreAuthorize("@pms.hasPermission('sys_label_del')")
	public R removeByIds(@RequestBody Long[] ids) {
		return R.ok(bizLabelService.removeBatchByIds(CollUtil.toList(ids)));
	}

	/**
	 * 插入标签组
	 *
	 * @param bizLabel 在线标注实体
	 * @return success/false
	 */
	@Operation(summary = "添加在线标注", description = "添加在线标注")
	@PostMapping("/save")
	@SysLog("添加在线标注")
	public R save(@RequestBody LabelDTO bizLabel) {
		return bizLabelService.saveLabel(bizLabel);
	}

	/**
	 * 批量插入在线标注
	 *
	 * @param bizLabel 在线标注实体
	 * @return success/false
	 */
	@Operation(summary = "批量添加在线标注", description = "批量添加在线标注")
	@PostMapping("/saveBatch")
	@SysLog("批量添加在线标注")
	public R saveBatch(@RequestBody LabelDTO bizLabel) {
		//批量保存分类时，保存到tagInfo表中
		return R.ok(bizLabelService.saveLabels(bizLabel));
	}

	/**
	 * 编辑
	 *
	 * @param bizLabel 实体
	 * @return success/false
	 */
	@Operation(summary = "编辑修改在线标注", description = "编辑修改在线标注")
	@SysLog("编辑修改在线标注")
	@PutMapping
//	@PreAuthorize("@pms.hasPermission('sys_label_edit')")
	public R update(@Valid @RequestBody LabelDTO bizLabel) {
		bizLabel.setUpdateTime(LocalDateTime.now());
		return bizLabelService.updateBizLabelById(bizLabel);
	}

	@Operation(summary = "同步标签", description = "同步标签")
	@PostMapping("/batchUpdate")
	public R batchUpdate(Long taskId, String labelType, String subLabelType) {
		return bizLabelService.syncLabel(taskId, labelType, subLabelType);
	}

	@PostMapping("autoAsyncLabel")
	public R autoAsyncLabel() {
		return bizLabelService.autoAsyncLabel();
	}

	/**
	 * 导出excel 表格
	 *
	 * @param bizLabel 查询条件
	 * @return
	 */
	@ResponseExcel
	@GetMapping("/export")
//	@PreAuthorize("@pms.hasPermission('sys_label_export')")
	public List<BizLabel> export(BizLabel bizLabel) {
		return bizLabelService.list(Wrappers.lambdaQuery(bizLabel));
	}
}
