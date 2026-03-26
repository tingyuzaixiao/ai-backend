package com.train.platform.admin.controller;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.train.platform.admin.api.entity.SysDeletedDir;
import com.train.platform.admin.service.SysDeletedDirService;
import com.train.platform.common.core.constant.CommonConstants;
import com.train.platform.common.core.util.R;
import com.train.platform.common.log.annotation.SysLog;
import com.train.platform.common.security.annotation.Inner;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/deleted-dir")
@Tag(description = "deleted-dir", name = "文件超时待删除模块")
public class SysDeletedDirController {
	
	private SysDeletedDirService sysDeletedDirService;

	/**
	 * 通过ID查询待删除文件信息
	 * @param id ID
	 * @return 待删除文件信息
	 */
	@GetMapping("/details/{id}")
	public R sysDelDir(@PathVariable Long id) {
		return R.ok(sysDeletedDirService.selectSysDelDirById(id));
	}

	/**
	 * 查询待删除文件信息
	 * @param query 查询条件
	 * @return 不为空返回待删除文件
	 */
	@Inner(value = false)
	@GetMapping("/details")
	public R getDetails(@ParameterObject SysDeletedDir query) {
		SysDeletedDir SysDeletedDir = sysDeletedDirService.getOne(Wrappers.query(query), false);
		return R.ok(SysDeletedDir == null ? null : CommonConstants.SUCCESS);
	}

	/**
	 * 删除待删除文件信息
	 * @param ids ID
	 * @return R
	 */
	@SysLog("删除待删除文件信息")
	@DeleteMapping
//	@PreAuthorize("@pms.hasPermission('sys_user_del')")
	@Operation(summary = "删除待删除文件", description = "根据ID删除待删除文件")
	public R sysDelDirDel(@RequestBody Long[] ids) {
		return R.ok(sysDeletedDirService.deleteSysDelDirsByIds(ids));
	}

	/**
	 * 添加待删除文件
	 * @param sysDeletedDir 文件信息
	 * @return success/false
	 */
	@SysLog("添加待删除文件")
	@PostMapping
//	@PreAuthorize("@pms.hasPermission('sys_dir_add')")
	public R sysDelDir(@RequestBody SysDeletedDir sysDeletedDir) {
		return R.ok(sysDeletedDirService.saveDeleteDir(sysDeletedDir));
	}

	/**
	 * 更新待删除文件信息
	 * @param sysDeletedDir 待删除文件信息
	 * @return R
	 */
	@SysLog("更新待删除文件信息")
	@PutMapping
//	@PreAuthorize("@pms.hasPermission('sys_dir_edit')")
	public R updateSysDelDir(@Valid @RequestBody SysDeletedDir sysDeletedDir) {
		return R.ok(sysDeletedDirService.updateById(sysDeletedDir));
	}

	/**
	 * 分页查询待删除文件
	 * @param page 参数集
	 * @param sysDeletedDir 查询参数列表
	 * @return 文件集合
	 */
	@GetMapping("/page")
	public R getUserPage(@ParameterObject Page page, @ParameterObject SysDeletedDir sysDeletedDir) {
		return R.ok(sysDeletedDirService.getSysDelDirsPage(page, sysDeletedDir));
	}

	/**
	 * 修改个人信息
	 * @param sysDeletedDir
	 * @return success/false
	 */
	@SysLog("修改待删除文件信息")
	@PutMapping("/edit")
	public R updateUserInfo(@Valid @RequestBody SysDeletedDir sysDeletedDir) {
		return R.ok(sysDeletedDirService.updateById(sysDeletedDir));
	}

	@SysLog("删除待删除文件")
	@GetMapping("/delHistoryDir")
	@Inner(value = false)
	public R delHistoryDir() {
		return R.ok(sysDeletedDirService.delHistoryDir());
	}

	@SysLog("立即删除待删除文件")
	@PostMapping("/immediatelyDelHistoryDir")
	@Inner(value = false)
	public R immediatelyDelHistoryDir(@RequestBody Long[] ids) {
		return R.ok(sysDeletedDirService.immediatelyDelHistoryDir(ids));
	}
}
