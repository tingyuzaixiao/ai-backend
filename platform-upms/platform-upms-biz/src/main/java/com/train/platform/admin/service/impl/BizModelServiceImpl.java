package com.train.platform.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.train.platform.admin.api.dto.ModelDTO;
import com.train.platform.admin.api.entity.BizBaseModel;
import com.train.platform.admin.api.entity.BizExperiment;
import com.train.platform.admin.api.entity.BizModel;
import com.train.platform.admin.api.entity.SysDeletedDir;
import com.train.platform.admin.api.vo.ModelVO;
import com.train.platform.admin.mapper.BizExperimentMapper;
import com.train.platform.admin.mapper.BizModelMapper;
import com.train.platform.admin.service.BizBaseModelService;
import com.train.platform.admin.service.BizModelService;
import com.train.platform.admin.service.SysDeletedDirService;
import com.train.platform.admin.service.SysFileService;
import com.train.platform.common.core.util.FileUtils;
import com.train.platform.common.core.util.R;
import com.train.platform.common.file.core.FileProperties;
import com.train.platform.common.file.core.FileTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.train.platform.common.core.constant.CommonConstants.*;

/**
 * <p>
 * 模型管理 服务实现类
 * </p>
 *
 * @author zhouzhipeng
 * @since 2024-01-20
 */
@Service
public class BizModelServiceImpl extends ServiceImpl<BizModelMapper, BizModel> implements BizModelService {

	private final Environment env;
	@Autowired
	private BizBaseModelService bizBaseModelService;

	@Autowired
	private BizExperimentMapper bizExperimentMapper;

	@Autowired
	private SysDeletedDirService sysDeletedDirService;

	@Autowired
	private SysFileService sysFileService;

	@Autowired
	private FileTemplate fileTemplate;

	@Autowired
	private FileProperties properties;

	public BizModelServiceImpl(Environment env) {
		this.env = env;
	}

	@Override
	public ResponseEntity<Resource> downloadFile(@PathVariable String filename, HttpServletRequest request) {
		try {
			// 创建文件路径
			String modelPath = env.getProperty("spring.model.path");
			Resource file = new FileSystemResource(modelPath + filename);
			if (!file.exists() || !file.isFile()) {
				throw new RuntimeException("文件不存在！");
			}

			// 设置响应头
			String contentType = request.getServletContext().getMimeType(file.getFile().getAbsolutePath());
			if (contentType == null) {
				contentType = "application/octet-stream";
			}

			return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"").body(file);

		} catch (Exception e) {
			// 处理异常
			e.printStackTrace();
			return ResponseEntity.status(500).body(null);
		}
	}

	@Override
	public R deleteModelByIds(Long[] ids) {
		List<ModelVO> list = baseMapper.getModelVOList(CollUtil.toList(ids));
		List<SysDeletedDir> deletedDirs = new ArrayList<>();

		for (ModelVO model : list) {
			//判断实验表中是否引用该模型
//			List<BizExperiment> bizExperiments = bizExperimentMapper.selectList(Wrappers.<BizExperiment>lambdaQuery().
//					eq(BizExperiment::getModelId, model.getModelId()));
//			if (!bizExperiments.isEmpty()) {
//				List<String> expNames = bizExperiments.stream().map(BizExperiment::getExperimentName).toList();
//				return R.failed(model.getModelName() + "已被实验" + String.join(",", expNames) + "引用，无法删除！");
//			}

			//将目录加入待删除目录表
			SysDeletedDir sysDeletedDir = new SysDeletedDir();
			Map<String, String> params = new HashMap<>();
			params.put("type", DATASET_FOLDER);
			params.put("modelName", model.getModelName());
			params.put("baseModelName", model.getBaseModelName());
			String path = FileUtils.getPath(params);
			sysDeletedDir.setDirPath(path);
			sysDeletedDir.setFunctionType(FILE_FUNCTION_DATASET);
			deletedDirs.add(sysDeletedDir);
		}

		sysDeletedDirService.saveBatch(deletedDirs);
		return R.ok(baseMapper.delete(Wrappers.<BizModel>lambdaQuery().in(BizModel::getModelId, CollUtil.toList(ids))));
	}

	@Override
	public R updateModelById(BizModel bizModel) {
		BizModel oldModel = baseMapper.selectById(bizModel.getModelId());
		String oldName = oldModel.getModelName();
		String newName = bizModel.getModelName();
		// 当模型的名字修改时对应的配置文件路径也需要更改
		if (!StringUtils.isEmpty(oldName) && !oldName.equals(newName)) {
			Map<String, String> pathParams = new HashMap<>() {{
				put("type", MODEL_FOLDER);
				put("modelName", oldName);
			}};
			Map<String, String> newPathParams = new HashMap<>() {{
				put("type", MODEL_FOLDER);
				put("modelName", newName);
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
			bizModel.setConfig(newBucketName + FileUtil.FILE_SEPARATOR + CONFIG_JSON);
			bizModel.setTrainConfig(newBucketName + FileUtil.FILE_SEPARATOR + TRAIN_CONFIG_JSON);
		}
		baseMapper.updateById(bizModel);
		return R.ok();
	}

	@Override
	public IPage<ModelVO> getModelPage(Page page, BizModel bizModel) {
		return baseMapper.getModelPage(page, bizModel);
	}

	@SneakyThrows
	@Override
	public R saveModel(ModelDTO modelDTO) {
		BizBaseModel bizBaseModel = bizBaseModelService.getById(modelDTO.getBaseModelId());
		BizModel bizModel = new BizModel();
		BeanUtils.copyProperties(modelDTO, bizModel);
		//TODO 需要本地配置和算法服务器配置核对
//		sysFileService.createFile(configPath, TRAIN_CONFIG_JSON, modelDTO.getTrainConfig());
//		if (bizBaseModel == null || StringUtils.isEmpty(bizBaseModel.getConfig())) {
//			return R.failed("基础模型不存在或基础模型配置文件为空");
//		}
		Map<String, String> params = new HashMap<>() {{
			put("type", MODEL_FOLDER);
			put("baseModelName", bizBaseModel.getBaseModelName());
			put("modelName", modelDTO.getModelName());
		}};
		String configPath = FileUtils.getPath(params) + CONFIG;
		bizModel.setConfig(configPath + FileUtil.FILE_SEPARATOR + CONFIG_JSON);
		bizModel.setTrainConfig(configPath + FileUtil.FILE_SEPARATOR + TRAIN_CONFIG_JSON);
		if (modelDTO.getParams() != null && !StringUtils.isEmpty(modelDTO.getParams())) {
			sysFileService.createLocalFile(configPath, CONFIG_JSON, JSONObject.toJSONString(modelDTO.getParams()));
		}
		return R.ok(baseMapper.insert(bizModel));
	}

	@Override
	public String getModelConfigByModelId(Long id, Boolean isBaseModel, String configType, String modelClass) {
		//1、根据模型ID查询modelName、baseModelName
		JSONObject res = new JSONObject();
		ModelVO modelVO = new ModelVO();
		String path = "", json = "";
		Map<String, String> params = new HashMap<>() {{
			put("type", MODEL_FOLDER);
		}};

		if (isBaseModel != null && isBaseModel) {
			BeanUtils.copyProperties(bizBaseModelService.getById(id), modelVO);
		} else {
			if (modelClass != null && modelClass.equals("base")) {
				BeanUtils.copyProperties(bizBaseModelService.getById(id), modelVO);
			} else {
				modelVO = baseMapper.getModelInfoById(id);
				params.put("modelName", modelVO.getModelName());
			}
			res.put("modelName", modelVO.getModelName());
		}
		params.put("baseModelName", modelVO.getBaseModelName());
		res.put("baseModelName", modelVO.getBaseModelName());
		path = FileUtils.getPath(params) + CONFIG + FileUtil.FILE_SEPARATOR;

		if (configType.equals(CONFIG)) {
			path = path + CONFIG_JSON;
		} else {
			path = path + TRAIN_CONFIG_JSON;
		}

		json = sysFileService.readJSONFile(path);
		if (json != null && !json.isEmpty()) {
			res.put("params", JSONObject.parse(json));
		} else {
			res.put("params", null);
		}

		return res.toJSONString();
	}

	@SneakyThrows
	@Override
	public void updateModelConfigByModelId(ModelDTO modelDTO) {
		//modelName baseModelName type
		Map<String, String> pathParams = new HashMap<>() {{
			put("type", MODEL_FOLDER);
			put("baseModelName", modelDTO.getBaseModelName());
		}};

		if (modelDTO.getIsBaseModel() != null && !modelDTO.getIsBaseModel()) {
			pathParams.put("modelName", modelDTO.getModelName());
		}

		String path = FileUtils.getPath(pathParams) + CONFIG + FileUtil.FILE_SEPARATOR;
		String params = JSONObject.toJSONString(modelDTO.getParams());

		String fileName = "";
		if (modelDTO.getConfigType().equals(CONFIG)) {
			fileName = CONFIG_JSON;
		} else {
			fileName = TRAIN_CONFIG_JSON;
		}

		fileTemplate.removeObject(path, fileName);
		sysFileService.createLocalFile(path, fileName, params);

		//更新模型更新时间
		baseMapper.update(Wrappers.<BizModel>lambdaUpdate().
				eq(BizModel::getModelId, modelDTO.getModelId()).
				set(BizModel::getUpdateTime, LocalDateTime.now()));
	}

}
