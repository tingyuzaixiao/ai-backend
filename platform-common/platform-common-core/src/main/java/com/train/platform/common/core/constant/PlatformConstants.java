package com.train.platform.common.core.constant;

public interface PlatformConstants {

	/**
	 * 任务类型-标注模式
	 */
	String CLASSIFY_MODE = "0";
	String CLASSIFY_MODE_STR = "classification";

	String DETECTION_MODE = "1";
	String DETECTION_MODE_STR = "detection";
	String LINE_LABEL = "line_label";
	String CIRCLE_LABEL = "circle_label";
	String BBOX_LABEL = "bbox_label";

	String DETECTION_RECTANGLE_MODE = "0";
	String DETECTION_RECTANGLE_MODE_STR = "rectangle";
	String DETECTION_LINE_MODE = "1";
	String DETECTION_LINE_MODE_STR = "line";
	String DETECTION_CIRCLE_MODE = "2";
	String DETECTION_CIRCLE_MODE_STR = "circle";

	String SEGMENTATION_MODE = "2";
	String SEGMENTATION_MODE_STR = "segmentation";

	/**
	 * 数据类型
	 */
	String IMAGE_TYPE = "0";
	String TEXT_TYPE = "1";
	String AUDIO_TYPE = "2";
	String VIDEO_TYPE = "3";
	String POINT_CLOUD_TYPE = "4";
	String SLICING_TYPE = "5";
	String ZIP = "zip";
	String TAR = "tar";
	String GZ = "gz";
	String TAR_GZ = "tar.gz";


	/**
	 * 导入状态
	 */
	String NOT_IMPORTED = "0";
	String IMPORTING = "1";
	String IMPORT_PARSING = "2";
	String IMPORTED = "3";
	String IMPORT_UPLOADING = "4";
	String IMPORT_UPLOADED = "5";
	String IMPORT_FAILED = "9";

	/**
	 * 类型
	 */
	String IMG_TYPE = "png,jpeg,jpg,tiff,gif,psd,bmp,tif,svg";
	String ZIP_TYPE = "zip,tar,gz,tar.gz";

	/**
	 * 参数
	 */
	String TAG_ID = "tag_id";
	String TASK_ID = "task_id";
	String BUCKET_NAME = "bucketName";
	String BASE_DATASET_ID = "baseDatasetId";
	String BASE_DATASET_NAME = "baseDatasetName";
	String UPLOAD_TYPE = "uploadType";
	String LABELTYPE = "labelType";
	String DATA_TYPE = "dataType";
	String SUB_LABEL_TYPE = "subLabelType";
	String LABEL_TAG = "labelTag";
	String TAG_NAME = "tagName";
	String COLOR = "color";
	String IMAGES_STANDARD_FORMAT_PATH = "imagesStandardFormatPath";
	String LABEL_STANDARD_FORMAT_PATH = "labelStandardFormatPath";
	String LABEL_CONFIG_FILES = "labelConfigFiles";
	String FILE_LIST = "fileList";
	String FILE_NAME = "fileName";
	String CHUNK_INDEX = "chunkIndex";
	String FILE_MD5 = "fileMd5";
	String CHUNK_SIZE = "chunkSize";
	String CHUNK_TOTAL = "chunkTotal";
	String CHUNK = "chunk";
	String TOTAL_CHUNKS = "totalChunks";

	/**
	 * 标注
	 */
	String TASK_TYPE = "task_type";
	String LABEL_TYPE = "label_type";
	String ANNOTATIONS = "annotations";
	String INSTANCES = "instances";
	String METAINFO = "metainfo";
	String DATA_LIST = "data_list";
	String CLASSES_INDICES = "classes_indices";
	String IMG_PATH = "img_path";
	String MASK_PATH = "mask_path";
	String IMG_LABEL = "img_label";
	String FILE_ID = "file_id";
	String LABEL_ID = "label_id";

	/**
	 * 文件
	 */
	String COMPRESS_PACKAGE_TYPE = "1";

}
