package com.train.platform.admin.api.vo;

import lombok.Data;

/**
 * @author zhouzhipeng
 * @date 2024/8/27 调用训练方法参数vo
 */
@Data
public class TrainVO {

	private static final long serialVersionUID = 1L;

	/**
	 * 训练ID
	 */
	private Long trainId;

	/**
	 * 训练名称
	 */
	private String trainName;

	/**
	 * 关联的模型ID
	 */
	private Long modelId;

	/**
	 * 关联的数据集ID
	 */
	private Long datasetId;


	/**
	 * 训练指标对应的log路径
	 */
	private String metricsPath;

	/**
	 * 训练算法
	 */
	private String algorithms;

	/**
	 * 超参数
	 */
	private String hyperParameters;

	/**
	 * 机器资源
	 */
	private String gpuId;

	/**
	 * 训练起始的权重id
	 */
	private Long checkpointId;

	/**
	 * 训练起始的权重存放路径
	 */
	private String checkpointPath;

	/**
	 * 模型参数
	 */
	private String modelParameters;

	/**
	 * 训练指标
	 */
	private String trainMetrics;

	/**
	 * 是否预训练
	 */
	private Boolean isPreTrain;

	/**
	 * 是否重新训练
	 */
	private Boolean isRestartTrain;

	/**
	 * 训练进度
	 */
	private String progress;


}
