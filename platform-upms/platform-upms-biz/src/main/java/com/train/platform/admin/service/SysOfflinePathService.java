package com.train.platform.admin.service;

import com.train.platform.admin.api.entity.SysOfflinePath;
import com.baomidou.mybatisplus.extension.service.IService;
import com.train.platform.common.core.util.R;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.io.IOException;

/**
* @author li
* @description 针对表【aircas_offline_path】的数据库操作Service
* @createDate 2025-06-17 14:25:52
*/
public interface SysOfflinePathService extends IService<SysOfflinePath> {

	R saveOfflinePath(@Valid SysOfflinePath sysOfflinePath);

	void download(SysOfflinePath sysOfflinePath, HttpServletResponse response) throws IOException;
}
