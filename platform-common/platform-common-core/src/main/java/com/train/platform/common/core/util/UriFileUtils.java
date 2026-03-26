package com.train.platform.common.core.util;

import lombok.extern.slf4j.Slf4j;
import org.mlflow_project.apachehttp.client.utils.URIBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.apache.commons.io.IOUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;


/**
 * @author zj
 * @date 2025/9/26 url file类
 */
@Slf4j
public class UriFileUtils {
	private static final int BUFFER_SIZE = 8 * 1024;

	public static URIBuilder newURIBuilder(String base) {
		try {
			return new URIBuilder(base);
		} catch (URISyntaxException e) {
			throw new RuntimeException("Failed to construct URIBuilder for " + base, e);
		}
	}

	public static URI newURI(String base) {
		try {
			return new URI(base);
		} catch (URISyntaxException e) {
			throw new RuntimeException("Failed to construct URI for " + base, e);
		}
	}

	public static String appendUrlPath(String basePath, String... paths) {
		URI baseUri = newURI(basePath);
		for (String path : paths) {
			URI pathUri = newURI(path);
			baseUri = baseUri.resolve(pathUri);
		}
		return baseUri.toString();
	}

	public static HttpHeaders getRemoteFileHeaders(RestTemplate restTemplate, String fileUrl) {
		return restTemplate.headForHeaders(fileUrl);
	}

	public static String extractFileName(HttpHeaders headers, String fileUrl) {
		String contentDisposition = headers.getFirst(HttpHeaders.CONTENT_DISPOSITION);
		if (contentDisposition != null && contentDisposition.contains("filename=")) {
			String fileName = contentDisposition.substring(contentDisposition.indexOf("filename=") + 9);
			return fileName.replace("\"", "");
		}

		String fileName = PathUtils.resolveUrlFileName(fileUrl);
		if (fileName.contains("?")) {
			fileName = fileName.substring(0, fileName.indexOf('?'));
		}
		return fileName;
	}

	public static String extractContentDisposition(HttpHeaders headers) {
		String contentDisposition = headers.getFirst(HttpHeaders.CONTENT_DISPOSITION);
		if (contentDisposition == null || !contentDisposition.contains("attachment")) {
			return "attachment";
		}
		return contentDisposition;
	}


	public static void writeFile(RestTemplate restTemplate,
								 HttpMethod httpMethod,
								 String fileUrl,
								 String dstFile) {
		restTemplate.execute(
				fileUrl,
				httpMethod,
				null,
				clientHttpResponse -> {
					if (!clientHttpResponse.getStatusCode().is2xxSuccessful()) {
						throw new IOException("HTTP request failed: " + clientHttpResponse.getStatusCode());
					}

					try (InputStream inputStream = clientHttpResponse.getBody();
						 OutputStream outputStream = new FileOutputStream(dstFile)) {
						IOUtils.copy(inputStream, outputStream, BUFFER_SIZE);
						outputStream.flush();
						log.info("file saved at {}", dstFile);
					} catch (IOException e) {
						throw new RuntimeException(String.format("file: %s download failed", fileUrl), e);
					}
					return null;
				}
		);
	}

	public static void transferFile(RestTemplate restTemplate,
									HttpMethod httpMethod,
									String fileUrl,
									OutputStream outputStream) {
		restTemplate.execute(
				fileUrl,
				httpMethod,
				null,
				clientHttpResponse -> {
					if (!clientHttpResponse.getStatusCode().is2xxSuccessful()) {
						throw new IOException("HTTP request failed: " + clientHttpResponse.getStatusCode());
					}

					InputStream inputStream = clientHttpResponse.getBody();

					IOUtils.copy(inputStream, outputStream, BUFFER_SIZE);
					return null;
				}
		);
	}

	public static void main(String[] args) {
		System.out.println(UriFileUtils.appendUrlPath("http://127.0.0.1:8001", "start"));
	}
}
