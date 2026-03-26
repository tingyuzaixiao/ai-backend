package com.train.platform.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.train.platform.admin.api.entity.*;
import com.train.platform.admin.api.vo.LabelTaskListVO;
import com.train.platform.admin.mapper.BizExperimentMapper;
import com.train.platform.admin.mapper.BizLabelTaskMapper;
import com.train.platform.admin.mapper.SysFileMapper;
import com.train.platform.admin.service.BizBaseDatasetService;
import com.train.platform.admin.service.BizDatasetService;
import com.train.platform.admin.service.SysDeletedDirService;
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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.train.platform.common.core.constant.CommonConstants.DATASET_FOLDER;
import static com.train.platform.common.core.constant.CommonConstants.FILE_FUNCTION_DATASET;

/**
 * <p>
 * 基础数据集
 * </p>
 *
 * @author lee
 * @since 2024-05-24
 */
@RestController
@AllArgsConstructor
@RequestMapping("/baseDataset")
@Tag(description = "baseDataset", name = "基础数据集模块")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class BizBaseDatasetController {

	private final BizBaseDatasetService bizBaseDatasetService;

	private final BizDatasetService bizDatasetService;


	/**
	 * 通过ID查询
	 *
	 * @param id ID
	 * @return BaseDataset
	 */
	@Operation(summary = "通过ID查询", description = "通过ID查询")
	@GetMapping("/{id}")
	public R getById(@PathVariable Long id) {
		return R.ok(bizBaseDatasetService.getById(id));
	}

	/**
	 * 查询全部基础数据集
	 */
	@Operation(summary = "查询全部基础数据集", description = "查询全部基础数据集")
	@GetMapping("/list")
	public R list() {
		return R.ok(bizBaseDatasetService.list());
	}

	@Operation(summary = "数据集分组", description = "数据集分组")
	@PostMapping("/listGroup")
	public R listGroup(@RequestBody(required = false) BizBaseDataset bizBaseDataset) {
		return R.ok(bizBaseDatasetService.listGroup(bizBaseDataset));
	}

	@Operation(summary = "分页查询", description = "分页查询")
	@GetMapping("/page")
	public R getBaseTrainDatasetPage(@ParameterObject Page page, @ParameterObject BizBaseDataset bizBaseDataset) {
		return R.ok(bizBaseDatasetService.getBaseDatasetVOsPage(page, bizBaseDataset));
	}

	/**
	 * 添加
	 *
	 * @param bizBaseDataset 实体
	 * @return success/false
	 */
	@Operation(summary = "添加基础数据集", description = "添加基础数据集")
	@SysLog("添加基础数据集")
	@PostMapping
	public R save(@Valid @RequestBody BizBaseDataset bizBaseDataset) {
		return R.ok(bizBaseDatasetService.saveBaseDataset(bizBaseDataset));
	}

	@Operation(summary = "查询并修改基础数据集状态", description = "查询并修改基础数据集状态")
	@GetMapping("/getState")
	public R getState(@ParameterObject BizBaseDataset bizBaseDataset) {
		return bizBaseDatasetService.getState(bizBaseDataset);
	}

	/**
	 * 删除
	 *
	 * @param id ID
	 * @return success/false
	 */
	@Operation(summary = "删除基础数据集", description = "删除基础数据集")
	@SysLog("删除基础数据集")
	@DeleteMapping("/delete/{id}")
	public R removeById(@PathVariable Long id) {
		return bizBaseDatasetService.removeBaseDatasetById(id);
	}

	/**
	 * 编辑
	 *
	 * @param bizBaseDataset 实体
	 * @return success/false
	 */
	@Operation(summary = "编辑基础数据集", description = "编辑基础数据集")
	@SysLog("编辑基础数据集")
	@PutMapping
	public R update(@Valid @RequestBody BizBaseDataset bizBaseDataset) {
		return R.ok(bizBaseDatasetService.updateBaseDataset(bizBaseDataset));
	}

	/**
	 * 查询数据集信息
	 *
	 * @param query 查询条件
	 * @return 角色信息
	 */
	@Operation(summary = "查询数据集信息", description = "查询数据集信息")
	@GetMapping("/details")
	public R getDetails(@ParameterObject BizBaseDataset query) {
		return R.ok(bizBaseDatasetService.getOne(Wrappers.<BizBaseDataset>lambdaQuery().eq(BizBaseDataset::getBaseDatasetName, query.getBaseDatasetName())));
	}

	/**
	 * 根据基础数据集ID查询图像IDs
	 */
	@Operation(summary = "根据基础数据集ID查询图像IDs", description = "根据基础数据集ID查询图像IDs")
	@GetMapping("/imageIds")
	public R getImageIds(@RequestParam Long baseDatasetId) {
		List<Long> imageIds = bizBaseDatasetService.getImageIds(baseDatasetId);
		if (!imageIds.isEmpty()) {
			Map<String, Object> resMap = new HashMap<>();
			resMap.put("imageIds", imageIds);
			return R.ok(resMap);
		} else {
			return R.ok();
		}
	}


	/**
	 * 根据数据集ID查询基础数据集标签组
	 */
	@Operation(summary = "根据数据集ID查询基础数据集标签组", description = "根据数据集ID查询基础数据集标签组")
	@GetMapping("/getLabelGroup")
	public R getLabelGroup(@RequestParam(required = false) Long baseDatasetId,
						   @RequestParam(required = false) Long datasetId) {
		if (datasetId == null) {
			return R.ok(bizBaseDatasetService.getLabelListById(baseDatasetId));
		} else {
			return R.ok(bizDatasetService.getLabelListById(datasetId));
		}

	}

	/**
	 * 根据数据集ID查询基础数据集FileIds
	 */
	@Operation(summary = "根据数据集ID查询基础数据集FileIds", description = "根据数据集ID查询基础数据集FileIds")
	@GetMapping("/getFilesByBaseDatasetId")
	public R getFilesByBaseDatasetId(@RequestParam Long baseDatasetId) {
		return R.ok(bizBaseDatasetService.getFilesByBaseDatasetId(baseDatasetId));
	}

	/**
	 * 根据md5判断基础数据集图像是否重复
	 */
	@Operation(summary = "根据md5判断基础数据集图像是否重复", description = "根据md5判断基础数据集图像是否重复")
	@GetMapping("/checkMd5")
	public R checkMd5(@RequestParam Long baseDatasetId, @RequestParam String md5) {
		return R.ok(bizBaseDatasetService.checkMd5(baseDatasetId, md5));
	}

}
