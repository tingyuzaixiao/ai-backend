package com.train.platform.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.train.platform.admin.api.entity.AlgoServerResource;

public interface AlgoServerResourceService {
	IPage<AlgoServerResource> getAlgoServerResourcePage(Integer current, Integer size,
														AlgoServerResource algoServerResource);

	IPage<AlgoServerResource> getFreeAlgoPage(Integer current, Integer size);
}
