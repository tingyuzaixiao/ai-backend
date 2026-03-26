package com.train.platform.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.train.platform.admin.api.dto.BizDatasetDTO;
import com.train.platform.admin.api.dto.DatasetImgDTO;
import com.train.platform.admin.api.entity.BizDataset;
import com.train.platform.admin.service.BizDatasetService;
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

/**
 * <p>
 * 数据集管理 控制层
 * </p>
 *
 * @author lee
 * @since 2024-05-24
 */
@RestController
@AllArgsConstructor
@RequestMapping("/dataset")
@Tag(description = "dataset", name = "数据集模块")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class BizDatasetController {

	private final BizDatasetService bizDatasetService;

	/**
	 * 通过ID查询
	 * @param id ID
	 * @return BizDataset
	 */
	@Operation(summary = "通过ID查询数据集", description = "通过ID查询数据集")
	@GetMapping("/{id}")
	public R getById(@PathVariable Long id) {
		return R.ok(bizDatasetService.getById(id));
	}

	/**
	 * 查询全部基础数据集
	 */
	@Operation(summary = "查询全部数据集", description = "查询全部数据集")
	@GetMapping("/list")
	public R list() {
		return R.ok(bizDatasetService.list());
	}

	@Operation(summary = "分页查询", description = "分页查询")
	@GetMapping("/page")
	public R getDatasetPage(@ParameterObject Page page, @ParameterObject BizDataset bizDataset) {
		return R.ok(bizDatasetService.getDatasetsPage(page, bizDataset));
	}

	/**
	 * 添加
	 * @param bizDataset 实体
	 * @return success/false
	 */
	@Operation(summary = "创建数据集", description = "创建数据集")
	@SysLog("创建数据集")
	@PostMapping
	public R save(@Valid @RequestBody BizDatasetDTO bizDataset) {
		return R.ok(bizDatasetService.saveBizDataset(bizDataset));
	}

	/**
	 * 删除
	 * @param id ID
	 * @return success/false
	 */
	@Operation(summary = "删除数据集", description = "删除数据集")
	@SysLog("删除数据集")
	@DeleteMapping("/delete/{id}")
	public R removeById(@PathVariable Long id) {
		return bizDatasetService.deleteById(id);
	}

	/**
	 * 查询数据集信息
	 * @param query 查询条件
	 * @return 数据集信息
	 */
	@Operation(summary = "查询数据集信息", description = "查询数据集信息")
	@GetMapping("/details")
	public R getDetails(@ParameterObject BizDataset query) {
		return R.ok(bizDatasetService.getOne(Wrappers.query(query), false));
	}

	/**
	 * 编辑
	 * @param bizDatasetDTO 实体
	 * @return success/false
	 */
	@Operation(summary = "编辑修改数据集", description = "编辑修改数据集")
	@SysLog("编辑修改数据集")
	@PutMapping
	public R update(@Valid @RequestBody BizDatasetDTO bizDatasetDTO) {
		bizDatasetDTO.setUpdateTime(LocalDateTime.now());
		return R.ok(bizDatasetService.updateById(bizDatasetDTO));
	}

	@Operation(summary = "分页查询数据集图像列表", description = "分页查询数据集图像列表")
	@GetMapping("/page/img")
	public R getDatasetImgPage(@ParameterObject DatasetImgDTO datasetImgDTO) {
		if (datasetImgDTO.getDatasetId() == null) {
			return R.failed("数据集ID不能为空");
		}
		return R.ok(bizDatasetService.getDatasetImgPage(datasetImgDTO));
	}

	@Operation(summary = "更新数据集标注进度",description = "更新数据集标注进度")
	@PostMapping("/updateLabelProgress")
	public R updateDatasetLabelProgress(@RequestBody BizDataset bizDataset) {
		bizDatasetService.updateDatasetLabelProgress(bizDataset);
		return R.ok();
	}
}
