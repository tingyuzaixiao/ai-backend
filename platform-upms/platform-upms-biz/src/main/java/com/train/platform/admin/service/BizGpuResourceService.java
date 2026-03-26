package com.train.platform.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.train.platform.admin.api.entity.BizExperiment;
import com.train.platform.admin.api.entity.BizGpuResource;
import com.train.platform.admin.api.vo.GpuResourceVO;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


/**
 * <p>
 * gpu资源管理 服务类
 * </p>
 *
 * @author zhouzhipeng
 * @since 2024-01-20
 */
public interface BizGpuResourceService extends IService<BizGpuResource> {
	IPage<BizGpuResource> getFreeGpuPage(Integer current, Integer size);

	IPage<BizGpuResource> getGpuPage(Integer current, Integer size, BizGpuResource bizGpuResource);

	Boolean updateBizGpu(BizGpuResource bizGpuResource);

	BizGpuResource getBizGpuResourceById(Long id);
}
