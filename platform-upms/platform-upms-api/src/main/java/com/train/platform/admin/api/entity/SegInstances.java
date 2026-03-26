package com.train.platform.admin.api.entity;

import lombok.Data;

@Data
public class SegInstances {
	private Long segmentation_label;
	private Integer segmentation_pixel_value;
	private Integer area;
	private Integer truncated;
	private int[] rle;
}
