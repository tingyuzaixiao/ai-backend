package com.train.platform.admin.service;

import com.train.platform.admin.api.entity.AlgoServerResource;
import com.train.platform.admin.api.entity.BizExperiment;
import com.train.platform.common.core.util.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;


public interface InnerAlgoService {
	R registerServer(AlgoServerResource algoServerResource, HttpServletRequest request);

	R heartbeat(Integer port, HttpServletRequest request);

	void downloadLabelFile(Long datasetId, Long taskId, HttpServletResponse response);

	void startRunning(BizExperiment experiment, String programName, AlgoServerResource algoServerResource);

	void stopRunning(Long resourceId, Integer experimentStopType);
}
