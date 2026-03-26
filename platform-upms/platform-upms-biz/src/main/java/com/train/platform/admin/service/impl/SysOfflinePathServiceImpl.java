package com.train.platform.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.train.platform.admin.api.entity.SysOfflinePath;
import com.train.platform.admin.service.SysOfflinePathService;
import com.train.platform.admin.mapper.SysOfflinePathMapper;
import com.train.platform.common.core.util.FileUtils;
import com.train.platform.common.core.util.R;
import com.train.platform.common.file.core.FileProperties;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static com.train.platform.common.core.util.FileUtils.*;

/**
 * @author li
 * @description 针对表【aircas_offline_path】的数据库操作Service实现
 * @createDate 2025-06-17 14:25:52
 */
@Service
public class SysOfflinePathServiceImpl extends ServiceImpl<SysOfflinePathMapper, SysOfflinePath>
		implements SysOfflinePathService {

	@Autowired
	private FileProperties properties;

	@Override
	public R saveOfflinePath(SysOfflinePath sysOfflinePath) {
		//判断当前用户同类型目录是否重复创建
		List<SysOfflinePath> sysOfflinePaths = baseMapper.selectList(Wrappers.<SysOfflinePath>lambdaQuery().
				eq(SysOfflinePath::getUserId, sysOfflinePath.getUserId()).
				eq(SysOfflinePath::getType, sysOfflinePath.getType()));
		if (sysOfflinePaths != null && !sysOfflinePaths.isEmpty()) {
			return R.failed("当前用户同类型目录已存在");
		} else {
			//保存目录
			baseMapper.insert(sysOfflinePath);
			return R.ok(sysOfflinePath.getId());
		}
	}

	@Override
	public void download(SysOfflinePath sysOfflinePath, HttpServletResponse response) throws IOException {
		String nginxFolder = properties.getLocal().getBasePath() + properties.getNginxPath();
		validateParameters(sysOfflinePath.getLocalPath(), Integer.parseInt(sysOfflinePath.getServerPort()));

		// 2. 创建临时工作目录
		Path tempDir = createTempDirectory();
		try {
			// 3. 复制原始Nginx目录
			Path nginxCopyPath = copyNginxDirectory(tempDir, nginxFolder);

			// 4. 生成并替换nginx.conf
			generateAndReplaceConfig(nginxCopyPath, sysOfflinePath.getLocalPath(), Integer.parseInt(sysOfflinePath.getServerPort()), sysOfflinePath.getServerUri());

			// 5. 创建ZIP压缩包
//			Path zipPath = createZipArchive(nginxCopyPath);

			// 6. 返回ZIP下载响应
//			return buildDownloadResponse(zipPath);
			FileUtils.downloadZip(nginxCopyPath.toString(), "nginx" + ".zip", response);
		} finally {
			// 7. 清理临时目录
			cleanTempDirectoryAsync(tempDir);
		}
	}
}




