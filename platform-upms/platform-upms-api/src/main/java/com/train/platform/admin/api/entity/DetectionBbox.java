package com.train.platform.admin.api.entity;

import lombok.Data;

@Data
public class DetectionBbox {
	private Integer xmin;
	private Integer ymin;
	private Integer xmax;
	private Integer ymax;

	@Override
	public String toString() {
		return "{" +
				"\"xmin\":" + xmin +
				", \"ymin\":" + ymin +
				", \"xmax\":" + xmax +
				", \"ymax\":" + ymax +
				'}';
	}
}
