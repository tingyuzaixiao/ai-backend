package com.train.platform.admin.api.vo;

import com.train.platform.admin.api.entity.BizExperiment;
import lombok.Data;


/**
 * @author zhouzhipeng
 * @date 2024/8/27 算法服务器返回VO对象
 */
@Data
public class TrainResPonseVO {

	private static final long serialVersionUID = 1L;

	/**
	 * 算法服务器返回状态码
	 */
	private int code;

	/**
	 * 算法服务器返回msg
	 */
	private String msg;

	/**
	 * 算法服务器返回data
	 */
	private BizExperiment bizExperiment;

}
