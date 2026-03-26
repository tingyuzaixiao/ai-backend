package com.train.platform.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.train.platform.admin.api.entity.BizGpuResource;
import com.train.platform.admin.mapper.BizGpuResourceMapper;
import com.train.platform.admin.service.BizGpuResourceService;
import com.train.platform.common.core.constant.enums.GpuStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * gpu资源管理  服务实现类
 * </p>
 *
 * @author zhouzhipeng
 * @since 2024-01-25
 * todo 资源有效性检测
 */
@Service
public class BizGpuResourceServiceImpl extends ServiceImpl<BizGpuResourceMapper, BizGpuResource> implements BizGpuResourceService {
	@Override
	public IPage<BizGpuResource> getFreeGpuPage(Integer current, Integer size) {
		Page<BizGpuResource> page = new Page<>(current, size);
		BizGpuResource bizGpuResource = new BizGpuResource();
		bizGpuResource.setState(GpuStatus.FREE.name());
		bizGpuResource.setDelFlag("0");
		return baseMapper.selectPage(page, Wrappers.query(bizGpuResource));
	}

	@Override
	public IPage<BizGpuResource> getGpuPage(Integer current, Integer size, BizGpuResource bizGpuResource) {
		Page<BizGpuResource> page = new Page<>(current, size);
		bizGpuResource.setDelFlag("0");
		return baseMapper.selectPage(page, Wrappers.query(bizGpuResource));
	}

	@Override
	public Boolean updateBizGpu(BizGpuResource bizGpuResource) {
		baseMapper.updateById(bizGpuResource);
		return true;
	}

	@Override
	public BizGpuResource getBizGpuResourceById(Long id) {
		if (id == null) {
			return null;
		}

		BizGpuResource bizGpuResource = new BizGpuResource();
		bizGpuResource.setId(id);
		bizGpuResource.setDelFlag("0");
		List<BizGpuResource> gpuResources = baseMapper.getBizGpuResources(bizGpuResource);
		if (gpuResources == null || gpuResources.isEmpty()) {
			return null;
		}
		return gpuResources.get(0);
	}
}
