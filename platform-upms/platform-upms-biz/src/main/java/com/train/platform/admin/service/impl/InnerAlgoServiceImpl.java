package com.train.platform.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.train.platform.admin.api.vo.InnerServerStartVO;
import com.train.platform.admin.api.vo.InnerServerStopVO;
import com.train.platform.admin.api.entity.AlgoServerResource;
import com.train.platform.admin.api.entity.BizExperiment;
import com.train.platform.admin.mapper.service.AlgoServerResourceMapperService;
import com.train.platform.admin.service.BizLabelTaskService;
import com.train.platform.admin.service.InnerAlgoService;
import com.train.platform.common.core.constant.enums.GpuStatus;
import com.train.platform.common.core.util.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class InnerAlgoServiceImpl implements InnerAlgoService {
	@Autowired
	private AlgoServerResourceMapperService resourceMapperService;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private BizLabelTaskService bizLabelTaskService;

	private static final int BUFFER_SIZE = 1024 * 1024;

	private static final String ALGO_SERVER_URL_FORMAT = "http://%s/api/";
	private static final String ALGO_START_API = "start";
	private static final String ALGO_STOP_API = "stop";

	private static final String REGISTER_SERVER_SUCCESS = "register success";
	private static final String HEART_BEAT_OK = "ok";

	private static final int BACKEND_TASK_PERIOD_S = 15;
	private static final int HEARTBEAT_TIMEOUT_MINUTE = 5;


	private final ScheduledTaskManager taskManager;
	{
		taskManager = new ScheduledTaskManager("InnerAlgoServer", 4);
	}

	private static final int LOCK_NUM = 16;
	private final Object[] locks = new Object[LOCK_NUM];
	{
		for (int i = 0; i < LOCK_NUM; ++i) {
			locks[i] = new Object();
		}
	}

	@PostConstruct
	public void init() {
		taskManager.scheduleWithFixedDelay(
				this::startInnerServerTask,
				0,
				BACKEND_TASK_PERIOD_S,
				TimeUnit.SECONDS);
	}

	@Override
	public R registerServer(AlgoServerResource algoServerResource, HttpServletRequest request) {
		String clientIp = IpUtils.normalizeIp(request.getRemoteAddr());
		int clientPort = Integer.parseInt(algoServerResource.getServer());
		String server = clientIp + ":" + clientPort;
		algoServerResource.setServer(server);


		algoServerResource.setHeartbeatTime(LocalDateTime.now());
		algoServerResource.setDelFlag("0");

		synchronized (LockUtils.getLock(server, locks)) {
			AlgoServerResource query = new AlgoServerResource();
			query.setServer(server);
			List<AlgoServerResource> servers = resourceMapperService.getBaseMapper()
					.getAlgoServerResources(query);

			LocalDateTime now = LocalDateTime.now();
			if (servers == null || servers.isEmpty()) {
				algoServerResource.setCreateTime(now);
				algoServerResource.setUpdateTime(now);
				algoServerResource.setStatus(GpuStatus.FREE.name());
				resourceMapperService.save(algoServerResource);
			} else {
				algoServerResource.setId(servers.get(0).getId());
				algoServerResource.setUpdateTime(now);
				resourceMapperService.getBaseMapper().updateByRegister(algoServerResource);
			}
		}
		return R.ok(REGISTER_SERVER_SUCCESS);
	}

	@Override
	public R heartbeat(Integer port, HttpServletRequest request) {
		if (port == null || port < 0) {
			return R.failed("port is invalid");
		}

		String clientIp = IpUtils.normalizeIp(request.getRemoteAddr());
		String server = clientIp + ":" + port;

		AlgoServerResource algoServerResource = new AlgoServerResource();
		algoServerResource.setHeartbeatTime(LocalDateTime.now());
		algoServerResource.setDelFlag("0");

		synchronized (LockUtils.getLock(server, locks)) {
			AlgoServerResource query = new AlgoServerResource();
			query.setServer(server);
			List<AlgoServerResource> servers = resourceMapperService.getBaseMapper()
					.getAlgoServerResources(query);

			if (servers == null || servers.isEmpty()) {
				return R.failed("please register first");
			} else {
				algoServerResource.setId(servers.get(0).getId());
				algoServerResource.setUpdateTime(LocalDateTime.now());
				resourceMapperService.getBaseMapper().updateHeartbeat(algoServerResource);
			}
		}
		return R.ok(HEART_BEAT_OK);
	}

	@Override
	public void downloadLabelFile(Long datasetId, Long taskId, HttpServletResponse response) {
		List<String> labelContents = bizLabelTaskService.getJsonLabels(datasetId, taskId);
		if (labelContents == null || labelContents.isEmpty()) {
			throw new RuntimeException("label task not exist");
		}

		String fileName = labelContents.get(0) + ".json";
		String encodedFileName = java.net.URLEncoder.encode(fileName, StandardCharsets.UTF_8)
				.replace("+", "%20");
		HttpHeaders headers = new HttpHeaders();
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName);
		response.setContentType("application/json; charset=UTF-8");

		String content = labelContents.get(1);
		try (Writer writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
			final int CHUNK_SIZE = 1024 * 1024;
			int length = content.length();
			int offset = 0;
			while (offset < length) {
				int end = Math.min(offset + CHUNK_SIZE, length);
				writer.write(content, offset, end - offset);
				offset = end;
				writer.flush();
			}
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void startInnerServerTask() {
		backendTaskForHeartbeat();
	}

	private void backendTaskForHeartbeat() {
		resourceMapperService.getBaseMapper().
				logicDeleteByHeartbeat(LocalDateTime.now(), getHeartBeatTimeout());
		// 注意：此处不将该训练服务上的训练任务设置为失败
	}

	private static LocalDateTime getHeartBeatTimeout() {
		return LocalDateTime.now().minusMinutes(HEARTBEAT_TIMEOUT_MINUTE);
	}

	@Override
	public void startRunning(BizExperiment experiment, String programName, AlgoServerResource algoServerResource) {
		Map<String, String> tags = new HashMap<>(1);
		tags.put("description", experiment.getDescription());
		InnerServerStartVO startRequestPayload = new InnerServerStartVO(
				experiment.getId(),
				experiment.getExperimentName(),
				programName,
				experiment.getHyperParameters(),
				experiment.getDatasetId(),
				experiment.getTaskId(),
				tags);
		log.info("startRequestPayload: {}", JacksonUtils.serialize(startRequestPayload));

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		String algoServerUrl = String.format(ALGO_SERVER_URL_FORMAT, algoServerResource.getServer());
		String url = UriFileUtils.appendUrlPath(algoServerUrl, ALGO_START_API);

		log.info("start url: {}", url);
		HttpEntity<InnerServerStartVO> requestEntity = new HttpEntity<>(startRequestPayload, headers);

		ResponseEntity<String> response = null;
		try {
			response = restTemplate.exchange(
					url,
					HttpMethod.POST,
					requestEntity,
					String.class);
		} catch (Exception e) {
			log.error("startRunning调用外部服务失败: {}", e.getMessage());
			throw new RuntimeException("startRunning调用外部服务失败: " + e.getMessage());
		}

		HttpStatusCode statusCode = response.getStatusCode();
		if (statusCode.is2xxSuccessful()) {
			String responseBody = response.getBody();
			log.info("server start response: {}", responseBody);

			TypeReference<R<String>> typeRef = new TypeReference<>() {};
			R<String> res = JacksonUtils.deserialize(responseBody, typeRef);
			if (res.getCode() != 0) {
				throw new RuntimeException("server start failed，" + res.getMsg());
			}
		} else {
			String errorBody = response.getBody();
			throw new RuntimeException("request failed, statusCode: " + statusCode + ", msg: " + errorBody);
		}
	}

	@Override
	public void stopRunning(Long resourceId, Integer experimentStopType) {
		AlgoServerResource algoServerResource = resourceMapperService.getBaseMapper().
				selectOne(Wrappers.<AlgoServerResource>lambdaQuery().
						eq(AlgoServerResource::getId, resourceId));
		if (algoServerResource == null) {
			throw new RuntimeException("algo server not exist");
		}
		if (algoServerResource.getDelFlag().equals("1")) {
			throw new RuntimeException("algo server not connected");
		}

		InnerServerStopVO stopRequestPayload = new InnerServerStopVO(experimentStopType);
		log.info("stopRequestPayload: {}", JacksonUtils.serialize(stopRequestPayload));

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		String algoServerUrl = String.format(ALGO_SERVER_URL_FORMAT, algoServerResource.getServer());
		String url = UriFileUtils.appendUrlPath(algoServerUrl, ALGO_STOP_API);

		HttpEntity<InnerServerStopVO> requestEntity = new HttpEntity<>(stopRequestPayload, headers);
		ResponseEntity<String> response = null;
		try {
			response = restTemplate.exchange(
					url,
					HttpMethod.POST,
					requestEntity,
					String.class);
		} catch (Exception e) {
			log.error("stopRunning调用外部服务失败: {}", e.getMessage());
			throw new RuntimeException("stopRunning调用外部服务失败: " + e.getMessage());
		}

		HttpStatusCode statusCode = response.getStatusCode();
		if (statusCode.is2xxSuccessful()) {
			String responseBody = response.getBody();
			log.info("server stop response: {}", responseBody);

			TypeReference<R<String>> typeRef = new TypeReference<>() {};
			R<String> res = JacksonUtils.deserialize(responseBody, typeRef);
			if (res.getCode() != 0) {
				throw new RuntimeException("server stop failed，" + res.getMsg());
			}
		} else {
			String errorBody = response.getBody();
			throw new RuntimeException("request failed, statusCode: " + statusCode + ", msg: " + errorBody);
		}
	}
}
