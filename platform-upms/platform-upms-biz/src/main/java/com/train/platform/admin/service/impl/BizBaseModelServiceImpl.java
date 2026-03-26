package com.train.platform.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.train.platform.admin.api.dto.BaseModelDTO;
import com.train.platform.admin.api.entity.BizBaseModel;
import com.train.platform.admin.api.entity.BizExperiment;
import com.train.platform.admin.api.entity.BizModel;
import com.train.platform.admin.api.entity.SysDeletedDir;
import com.train.platform.admin.api.util.RestTemplateUtil;
import com.train.platform.admin.api.vo.BaseModelVO;
import com.train.platform.admin.mapper.BizBaseModelMapper;
import com.train.platform.admin.mapper.BizExperimentMapper;
import com.train.platform.admin.mapper.BizModelMapper;
import com.train.platform.admin.service.BizBaseModelService;
import com.train.platform.admin.service.SysDeletedDirService;
import com.train.platform.admin.service.SysFileService;
import com.train.platform.common.core.util.FileUtils;
import com.train.platform.common.core.util.R;
import com.train.platform.common.file.core.FileProperties;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.train.platform.common.core.constant.CommonConstants.*;

/**
 * <p>
 * 基础模型管理 服务实现类
 * </p>
 *
 * @author zhouzhipeng
 * @since 2024-01-20
 */
@Service
public class BizBaseModelServiceImpl extends ServiceImpl<BizBaseModelMapper, BizBaseModel> implements BizBaseModelService {

	@Autowired
	private SysFileService sysFileService;
	@Autowired
	private FileProperties properties;

	@Autowired
	private BizModelMapper bizModelMapper;

	@Autowired
	private BizExperimentMapper bizExperimentMapper;

	@Autowired
	private SysDeletedDirService sysDeletedDirService;

	private final static ObjectMapper objectMapper = new ObjectMapper();

	private final String BASE_MODEL_SUCCES = "基础模型创建成功";

	private final String BASE_MODEL_FAILED = "基础模型创建失败";

	@Override
	public R saveBaseModel() {
		try {
			// 调用算法服务端接口，获取基础模型相关信息
			ResponseEntity<String> response = RestTemplateUtil.sendHttpPostWithJson(BASE_MODEL_URL, "");
			if (response.getStatusCode().is2xxSuccessful()) {
				try {
					// 接口返回的应该是一个基础模型的list
					JavaType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, BizBaseModel.class);
					List<BizBaseModel> responseList = objectMapper.readValue(response.getBody(), listType);
					// 使用responseList做进一步处理
					for (BizBaseModel bizBaseModel : responseList) {
						String modelConfig = bizBaseModel.getConfig();
						String trainConfig = bizBaseModel.getTrainConfig();
						String modelName = bizBaseModel.getBaseModelName();
						String ConfigPath = MODEL_FOLDER + FileUtil.FILE_SEPARATOR + modelName + BASE_MODEL + FileUtil.FILE_SEPARATOR + CONFIG;
						sysFileService.createLocalFile(ConfigPath, CONFIG_JSON, modelConfig);
						sysFileService.createLocalFile(ConfigPath, TRAIN_CONFIG_JSON, trainConfig);
						bizBaseModel.setConfig(ConfigPath + FileUtil.FILE_SEPARATOR + CONFIG_JSON);
						bizBaseModel.setTrainConfig(ConfigPath + FileUtil.FILE_SEPARATOR + TRAIN_CONFIG_JSON);
						baseMapper.insertBizBaseModel(bizBaseModel);
					}
					return R.ok(null, BASE_MODEL_SUCCES);
				} catch (Exception e) {
					// 处理解析返回数据调用异常
					log.error(BASE_MODEL_FAILED, e);
					return R.failed(e.getLocalizedMessage());
				}
			} else {
				// 处理非2xx的HTTP状态码
				log.error(BASE_MODEL_FAILED);
				return R.failed(BASE_MODEL_FAILED);
			}
		} catch (RestClientException e) {
			// 处理RestClientException异常
			log.error(BASE_MODEL_FAILED, e);
			return R.failed(e.getLocalizedMessage());
		}

	}

	@Override
	public List<BaseModelVO> listGroup(BizBaseModel bizBaseModel) {
		return baseMapper.selectListGroup(bizBaseModel);
	}

	@Override
	public R updateBaseModelById(BizBaseModel bizBaseModel) {
		BizBaseModel oldBaseModel = baseMapper.selectById(bizBaseModel.getBaseModelId());
		String oldName = oldBaseModel.getBaseModelName();
		String newName = bizBaseModel.getBaseModelName();
		// 当基础模型的名字修改时对应的配置文件路径也需要更改
		if (!StringUtils.isEmpty(oldName) && !oldName.equals(newName)) {
			Map<String, String> pathParams = new HashMap<>() {{
				put("type", MODEL_FOLDER);
				put("baseModelName", oldName);
			}};
			Map<String, String> newPathParams = new HashMap<>() {{
				put("type", MODEL_FOLDER);
				put("baseModelName", newName);
			}};
			String bucketName = FileUtils.getPath(pathParams) + CONFIG;
			String path = properties.getLocal().getBasePath() + FileUtil.FILE_SEPARATOR + bucketName;
			String newBucketName = FileUtils.getPath(newPathParams) + CONFIG;
			File file = FileUtil.file(path);
			if (file.exists()) {
				CompletableFuture<Void> copyFuture = CompletableFuture.runAsync(() -> {
					sysFileService.copyLocalFile(bucketName, newBucketName);
				});
				copyFuture.thenRun(() -> {
					sysFileService.removeBucket(bucketName);
				});
			}
			bizBaseModel.setConfig(newBucketName + FileUtil.FILE_SEPARATOR + CONFIG_JSON);
			bizBaseModel.setTrainConfig(newBucketName + FileUtil.FILE_SEPARATOR + TRAIN_CONFIG_JSON);
		}
		baseMapper.updateById(bizBaseModel);
		return R.ok();
	}

	@Override
	public R deleteModelByIds(Long[] ids) {
		List<BizBaseModel> list = baseMapper.selectBatchIds(CollUtil.toList(ids));
		for (BizBaseModel baseModel : list) {
//			List<BizModel> bizModels = bizModelMapper.selectList(Wrappers.<BizModel>lambdaQuery().eq(BizModel::getBaseModelId, baseModel.getBaseModelId()));
//
//			if (!bizModels.isEmpty()) {
//				for (BizModel model : bizModels) {
//					List<BizExperiment> bizExperiments = bizExperimentMapper.selectList(Wrappers.<BizExperiment>lambdaQuery().
//							eq(BizExperiment::getModelId, model.getModelId()));
//					if (!bizExperiments.isEmpty()) {
//						List<String> expNames = bizExperiments.stream().map(BizExperiment::getExperimentName).toList();
//						return R.failed(baseModel.getBaseModelName() + "中" + model.getModelName() + "已被实验" + String.join(",", expNames) + "引用，无法删除！");
//					}
//				}
//			}

			//将目录加入待删除目录表
			SysDeletedDir sysDeletedDir = new SysDeletedDir();
			sysDeletedDir.setDirPath(baseModel.getBaseModelName());
			sysDeletedDir.setFunctionType(FILE_FUNCTION_MODEL);
			sysDeletedDirService.saveDeleteDir(sysDeletedDir);
		}
		return R.ok(baseMapper.delete(Wrappers.<BizBaseModel>lambdaQuery().in(BizBaseModel::getBaseModelId, CollUtil.toList(ids))));
	}

	@Override
	public R deleteBaseModelById(Long id) {
		BizBaseModel baseModel = baseMapper.selectById(id);
		if (baseModel != null) {
			List<BizModel> bizModels = bizModelMapper.selectList(Wrappers.<BizModel>lambdaQuery().eq(BizModel::getBaseModelId, id));

			if (!bizModels.isEmpty()) {
//				for (BizModel model : bizModels) {
//					List<BizExperiment> bizExperiments = bizExperimentMapper.selectList(Wrappers.<BizExperiment>lambdaQuery().
//							eq(BizExperiment::getModelId, model.getModelId()));
//					if (!bizExperiments.isEmpty()) {
//						List<String> expNames = bizExperiments.stream().map(BizExperiment::getExperimentName).toList();
//						return R.failed(baseModel.getBaseModelName() + "中" + model.getModelName() + "已被实验" + String.join(",", expNames) + "引用，无法删除！");
//					}
//				}
				bizModelMapper.deleteBatchIds(bizModels.stream().map(BizModel::getModelId).collect(Collectors.toList()));
			}

			//将目录加入待删除目录表
			SysDeletedDir sysDeletedDir = new SysDeletedDir();
			sysDeletedDir.setDirPath(baseModel.getBaseModelName());
			sysDeletedDir.setFunctionType(FILE_FUNCTION_MODEL);
			sysDeletedDirService.saveDeleteDir(sysDeletedDir);

			return R.ok(baseMapper.deleteById(id));
		}
		return R.failed("暂无基础模型id为" + id + "的数据");
	}

	private void removeBucket(BizBaseModel baseModel) {
		String modelName = baseModel.getBaseModelName();
		Map<String, String> pathParams = new HashMap<>() {{
			put("type", MODEL_FOLDER);
			put("baseModelName", modelName);
		}};
		String bucketName = FileUtils.getPath(pathParams) + CONFIG;
		sysFileService.removeBucket(bucketName);
	}

	@Override
	public boolean updateBaseModelConfigFile(BizBaseModel bizBaseModel) {
		//TODO 等待对接算法服务器拉取最新配置文件
		return baseMapper.update(Wrappers.<BizBaseModel>lambdaUpdate().set(BizBaseModel::getCreateTime, LocalDateTime.now())) > 0;
	}

	@Override
	public R getBaseModelConfig(Long id) {
		JSONObject res = new JSONObject();
		String configPath = "", trainConfigPath = "", path = "", configJson = "", trainJson = "";
		Map<String, String> params = new HashMap<>() {{
			put("type", MODEL_FOLDER);
		}};

		BizBaseModel bizBaseModel = baseMapper.selectById(id);

		params.put("baseModelName", bizBaseModel.getBaseModelName());
		res.put("baseModelName", bizBaseModel.getBaseModelName());
		path = FileUtils.getPath(params) + CONFIG + FileUtil.FILE_SEPARATOR;

		configPath = path + CONFIG_JSON;
		trainConfigPath = path + TRAIN_CONFIG_JSON;

		try {
			configJson = sysFileService.readJSONFile(configPath);
		} catch (Exception e) {
			return R.failed("基础模型配置文件读取失败");
		}

		try {
			trainJson = sysFileService.readJSONFile(trainConfigPath);
		} catch (Exception e) {
			return R.failed("基础模型训练配置文件读取失败");
		}

		if (configJson != null && !configJson.isEmpty()) {
			res.put("params", JSONObject.parse(configJson));
		}

		if (trainJson != null && !trainJson.isEmpty()) {
			res.put("trainParams", JSONObject.parse(trainJson));
		}

		return R.ok(res.toJSONString());
	}

	@Override
	public void updateBaseModelConfig(BaseModelDTO baseModelDTO) {
		BizBaseModel bizBaseModel = new BizBaseModel();
		BeanUtils.copyProperties(baseModelDTO, bizBaseModel);
		baseMapper.updateById(bizBaseModel);

		Map<String, String> pathParams = new HashMap<>() {{
			put("type", MODEL_FOLDER);
			put("baseModelName", baseModelDTO.getBaseModelName());
		}};

		String path = FileUtils.getPath(pathParams) + CONFIG + FileUtil.FILE_SEPARATOR;
		String configFileName = CONFIG_JSON, trainFileName = TRAIN_CONFIG_JSON;

		if (baseModelDTO.getParams() != null) {
			String params = JSONObject.toJSONString(baseModelDTO.getParams());
			sysFileService.removeFile(path, configFileName);
			sysFileService.createLocalFile(path, configFileName, params);
		}

		if (baseModelDTO.getTrainParams() != null) {
			String trainParams = JSONObject.toJSONString(baseModelDTO.getTrainParams());
			sysFileService.removeFile(path, trainFileName);
			sysFileService.createLocalFile(path, trainFileName, trainParams);
		}
	}

	@Override
	public void saveModel(BizBaseModel bizBaseModel) {
		Map<String, String> params = new HashMap<>() {{
			put("type", MODEL_FOLDER);
			put("baseModelName", bizBaseModel.getBaseModelName());
		}};
		String configPath = FileUtils.getPath(params) + CONFIG;
		//todo 待判断改模型的配置参数是否存在
		bizBaseModel.setConfig(configPath + FileUtil.FILE_SEPARATOR + CONFIG_JSON);
		bizBaseModel.setTrainConfig(configPath + FileUtil.FILE_SEPARATOR + TRAIN_CONFIG_JSON);
		sysFileService.createBucket(configPath);
		baseMapper.insertBizBaseModel(bizBaseModel);
	}
}
