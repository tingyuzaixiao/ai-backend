package com.train.platform.common.core.constant;

import cn.hutool.core.io.FileUtil;

/**
 * @author lee
 * @date 2025/2/1
 */
public interface CommonConstants {

	/**
	 * 未删除
	 */
	String STATUS_NOT_DEL = "0";

	/**
	 * 删除
	 */
	String STATUS_DEL = "1";

	/**
	 * 正常
	 */
	String STATUS_NORMAL = "0";

	/**
	 * 锁定
	 */
	String STATUS_LOCK = "9";

	/**
	 * 菜单树根节点
	 */
	Long MENU_TREE_ROOT_ID = -1L;

	/**
	 * 菜单
	 */
	String MENU = "0";

	/**
	 * 编码
	 */
	String UTF8 = "UTF-8";

	/**
	 * JSON 资源
	 */
	String CONTENT_TYPE = "application/json; charset=utf-8";

	/**
	 * 前端工程名
	 */
	String FRONT_END_PROJECT = "platform-ui";

	/**
	 * 后端工程名
	 */
	String BACK_END_PROJECT = "platform";

	/**
	 * 成功标记
	 */
	Integer SUCCESS = 0;

	/**
	 * 失败标记
	 */
	Integer FAIL = 1;

	/**
	 * 当前页
	 */
	String CURRENT = "current";

	/**
	 * size
	 */
	String SIZE = "size";

	/**
	 * 请求开始时间
	 */
	String REQUEST_START_TIME = "REQUEST-START-TIME";

	/**
	 * 数据集目录
	 */
	String DATASET_FOLDER = "dataset";

	/**
	 * 数据集目录
	 */
	String BASE_FOLDER = "base";

	/**
	 * 标注目录
	 */
	String LABEL_FOLDER = "annotations";

	/**
	 * 图像目录
	 */
	String IMG_FOLDER = "images";

	String MASK_FOLDER = "mask";

	/**
	 * 基础数据集图像顶层目录
	 */
	String BASE_IMAGE_FOLDER = "base" + FileUtil.FILE_SEPARATOR + "images";

	/**
	 * 基础数据集标签顶层目录
	 */
	String BASE_LABEL_FOLDER = "base" + FileUtil.FILE_SEPARATOR + "label";

	/**
	 * 模型目录
	 */
	String MODEL_FOLDER = "model";

	/**
	 * 项目目录
	 */
	String PROGRAM_FOLDER = "program";

	/**
	 * 参考标签目录
	 */
	String REFERENCE_FOLDER = "reference";

	/**
	 * 上传基础标签文件类型
	 */
	String UPLOAD_BASE_LABEL = "0";

	/**
	 * 上传标签文件类型
	 */
	String UPLOAD_LABEL = "1";


	/**
	 * 上传图像文件类型
	 */
	String UPLOAD_IMAGE = "2";

	/**
	 * 上传标签文件类型（参考标签）
	 */
	String UPLOAD_REFERENCE_LABEL = "3";

	/**
	 * 上传mask图像
	 */
	String UPLOAD_MASK_IMAGE = "4";

	/**
	 * 普通标签
	 */
	String ANNOTATION_COMMON_TYPE = "0";

	/**
	 * （标注类型）参考标签
	 */
	String ANNOTATION_REFERENCE_TYPE = "1";

	/**
	 * 训练
	 */
	Integer EXPERIMENT_TRAIN = 0;

	/**
	 * 校验
	 */
	Integer EXPERIMENT_VALIDATE = 1;

	/**
	 * 预测
	 */
	Integer EXPERIMENT_PREDICT = 2;

	/**
	 * 模型配置文件名称
	 */
	String CONFIG_JSON = "config.json";

	/**
	 * 模型训练配置文件名称
	 */
	String TRAIN_CONFIG_JSON = "trainConfig.json";

	/**
	 * 文件路径目录
	 */
	String CONFIG = "config";

	/**
	 * 权重路径目录
	 */
	String CHECKPOINT_FOLDER = "checkpoint";

	/**
	 * 默认目录
	 */
	String DEFAULT_FOLDER = "default";

	/**
	 * 基础模型目录名称
	 */
	String BASE_MODEL = "基础模型";

	/**
	 * 模型目录名称
	 */
	String MODEL = "模型";

	/**
	 * 算法服务器基础模型接口:样例
	 */
	String BASE_MODEL_URL = "http://127.1.0.3:9999/admin/baseModel/list";

	/**
	 * 模型训练结果常量
	 */
	String TRANIN_START_SUCCES = "开始训练成功";

	/**
	 * 模型训练结果常量
	 */
	String TRANIN_START_FAILED = "开始训练失败";

	/**
	 * 模型训练结果常量
	 */
	String TRANIN_PAUSE_SUCCES = "暂停训练成功";

	/**
	 * 模型训练结果常量
	 */
	String TRANIN_PAUSE_FAILED = "暂停训练失败";

	/**
	 * 占用
	 */
	String OCCUPY_GPU = "1";

	/**
	 * 未占用
	 */
	String FREE_GPU = "0";

	/**
	 * 0开始实验
	 */
	String EXPERIMENT_TAG_START = "0";

	/**
	 * 1训练中
	 */
	String EXPERIMENT_TRAINING = "1";


	/**
	 * 1暂停实验
	 */
	String EXPERIMENT_TAG_PAUSE = "1";

	/**
	 * 2暂停中
	 */
	String EXPERIMENT_PAUSING = "2";

	/**
	 * 2重新实验
	 */
	String EXPERIMENT_TAG_RESTART = "2";

	/**
	 * 3暂停后开始实验
	 */
	String EXPERIMENT_TAG_PAUSED_START = "3";

	/**
	 * 4完成实验
	 */
	String EXPERIMENT_TAG_FINISH = "4";

	/**
	 * 3、已完成
	 */
	String EXPERIMENT_FINISH = "3";


	String FILE_FUNCTION_DATASET = "0";

	String FILE_FUNCTION_MODEL = "1";

	String FILE_FUNCTION_PROGRAM = "2";

	String FILE_FUNCTION_EXPERIMENT = "3";

	String MODEL_BUCKET = "models";
	String WEIGHTS_BUCKET = "weights";
}
