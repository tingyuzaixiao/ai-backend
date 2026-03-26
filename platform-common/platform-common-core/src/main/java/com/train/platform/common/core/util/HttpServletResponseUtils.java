package com.train.platform.common.core.util;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zj
 * @date 2025/9/30 HttpServletResponse工具类
 */
@Slf4j
public class HttpServletResponseUtils {
	private static final int BUFFER_SIZE = 8 * 1024;

	public static void handleDownloadError(HttpServletResponse response, int statusCode, String errMsg) {
		if (!response.isCommitted()) {
			try {
				response.reset();
				response.setStatus(statusCode);
				response.setContentType("application/json;charset=UTF-8");

				try (OutputStream out = response.getOutputStream()) {
					Map<String, Object> errorResponse = new HashMap<>();
					errorResponse.put("code", statusCode);
					errorResponse.put("message", errMsg);
					errorResponse.put("timestamp", System.currentTimeMillis());

					JacksonUtils.getObjectMapper().writeValue(out, errorResponse);
				}
			} catch (IOException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
	}

	public static void downloadFromLocalFile(HttpServletResponse response, String file) {
		Path filePath = Path.of(file);
		if (!Files.exists(filePath)) {
			HttpServletResponseUtils.handleDownloadError(response,
					HttpServletResponse.SC_NOT_FOUND,
					"file not found");
			return;
		}

		String contentType = null;
		try {
			contentType = Files.probeContentType(filePath);
		} catch (IOException e) {
			HttpServletResponseUtils.handleDownloadError(response,
					HttpServletResponse.SC_BAD_REQUEST,
					e.getMessage());
			return;
		}
		if (contentType == null) {
			contentType = "application/octet-stream";
		}

		response.setContentType(contentType);
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +
				URLEncoder.encode(filePath.getFileName().toString(), StandardCharsets.UTF_8) + "\"");

		try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
			IOUtils.copy(inputStream, response.getOutputStream(), BUFFER_SIZE);
			response.getOutputStream().flush();
		} catch (IOException e) {
			log.error("file download failed (client disconnected): {}", e.getMessage());
			return;
		}
	}
}
