package com.train.platform.common.core.mlflow;

import io.micrometer.common.util.StringUtils;
import org.mlflow.api.proto.Service;

import java.util.List;

/**
 * @author zj
 * @date 2025/9/30 mlflow工具类
 */
public class MlflowHelper {
	private static final String PROGRESS = "progress";
	private static final String TAG_IGNORED_PREFIX = "mlflow.";


	public static void parseMetrics(List<Service.Metric> metrics,
									List<String> metricNames,
									List<Double> metricVals) {
		if (metrics == null || metrics.isEmpty()) {
			return;
		}

		for (Service.Metric metric : metrics) {
			metricNames.add(metric.getKey());
			metricVals.add(metric.getValue());
		}
	}

	public static void parseParams(List<Service.Param> params,
								   List<String> paramNames,
								   List<String> paramVals) {
		if (params == null || params.isEmpty()) {
			return;
		}

		for (Service.Param param : params) {
			paramNames.add(param.getKey());
			paramVals.add(param.getValue());
		}
	}

	public static void parseTags(List<Service.RunTag> tags,
								 List<String> tagNames,
								 List<String> tagVals) {
		if (tags == null || tags.isEmpty()) {
			return;
		}

		for (Service.RunTag tag : tags) {
			String key = tag.getKey();
			if (key.startsWith(TAG_IGNORED_PREFIX)) {
				continue;
			}
			tagNames.add(key);
			tagVals.add(tag.getValue());
		}
	}

	public static String parseModelId(Service.RunOutputs runOutputs) {
		List<Service.ModelOutput> modelOutputs = runOutputs.getModelOutputsList();
		if (modelOutputs == null || modelOutputs.isEmpty()) {
			return null;
		}
		for (Service.ModelOutput modelOutput : modelOutputs) {
			String modelId = modelOutput.getModelId();
			if (StringUtils.isNotBlank(modelId)) {
				return modelId;
			}
		}
		return null;
	}

	public static Long parseDuration(Service.RunInfo runInfo) {
		if (Service.RunStatus.FINISHED.equals(runInfo.getStatus())) {
			long mills = runInfo.getEndTime() - runInfo.getStartTime();
			return mills / 1000;
		}
		return null;
	}

	public static Double parseProgress(List<Service.Param> params) {
		if (params == null || params.isEmpty()) {
			return null;
		}

		for (Service.Param param : params) {
			if (PROGRESS.equals(param.getKey())) {
				String strVal = param.getValue();
				return Double.parseDouble(strVal);
			}
		}
		return null;
	}
}
