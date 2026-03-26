package com.train.platform.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.train.platform.admin.api.entity.SysProgram;
import com.train.platform.admin.service.SysProgramService;
import com.train.platform.common.core.constant.CacheConstants;
import com.train.platform.common.core.util.R;
import com.train.platform.common.log.annotation.SysLog;
import com.train.platform.common.security.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文件管理
 *
 * @author Lee
 * @date 2024-05-17 17:11:42
 */
@RestController
@AllArgsConstructor
@RequestMapping("/sys-program")
@Tag(description = "sys-program", name = "项目管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class SysProgramController {

	private final SysProgramService sysProgramService;

	/**
	 * 分页查询
	 *
	 * @param page       分页对象
	 * @param sysProgram 项目管理
	 * @return
	 */
	@Operation(summary = "分页查询", description = "分页查询")
	@GetMapping("/page")
	public R getSysProgramPage(@ParameterObject Page page, @ParameterObject SysProgram sysProgram) {
		// 获取符合条件的项目
		List<Long> roles = SecurityUtils.getRoles();

		return R.ok(sysProgramService.getProgramVOsWithRolePage(page, sysProgram, roles));
	}

	@Operation(summary = "通过id查询", description = "通过id查询")
	@GetMapping("/{id}")
	public R getById(@PathVariable("id") Long id) {
		return R.ok(sysProgramService.getDetails(id));
	}

	@Operation(summary = "通过项目id/programId/name查询", description = "通过项目id/programId/name查询")
	@GetMapping("/get")
	public R getSysProgram(@ParameterObject SysProgram sysProgram) {
		return R.ok(sysProgramService.getSysProgram(sysProgram.getId(), sysProgram.getProgramId(), sysProgram.getName()));
	}

	@SysLog("删除项目")
	@Operation(summary = "通过id删除项目", description = "通过id删除项目")
	@DeleteMapping
	// @PreAuthorize("@pms.hasPermission('sys_program_del')")
	@CacheEvict(value = CacheConstants.PROGRAM_DETAILS, allEntries = true)
	public R removeById(@RequestBody Long id) {
		return R.ok(sysProgramService.removeProgramById(id));
	}

	@Operation(summary = "检测项目名是否可用", description = "检测项目名是否可用")
	@GetMapping("/checkName")
	public R checkProgramNameAvailable(@RequestParam("name") String name) {
		return R.ok(sysProgramService.checkProgramNameAvailable(name));
	}

	@Operation(summary = "新增项目", description = "新增项目")
	@SysLog("新增项目")
	@PostMapping
	public R save(@Valid @RequestBody SysProgram sysProgram) {
		return R.ok(sysProgramService.saveProgram(sysProgram));
	}

	@Operation(summary = "修改项目", description = "修改项目")
	@SysLog("修改项目")
	@PutMapping
	public R updateById(@RequestBody SysProgram sysProgram) {
		return R.ok(sysProgramService.updateById(sysProgram));
	}

	@Operation(summary = "查询项目信息", description = "查询项目信息")
	@GetMapping("/details")
	public R getDetails(@RequestParam("id") Long id) {
		return R.ok(sysProgramService.getDetails(id));
	}

//	/**
//	 * 返回当前用户的项目集合
//	 *
//	 * @return 当前用户的项目集合
//	 */
//	@Operation(summary = "查询当前用户项目列表", description = "查询当前用户项目列表")
//	@GetMapping
//	public R getRoleProgram() {
//		// 获取符合条件的项目
//		Set<ProgramVO> all = new HashSet<>();
//		SecurityUtils.getRoles().forEach(roleId -> all.addAll(sysProgramService.findProgramByRoleId(roleId)));
//		return R.ok(all);
//	}

//	/**
//	 * 查询项目信息
//	 * @param query 查询条件
//	 * @return 项目信息
//	 */
//	@Operation(summary = "查询项目信息", description = "查询项目信息")
//	@GetMapping("/details")
//	public R getDetails(@ParameterObject SysProgram query) {
//		return R.ok(sysProgramService.getOne(Wrappers.<SysProgram>query(query)));
//	}

}
