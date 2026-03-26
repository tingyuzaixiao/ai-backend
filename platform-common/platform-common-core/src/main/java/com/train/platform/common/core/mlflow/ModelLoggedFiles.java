package com.train.platform.common.core.mlflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @author zj
 * @date 2025/9/30 mlflow查询logged model返回类型
 */
@Data
public class ModelLoggedFiles {
	@Data
	public static class File {
		@JsonProperty("path")
		private String path;

		@JsonProperty("is_dir")
		private Boolean dir;
	}

	@JsonProperty("root_uri")
	private String rootUri;

	@JsonProperty("files")
	private List<File> files;
}
