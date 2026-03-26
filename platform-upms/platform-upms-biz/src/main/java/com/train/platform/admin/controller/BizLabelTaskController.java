package com.train.platform.admin.controller;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import com.train.platform.admin.api.dto.FileDTO;
import com.train.platform.admin.api.dto.LabelTaskDTO;
import com.train.platform.admin.api.entity.BizLabelTask;
import com.train.platform.admin.api.vo.LabelFileVO;
import com.train.platform.admin.service.BizLabelTaskService;
import com.train.platform.admin.service.SysFileService;
import com.train.platform.common.core.util.R;
import com.train.platform.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/labelTask")
@Tag(description = "labelTask", name = "在线标注任务模块")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class BizLabelTaskController {

	private final BizLabelTaskService bizLabelTaskService;
	private final SysFileService sysFileService;

	/**
	 * 简单分页查询
	 *
	 * @param page         分页对象
	 * @param bizLabelTask 系统在线标注任务
	 * @return R
	 */
	@GetMapping("/page")
	public R getLabelTaskPage(@ParameterObject Page page, @ParameterObject BizLabelTask bizLabelTask) {
		return R.ok(bizLabelTaskService.getLabelTaskByPageNew(page, bizLabelTask));
	}

	/**
	 * 通过ID查询
	 *
	 * @param id ID
	 * @return BizLabelTask
	 */
	@Operation(summary = "通过ID查询在线标注任务", description = "通过ID查询在线标注任务")
	@GetMapping("/{id}")
	public R getById(@PathVariable Long id) {
		return R.ok(bizLabelTaskService.getLabelTaskById(id));
	}

	/**
	 * 查询在线标注任务信息
	 *
	 * @param query 查询条件
	 * @return 在线标注信息
	 */
	@Operation(summary = "查询在线标注任务信息", description = "查询在线标注任务信息")
	@GetMapping("/details")
	public R getDetails(@ParameterObject BizLabelTask query) {
		return R.ok(bizLabelTaskService.getOne(Wrappers.query(query), false));
	}

	/**
	 * 查询全部在线标注任务
	 */
	@Operation(summary = "查询全部在线标注任务", description = "查询全部在线标注任务")
	@GetMapping("/list")
	public R list() {
		return R.ok(bizLabelTaskService.list());
	}


	@Operation(summary = "查询已标注总计", description = "查询已标注总计")
	@GetMapping("/getLabeledTotal")
	public R getLabeledTotal(@ParameterObject Page page, @ParameterObject FileDTO file) {
		return R.ok(bizLabelTaskService.getLabeledTotal(page, file));
	}

	/**
	 * 批量删除标签组
	 *
	 * @param ids ID
	 * @return success/false
	 */
	@Operation(summary = "批量删除在线标注任务", description = "批量删除在线标注任务")
	@DeleteMapping
	@SysLog("删除在线标注任务")
//	@PreAuthorize("@pms.hasPermission('sys_label_task_del')")
	public R removeByIds(@RequestBody Long[] ids) {
		return bizLabelTaskService.removeLabelTaskByIds(CollUtil.toList(ids));
	}

	/**
	 * 插入标签组
	 *
	 * @param labelTaskDTO 在线标注任务实体
	 * @return success/false
	 */
	@Operation(summary = "添加在线标注任务", description = "添加在线标注任务")
	@SysLog("添加在线标注任务")
	@PostMapping("/save")
	public R save(@RequestBody LabelTaskDTO labelTaskDTO) {
		return bizLabelTaskService.saveLabelTask(labelTaskDTO);
	}

	/**
	 * 编辑
	 *
	 * @param labelTaskDTO 实体
	 * @return success/false
	 */
	@Operation(summary = "编辑修改在线标注任务", description = "编辑修改在线标注任务")
	@SysLog("编辑修改在线标注任务")
	@PutMapping
//	@PreAuthorize("@pms.hasPermission('sys_label_task_edit')")
	public R update(@Valid @RequestBody LabelTaskDTO labelTaskDTO) {
		labelTaskDTO.setUpdateTime(LocalDateTime.now());
		return bizLabelTaskService.updateLabelTaskById(labelTaskDTO);
	}

	/**
	 * 导出excel 表格
	 *
	 * @param bizLabelTask 查询条件
	 * @return R
	 */
	@ResponseExcel
	@GetMapping("/export")
	@Operation(summary = "导出在线标注任务", description = "导出在线标注任务")
	@SysLog("导出在线标注任务")
//	@PreAuthorize("@pms.hasPermission('sys_label_task_export')")
	public List<BizLabelTask> export(BizLabelTask bizLabelTask) {
		return bizLabelTaskService.list(Wrappers.lambdaQuery(bizLabelTask));
	}

	@ResponseExcel
	@GetMapping("/exportExcel")
	@Operation(summary = "导出在线标注任务Excel", description = "导出在线标注任务Excel")
	@SysLog("导出在线标注任务Excel")
//	@PreAuthorize("@pms.hasPermission('sys_label_task_export')")
	public List<LabelFileVO> exportByIds(LabelTaskDTO bizLabelTask) {
//		return bizLabelTaskService.export(bizLabelTask);
		//分类  检测和分割区分开
		return bizLabelTaskService.exportLabelTask(bizLabelTask);
	}

	@GetMapping("/exportCSV")
	@Operation(summary = "导出在线标注任务CSV", description = "导出在线标注任务CSV")
	@SysLog("导出在线标注任务CSV")
	public void exportCSV(HttpServletResponse response, LabelTaskDTO bizLabelTask) {
		response.setContentType("text/csv; charset=UTF-8");
		String filename = "label.csv";
		String encodedFilename = new String(filename.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
		response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFilename + "\"");

		String labelType = bizLabelTask.getLabelType();

		if (labelType != null && !labelType.isEmpty()) {
			// 获取输出流并导出CSV
			try (Writer writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
				bizLabelTaskService.exportLabelsToCsv(writer, bizLabelTask);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@GetMapping("/exportJSON")
	@Operation(summary = "导出在线标注任务JSON", description = "导出在线标注任务JSON")
	@SysLog("导出在线标注任务JSON")
	public void exportJSON(HttpServletResponse response, LabelTaskDTO bizLabelTask) {
		response.setContentType("application/json; charset=UTF-8");
		String filename = "label.json";
		String encodedFilename = new String(filename.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
		response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFilename + "\"");

		// 获取输出流并导出CSV
		try (Writer writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
			String exportLabelsToJSON = bizLabelTaskService.exportLabelsToJSON(bizLabelTask);
			writer.write(exportLabelsToJSON);
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@GetMapping("/exportMASK")
	@Operation(summary = "导出在线标注任务MASK", description = "导出在线标注任务MASK")
	@SysLog("导出在线标注任务MASK")
	public void exportMASK(HttpServletResponse response, @RequestParam Map<String, String> params) {
		response.setContentType("application/octet-stream; charset=UTF-8");
		String encodedFilename = new String(params.get("fileName").getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
		response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFilename + "\"");
		sysFileService.download(params, response);
	}
}
