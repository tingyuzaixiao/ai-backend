package com.train.platform.admin.controller;

import com.train.platform.common.core.util.R;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/webrtc")
@Tag(description = "webrtc", name = "webrtc模块")
//@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class WebRTCController {

	// 简化版的信令交换
	@PostMapping("/signal")
	public R signal(@RequestBody Map<String, Object> signal) {
		Map<String, Object> response = new HashMap<>();

		// 实际应使用WebSocket实现完整的信令交换
		// 这里返回模拟的answer
		if (signal.containsKey("offer")) {
			Map<String, Object> answer = new HashMap<>();
			answer.put("type", "answer");
			answer.put("sdp", "v=0\r\no=...");
			response.put("answer", answer);
		}

		return R.ok(response);
	}
}
