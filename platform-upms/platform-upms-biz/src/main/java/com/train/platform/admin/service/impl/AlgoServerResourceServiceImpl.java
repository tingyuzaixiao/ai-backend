package com.train.platform.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.train.platform.admin.api.entity.AlgoServerResource;
import com.train.platform.admin.api.entity.BizExperiment;
import com.train.platform.admin.mapper.service.AlgoServerResourceMapperService;
import com.train.platform.admin.service.AlgoServerResourceService;
import com.train.platform.common.core.constant.enums.GpuStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AlgoServerResourceServiceImpl implements AlgoServerResourceService {
	@Autowired
	private AlgoServerResourceMapperService mapperService;

	@Override
	public IPage<AlgoServerResource> getAlgoServerResourcePage(Integer current, Integer size,
															   AlgoServerResource algoServerResource) {
		Page<AlgoServerResource> page = new Page<>(current, size);
		algoServerResource.setDelFlag("0");

		return mapperService.getBaseMapper().selectPage(page, Wrappers.query(algoServerResource));
	}

	@Override
	public IPage<AlgoServerResource> getFreeAlgoPage(Integer current, Integer size) {
		Page<AlgoServerResource> page = new Page<>(current, size);
		AlgoServerResource algoServerResource = new AlgoServerResource();
		algoServerResource.setStatus(GpuStatus.FREE.name());
		algoServerResource.setDelFlag("0");

		return mapperService.getBaseMapper().selectPage(page, Wrappers.query(algoServerResource));
	}
}
