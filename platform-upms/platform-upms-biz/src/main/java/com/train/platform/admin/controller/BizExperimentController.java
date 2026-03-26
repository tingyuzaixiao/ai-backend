package com.train.platform.admin.controller;

import com.train.platform.admin.api.entity.BizExperiment;
import com.train.platform.admin.api.entity.BizGpuResource;
import com.train.platform.admin.api.vo.ExperimentBatchDeleteVO;
import com.train.platform.admin.api.vo.ExperimentStartVO;
import com.train.platform.admin.api.vo.ExperimentStopVO;
import com.train.platform.admin.service.BizExperimentService;
import com.train.platform.admin.service.impl.AlgoServerResourceServiceImpl;
import com.train.platform.admin.service.impl.BizGpuResourceServiceImpl;
import com.train.platform.common.core.constant.enums.ExperimentStopType;
import com.train.platform.common.core.util.R;
import com.train.platform.common.log.annotation.SysLog;
import com.train.platform.common.security.annotation.Inner;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.mlflow_project.google.common.base.Preconditions;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 实验管理 控制层
 * </p>
 *
 * @author zhouzhipeng
 * @since 2024-05-20
 */
@RestController
@AllArgsConstructor
@RequestMapping("/experiment")
@Tag(description = "experiment", name = "实验（训练、校验、预测）模块")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class BizExperimentController {
	private final BizExperimentService bizExperimentService;
	private final BizGpuResourceServiceImpl bizGpuResourceService;
	private final AlgoServerResourceServiceImpl algoServerResourceService;

	@Operation(summary = "通过ID查询实验配置", description = "通过ID查询实验配置")
	@GetMapping("/{id}")
	public R getExperimentConfigById(@PathVariable("id") Long id) {
		return R.ok(bizExperimentService.queryExperimentForWeb(id, null, null));
	}

	@Operation(summary = "通过实验id/experimentId/name查询", description = "通过实验id/experimentId/name查询")
	@GetMapping("/get")
	public R getExperiment(@ParameterObject BizExperiment experiment) {
		return R.ok(bizExperimentService.queryExperimentForWeb(experiment.getId(), experiment.getExperimentId(),
				experiment.getExperimentName()));
	}

	@Operation(summary = "分页查询", description = "分页查询")
	@GetMapping("/page")
	public R getExperimentPage(@RequestParam("current") Integer current,
							   @RequestParam("size") Integer size,
							   @ParameterObject BizExperiment experiment) {
		Preconditions.checkArgument(StringUtils.hasText(experiment.getProgramId()),
				"programId is invalid");
		return R.ok(bizExperimentService.getExperimentsByPage(current, size, experiment.getProgramId()));
	}

	@Operation(summary = "检测实验名是否可用", description = "检测实验名是否可用")
	@GetMapping("/checkName")
	public R checkExperimentNameAvailable(@RequestParam("experimentName") String experimentName,
											 @RequestParam("programId") String programId) {
		return R.ok(bizExperimentService.checkExperimentNameAvailable(experimentName, programId));
	}

	@Operation(summary = "创建实验记录", description = "创建实验记录")
	@SysLog("创建实验记录")
	@PostMapping
	public R save(@Valid @RequestPart("experiment") BizExperiment experiment,
				  @RequestPart(value = "modelFile", required = false) MultipartFile modelFile,
				  @RequestPart(value = "weightsFile", required = false) MultipartFile weightsFile) {
		return R.ok(bizExperimentService.createExperiment(experiment, modelFile, weightsFile));
	}

	@Operation(summary = "根据实验ID更新实验配置", description = "根据实验ID更新实验配置")
	@PostMapping("/updateConfig")
	public R updateExperimentConfig(@RequestPart("experiment") BizExperiment experiment,
									@RequestPart(value = "modelFile", required = false) MultipartFile modelFile,
									@RequestPart(value = "weightsFile", required = false) MultipartFile weightsFile) {
		return R.ok(bizExperimentService.updateExperimentConfig(experiment, modelFile, weightsFile));
	}

	@Operation(summary = "查询实验详情", description = "通过ID查询实验详情")
	@GetMapping("/details/{id}")
	public R queryDetails(@PathVariable Long id) {
		return R.ok(bizExperimentService.queryDetails(id));
	}

	@Operation(summary = "查询指标历史值", description = "查询指标历史值")
	@GetMapping("/metrics")
	public R queryMetricHistory(@RequestParam("experimentId") String experimentId,
								@RequestParam("metricName") String metricName) {
		return R.ok(bizExperimentService.queryMetricHistory(experimentId, metricName));
	}

	/**
	 * @param fileType {@link com.train.platform.common.core.constant.enums.MLFileType}
	 * @param path 目录
	 */
	@Operation(summary = "查询文件列表", description = "查询文件列表")
	@GetMapping("/directories")
	public R listFiles(@RequestParam("experimentId") String experimentId,
					   @RequestParam("fileType") Integer fileType,
					   @RequestParam(value="path", required=false) String path) {
		return R.ok(bizExperimentService.listFiles(experimentId, fileType, path));
	}

	/**
	 * @param fileType {@link com.train.platform.common.core.constant.enums.MLFileType}
	 * @param file 包含路径的文件名
	 */
	@Operation(summary = "下载mlflow文件", description = "下载mlflow文件")
	@GetMapping("/mlfile")
	public void downloadFile(@RequestParam("experimentId") String experimentId,
							 @RequestParam("fileType") Integer fileType,
							 @RequestParam(value="file") String file,
							 HttpServletResponse response) {
		bizExperimentService.downloadFile(response, experimentId, fileType, file);
	}

	/**
	 * @param fileType {@link com.train.platform.common.core.constant.enums.ExperimentFileType}
	 */
	@Operation(summary = "下载文件", description = "下载文件")
	@GetMapping("/file")
	public void downloadExperimentFile(@RequestParam("id") Long id,
									   @RequestParam("fileType") Integer fileType,
									   HttpServletResponse response) {
		bizExperimentService.downloadExperimentFile(response, id, fileType);
	}

	@Operation(summary = "删除实验记录", description = "根据id列表删除实验记录")
	@SysLog("删除实验记录")
	@DeleteMapping("/batchDelete")
	public R removeByIds(@Valid @RequestBody ExperimentBatchDeleteVO experimentBatchDeleteVO) {
		return R.ok(bizExperimentService.removeExperimentByIds(experimentBatchDeleteVO.getIds()));
	}

	@Operation(summary = "试验开始运行", description = "试验开始运行")
	@SysLog("试验开始运行")
	@PostMapping("/start")
	public R startRunning(@Valid @RequestBody ExperimentStartVO experimentStartVO) {
		return R.ok(bizExperimentService.startRunning(
				experimentStartVO.getId(),
				experimentStartVO.getResourceId()));
	}

	@Operation(summary = "停止试验运行", description = "停止试验运行")
	@SysLog("停止试验运行")
	@PostMapping("/stop")
	public R stopRunning(@Valid @RequestBody ExperimentStopVO experimentStopVO) {
		if (experimentStopVO.getStopType() == null) {
			experimentStopVO.setStopType(ExperimentStopType.SAVE_WEIGHTS.getValue());
		}
		return R.ok(bizExperimentService.stopRunning(
				experimentStopVO.getId(),
				experimentStopVO.getStopType()));
	}

	@Operation(summary = "查询空闲算法服务", description = "查询空闲算法服务")
	@GetMapping("/getFreeAlgo")
	@Inner(value = false)
	public R getFreeAlgoPage(@RequestParam("current") Integer current,
							 @RequestParam("size") Integer size) {
		return R.ok(algoServerResourceService.getFreeAlgoPage(current, size));
	}

	@Operation(summary = "查询空闲GPU资源", description = "查询空闲GPU资源")
	@GetMapping("/getFreeGpu")
	@Inner(value = false)
	public R getFreeGpuPage(@RequestParam("current") Integer current,
						@RequestParam("size") Integer size) {
		return R.ok(bizGpuResourceService.getFreeGpuPage(current, size));
	}

	@Operation(summary = "更新GPU资源", description = "更新GPU资源")
	@PostMapping("/updateGpuResource")
	public R updateGpuResource(@RequestBody BizGpuResource bizGpuResource) {
		return R.ok(bizGpuResourceService.updateBizGpu(bizGpuResource));
	}
}
