package com.train.platform.admin.api.util;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class RestTemplateUtil {

	private static final RestTemplate restTemplate = new RestTemplate();

	// 发送POST请求，携带JSON数据体
	public static ResponseEntity sendHttpPostWithJson(String url, String jsonPayload) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);
		ResponseEntity<String> response = restTemplate.exchange(
				url,
				HttpMethod.POST,
				entity,
				String.class
		);
		return response;
	}
}
