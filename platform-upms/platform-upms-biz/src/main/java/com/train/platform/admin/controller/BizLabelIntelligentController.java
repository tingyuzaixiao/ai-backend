package com.train.platform.admin.controller;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.train.platform.admin.api.entity.BizLabelIntelligent;
import com.train.platform.admin.service.BizLabelIntelligentService;
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

import java.time.LocalDateTime;

@RestController
@AllArgsConstructor
@RequestMapping("/labelIntelligent")
@Tag(description = "label-intelligent", name = "智能标注模块")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class BizLabelIntelligentController {

	private final BizLabelIntelligentService bizLabelIntelligentService;

	/**
	 * 简单分页查询
	 *
	 * @param page         分页对象
	 * @param bizLabelIntelligent 系统智能标注
	 * @return 分页
	 */
	@Operation(summary = "智能标注分页", description = "智能标注分页")
	@GetMapping("/page")
	public R getLabelIntelligentPage(@ParameterObject Page page, @ParameterObject BizLabelIntelligent bizLabelIntelligent) {
		return R.ok(bizLabelIntelligentService.page(page, Wrappers.lambdaQuery(bizLabelIntelligent)));
	}

	/**
	 * 通过ID查询
	 *
	 * @param id ID
	 * @return bizLabelIntelligent 智能标注信息
	 */
	@Operation(summary = "通过ID查询智能标注", description = "通过ID查询智能标注")
	@GetMapping("/{id}")
	public R getById(@PathVariable Long id) {
		return R.ok(bizLabelIntelligentService.getById(id));
	}

	/**
	 * 查询智能标注信息
	 *
	 * @param query 查询条件
	 * @return 智能标注信息
	 */
	@Operation(summary = "查询智能标注信息", description = "查询智能标注信息")
	@GetMapping("/details")
	public R getDetails(@ParameterObject BizLabelIntelligent query) {
		return R.ok(bizLabelIntelligentService.getOne(Wrappers.query(query), false));
	}

	/**
	 * 查询全部智能标注
	 */
	@Operation(summary = "查询全部智能标注", description = "查询全部智能标注")
	@GetMapping("/list")
	public R list() {
		return R.ok(bizLabelIntelligentService.list());
	}
	

	/**
	 * 批量删除标签组
	 *
	 * @param ids ID
	 * @return success/false
	 */
	@Operation(summary = "批量删除智能标注", description = "批量删除智能标注")
	@DeleteMapping
	@SysLog("删除智能标注")
//	@PreAuthorize("@pms.hasPermission('sys_label_task_del')")
	public R removeByIds(@RequestBody Long[] ids) {
		return R.ok(bizLabelIntelligentService.removeByIds(CollUtil.toList(ids)));
	}

	/**
	 * 插入标签组
	 *
	 * @param bizLabelIntelligent 智能标注实体
	 * @return success/false
	 */
	@Operation(summary = "添加智能标注", description = "添加智能标注")
	@SysLog("添加智能标注")
	@PostMapping("/save")
	public R save(@RequestBody BizLabelIntelligent bizLabelIntelligent) {
		return R.ok(bizLabelIntelligentService.save(bizLabelIntelligent));
	}

	/**
	 * 编辑
	 *
	 * @param bizLabelIntelligent 实体
	 * @return success/false
	 */
	@Operation(summary = "编辑修改智能标注", description = "编辑修改智能标注")
	@SysLog("编辑修改智能标注")
	@PutMapping
//	@PreAuthorize("@pms.hasPermission('sys_label_task_edit')")
	public R update(@Valid @RequestBody BizLabelIntelligent bizLabelIntelligent) {
		bizLabelIntelligent.setUpdateTime(LocalDateTime.now());
		return R.ok(bizLabelIntelligentService.updateById(bizLabelIntelligent));
	}
}
