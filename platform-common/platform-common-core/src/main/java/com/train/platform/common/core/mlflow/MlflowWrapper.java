package com.train.platform.common.core.mlflow;

import com.train.platform.common.core.util.FileUtils;
import com.train.platform.common.core.util.HttpServletResponseUtils;
import com.train.platform.common.core.util.PathUtils;
import com.train.platform.common.core.util.UriFileUtils;
import org.apache.commons.lang3.StringUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.mlflow.api.proto.Service;
import org.mlflow.tracking.*;
import org.mlflow_project.apachehttp.client.utils.URIBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zj
 * @date 2025/9/22 mlflow包装
 */
@Slf4j
public class MlflowWrapper {
	private static final int MAX_RESULTS = 10;

	private static final String LM_PATH_FORMAT = "logged-models/%s/artifacts/directories";
	private static final String LM_REQUEST_PARAM = "artifact_directory_path";
	private static final String LM_DOWNLOAD_FORMAT = "ajax-api/2.0/mlflow/logged-models/%s/artifacts/files?artifact_file_path=%s";
	private static final String Artifact_DOWNLOAD_FORMAT = "get-artifact?path=%s&run_uuid=%s";

	private static final String CONTENT_DISPOSITION_FORMAT = "attachment; filename=%s";

	private static final String IGNORED_MEG = "RESOURCE_DOES_NOT_EXIST";

	private final String trackingUri;
	private final MlflowClient mlflowClient;

	private final RestTemplate restTemplate = new RestTemplate();


	public MlflowWrapper(String trackingUri) {
		log.info("trackingUri: {}", trackingUri);
		this.trackingUri = trackingUri;
		mlflowClient = new MlflowClient(trackingUri);
	}

	public String createExperiment(String experimentName) {
		return mlflowClient.createExperiment(experimentName);
	}

	public void renameExperiment(String experimentId, String newName) {
		mlflowClient.renameExperiment(experimentId, newName);
	}

	public void deleteExperiment(String experimentId) {
		try {
			mlflowClient.deleteExperiment(experimentId);
		} catch (MlflowHttpException e) {
			if (!e.getMessage().contains(IGNORED_MEG)) {
				throw e;
			}
		}
	}

	public void setTerminated(String runId) {
		mlflowClient.setTerminated(runId, Service.RunStatus.KILLED);
	}

	public List<Service.Experiment> searchExperiment(String experimentName) {
		ExperimentsPage experimentsPage = mlflowClient.searchExperiments(
				String.format("attribute.name='%s'", experimentName),
				Service.ViewType.ACTIVE_ONLY,
				MAX_RESULTS,
				List.of("attribute.creation_time DESC")
		);
		if (!experimentsPage.hasNextPage()) {
			return experimentsPage.getItems();
		}

		List<Service.Experiment> experiments = new ArrayList<>(experimentsPage.getItems());
		Page<Service.Experiment> page = experimentsPage;
		while (page.hasNextPage()) {
			page = experimentsPage.getNextPage();
			experiments.addAll(((ExperimentsPage)page).getItems());
		}
		return experiments;
	}

	public Service.Run getRun(String runId) {
		return mlflowClient.getRun(runId);
	}

	public void deleteRun(String runId) {
		try {
			mlflowClient.deleteRun(runId);
		} catch (MlflowHttpException e) {
			if (!e.getMessage().contains(IGNORED_MEG)) {
				throw e;
			}
		}
	}

	public List<Service.Run> searchRunsByName(String experimentId,
											  String runName) {
		RunsPage runsPage = mlflowClient.searchRuns(
				List.of(experimentId),
				String.format("attribute.run_name='%s'", runName),
				Service.ViewType.ACTIVE_ONLY,
				MAX_RESULTS,
				List.of("attribute.start_time DESC"));
		if (!runsPage.hasNextPage()) {
			return runsPage.getItems();
		}

		List<Service.Run> runs = new ArrayList<>(runsPage.getItems());
		Page<Service.Run> page = runsPage;
		while (page.hasNextPage()) {
			page = runsPage.getNextPage();
			runs.addAll(((RunsPage)page).getItems());
		}
		return runs;
	}

	public List<Service.Run> searchRunsByIds(String experimentId,
											 List<String> runIds) {
		if (runIds == null || runIds.isEmpty()) {
			return null;
		}
		StringBuilder sb = new StringBuilder("attribute.run_id IN(");
		for (int i = 0; i < runIds.size(); ++i) {
			if (i == 0) {
				sb.append("'");
			} else {
				sb.append(",'");
			}
			sb.append(runIds.get(i));
			sb.append("'");
		}
		sb.append(")");
		String searchFilter = sb.toString();
		log.info("searchRunsByIds searchFilter: {}", searchFilter);

		RunsPage runsPage = mlflowClient.searchRuns(
				List.of(experimentId),
				searchFilter,
				Service.ViewType.ACTIVE_ONLY,
				MAX_RESULTS,
				List.of("attribute.start_time DESC"));
		if (!runsPage.hasNextPage()) {
			return runsPage.getItems();
		}

		List<Service.Run> runs = new ArrayList<>(runsPage.getItems());
		Page<Service.Run> page = runsPage;
		while (page.hasNextPage()) {
			page = runsPage.getNextPage();
			runs.addAll(((RunsPage)page).getItems());
		}
		return runs;
	}

	public List<Service.Metric> getMetricHistory(String runId, String key) {
		List<Service.Metric> metrics = mlflowClient.getMetricHistory(runId, key);
		int size = metrics.size();
		if (size > 1) {
			metrics.remove(size - 1);
		}
		return metrics;
	}

	public List<Service.FileInfo> listArtifacts(String runId, String artifactPath) {
		if (StringUtils.isBlank(artifactPath)) {
			return mlflowClient.listArtifacts(runId);
		}
		return mlflowClient.listArtifacts(runId, artifactPath);
	}

	public File downloadArtifacts(String runId, String artifactPath) {
		if (StringUtils.isBlank(artifactPath)) {
			return mlflowClient.downloadArtifacts(runId);
		}
		return mlflowClient.downloadArtifacts(runId, artifactPath);
	}

	public String getArtifactDownloadUri(String runId, String file) {
		return UriFileUtils.appendUrlPath(trackingUri, String.format(Artifact_DOWNLOAD_FORMAT, file, runId));
	}

	public void downloadAndUploadArtifacts(HttpServletResponse response, String runId, String artifactFile) {
//        if (StringUtils.isBlank(artifactFile)) {
//            throw new IllegalArgumentException("artifactPath is blank");
//        }
//        String fileName = PathUtils.resolveFileName(artifactFile);
//        if (StringUtils.isBlank(fileName)) {
//            throw new IllegalArgumentException("artifactPath is invalid");
//        }
//
//        File file = mlflowClient.downloadArtifacts(runId, artifactFile);
//        try {
//            FileUtils.copyFile(file, response.getOutputStream());
//        } catch (Throwable throwable) {
//            throw new RuntimeException(throwable);
//        }
//        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
//        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, String.format(CONTENT_DISPOSITION_FORMAT, fileName));
		String path = getArtifactDownloadUri(runId, artifactFile);
		log.info("artifact download path: {}", path);
		transferFile(response, path);
	}

	public String listLMFiles(String modelId, String requestPath) {
		String path = null;
		if (StringUtils.isBlank(requestPath)) {
			path = String.format(LM_PATH_FORMAT, modelId);
		} else {
			URIBuilder builder = UriFileUtils.newURIBuilder(String.format(LM_PATH_FORMAT, modelId))
					.setParameter(LM_REQUEST_PARAM, requestPath);
			path = builder.toString();
		}
		log.info("listLMFiles path: {}", path);
		return mlflowClient.sendGet(path);
	}

	public String getLMDownloadUri(String modelId, String file) {
		return UriFileUtils.appendUrlPath(trackingUri, String.format(LM_DOWNLOAD_FORMAT, modelId, file));
	}

	public String downloadLMFiles(String modelId, String file, String dstPath) {
		String path = getLMDownloadUri(modelId, file);
		log.info("downloadLMFiles path: {}", path);

		HttpHeaders headers = UriFileUtils.getRemoteFileHeaders(restTemplate, path);
		if (headers.isEmpty() || headers.getContentType() == null) {
			throw new RuntimeException("Failed to get file headers from " + path);
		}
		String fileName = UriFileUtils.extractFileName(headers, path);
		String dstFile = Paths.get(dstPath, fileName).toString();
		UriFileUtils.writeFile(restTemplate, HttpMethod.GET, path, dstFile);
//        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
//        ResponseEntity<byte[]> responseEntity = restTemplate.exchange(path, HttpMethod.GET, requestEntity, byte[].class);
//        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
//            throw new RuntimeException(String.format("Get path: %s failed with status: %d", path, responseEntity.getStatusCode().value()));
//        }
		return dstFile;
	}

	public void downloadAndUploadLMFiles(HttpServletResponse response, String modelId, String file) {
		String path = getLMDownloadUri(modelId, file);
		log.info("lm download path: {}", path);
		transferFile(response, path);
	}

	private void transferFile(HttpServletResponse response, String path) {
		HttpHeaders headers = UriFileUtils.getRemoteFileHeaders(restTemplate, path);
		if (headers.isEmpty() || headers.getContentType() == null) {
			HttpServletResponseUtils.handleDownloadError(response,
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Failed to get file headers from " + path);
			return;
		}

		response.reset();
		String contentType = headers.getContentType().toString();
		response.setContentType(contentType);
		log.info("download Content-Type: {}", contentType);
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, UriFileUtils.extractContentDisposition(headers));
		log.info("download Content-Disposition: {}", response.getHeader(HttpHeaders.CONTENT_DISPOSITION));

		try {
			UriFileUtils.transferFile(restTemplate, HttpMethod.GET, path, response.getOutputStream());
		} catch (Throwable throwable) {
			HttpServletResponseUtils.handleDownloadError(response,
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					throwable.getMessage());
			return;
		}

		try {
			response.getOutputStream().flush();
		} catch (IOException e) {
			log.error("lm file download failed (client disconnected): {}", e.getMessage());
			return;
		}
	}
}
