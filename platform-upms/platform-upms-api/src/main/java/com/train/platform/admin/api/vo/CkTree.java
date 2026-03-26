package com.train.platform.admin.api.vo;

import lombok.Data;

import java.util.List;

@Data
public class CkTree {
	private String label;

	private String value;

	private boolean disabled;

	private List<CkTree> children;
}
