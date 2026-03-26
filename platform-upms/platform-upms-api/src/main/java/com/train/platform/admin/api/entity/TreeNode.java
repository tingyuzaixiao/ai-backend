package com.train.platform.admin.api.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TreeNode {
	private String id;          // 节点的唯一标识符
	private String label;       // 节点的显示名称
	private String parentId;    // 父节点的标识符
	private List<TreeNode> children; // 子节点列表

	// 构造函数
	public TreeNode(String id, String label, String parentId) {
		this.id = id;
		this.label = label;
		this.parentId = parentId;
		this.children = new ArrayList<>(); // 初始化子节点列表
	}
}
