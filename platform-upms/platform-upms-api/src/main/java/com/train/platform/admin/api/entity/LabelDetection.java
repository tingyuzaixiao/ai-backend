package com.train.platform.admin.api.entity;

import lombok.Data;

@Data
public class LabelDetection {
	private DetectionBbox bbox;
	private Integer bbox_label;
	private Integer truncated;
	private Integer area;

	@Override
	public String toString() {
		return "{" +
				"\"bbox\":" + bbox +
				", \"bbox_label\":" + bbox_label +
				", \"truncated\":" + truncated +
				", \"area\":" + area +
				'}';
	}
}
