package com.train.platform.admin.controller;

import com.train.platform.admin.api.entity.AlgoServerResource;
import com.train.platform.admin.api.vo.ExperimentStopVO;
import com.train.platform.admin.service.InnerAlgoService;
import com.train.platform.common.core.util.HttpServletResponseUtils;
import com.train.platform.common.core.util.R;
import com.train.platform.common.security.annotation.Inner;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.Collections;


@Slf4j
@RestController
@RequestMapping("/internal/api/")
@Tag(description = "内部服务调用", name = "内部服务调用")
@SecurityRequirement(name = "X-Service-Key")
public class InnerAlgoController {
	@Autowired
	private InnerAlgoService innerAlgoService;

	@Operation(summary = "服务注册", description = "服务注册")
	@PostMapping("/register")
	@Inner
	public R registerServer(@Valid @RequestBody AlgoServerResource algoServerResource,
							HttpServletRequest request) {
		try {
			return innerAlgoService.registerServer(algoServerResource, request);
		} catch (Throwable throwable) {
			log.error("registerServer catch exception: {}", throwable.getMessage());
			return R.failed(throwable.getMessage());
		}
	}

	@Operation(summary = "心跳", description = "心跳")
	@GetMapping("/heartbeat")
	@Inner
	public R heartbeat(Integer port, HttpServletRequest request) {
		try {
			return innerAlgoService.heartbeat(port, request);
		} catch (Throwable throwable) {
			log.error("heartbeat catch exception: {}", throwable.getMessage());
			return R.failed(throwable.getMessage());
		}
	}

	@Operation(summary = "下载文件", description = "下载文件")
	@GetMapping("/file")
	@Inner
	public void downloadLabelFile(@RequestParam("datasetId") Long datasetId,
								  @RequestParam("labelId") Long taskId,
								  HttpServletResponse response) {
		try {
			innerAlgoService.downloadLabelFile(datasetId, taskId, response);
		} catch (Throwable throwable) {
			log.error("downloadLabelFile catch exception: ", throwable);
			HttpServletResponseUtils.handleDownloadError(response,
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					throwable.getMessage());
		}
	}
}
