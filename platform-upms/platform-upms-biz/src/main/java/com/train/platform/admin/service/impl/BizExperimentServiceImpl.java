package com.train.platform.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.train.platform.admin.api.entity.*;
import com.train.platform.admin.api.vo.*;
import com.train.platform.admin.mapper.*;
import com.train.platform.admin.mapper.service.AlgoServerResourceMapperService;
import com.train.platform.admin.mapper.service.SysProgramMapperService;
import com.train.platform.admin.service.BizExperimentService;
import com.train.platform.admin.service.InnerAlgoService;
import com.train.platform.admin.service.SysFileService;
import com.train.platform.common.core.constant.CommonConstants;
import com.train.platform.common.core.constant.enums.*;
import com.train.platform.common.core.mlflow.MlflowHelper;
import com.train.platform.common.core.mlflow.MlflowWrapper;
import com.train.platform.common.core.mlflow.ModelLoggedFiles;
import com.train.platform.common.core.util.*;
import com.train.platform.common.security.util.SecurityUtils;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.mlflow.tracking.MlflowHttpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;


/**
 * @author lee
 * @description 针对表【aircas_train(训练记录表)】的数据库操作Service实现
 * @createDate 2024-05-24 14:36:51
 */
@Slf4j
@Service
public class BizExperimentServiceImpl extends ServiceImpl<BizExperimentMapper, BizExperiment> implements BizExperimentService {
	@Autowired
	private SysFileService sysFileService;

	@Autowired
	private MlflowWrapper mlflowWrapper;

	@Autowired
	private BizGpuResourceServiceImpl bizGpuResourceService;

	@Autowired
	private ProgramLock programLock;

	@Autowired
	private SysProgramMapperService sysProgramMapperService;

	@Autowired
	private InnerAlgoService innerAlgoService;

	@Autowired
	private AlgoServerResourceMapperService resourceMapperService;

	private static final int PROGRAM_LOCK_NUM = 8;
	private static final int EXPERIMENT_LOCK_NUM = 16;
	private final ReentrantLock[][] experimentLocks = new ReentrantLock[PROGRAM_LOCK_NUM][EXPERIMENT_LOCK_NUM];
	{
		for (int i = 0; i < experimentLocks.length; i++) {
			for (int j = 0; j < experimentLocks[i].length; ++j) {
				experimentLocks[i][j] = new ReentrantLock();
			}
		}
	}
	private final ReentrantLock fileLock = new ReentrantLock();

	private final ScheduledTaskManager taskManager;
	{
		taskManager = new ScheduledTaskManager("experimentBackend", 4);
	}

	private static final int WAIT_TIMEOUT_SECONDS = 2;
	private static final int BACKEND_TASK_PERIOD_S = 15;

	private static final int FILE_CLEAR_PERIOD_DAY = 7;
	private static final int FILE_EXPIRED_DAYS = 90;

	private static final int  SLEEP_MILLS = 10;

	@Data
	private static class ExperimentFile {
		private Path savePath;
		private String dbFileName;
		private String folder;
	}

	@PostConstruct
	public void init() {
		taskManager.scheduleWithFixedDelay(
				this::startBackendTask,
				0,
				BACKEND_TASK_PERIOD_S,
				TimeUnit.SECONDS);

		taskManager.scheduleWithFixedDelay(
				this::clearExpiredFile,
				FILE_CLEAR_PERIOD_DAY,
				FILE_CLEAR_PERIOD_DAY,
				TimeUnit.DAYS);
	}

	@Override
	public Boolean checkExperimentNameAvailable(String experimentName, String programId) {
		return !baseMapper.hasRepeatName(experimentName, programId);
	}

	@Override
	public SysProgram getProgramByProgramId(String programId) {
		return sysProgramMapperService.getBaseMapper().
				selectOne(Wrappers.<SysProgram>lambdaQuery().
				eq(SysProgram::getProgramId, programId).
				eq(SysProgram::getDelFlag, "0"));
	}

	@Override
	public BizExperiment queryExperimentForWeb(Long id, String experimentId, String experimentName) {
		BizExperiment experiment = queryExperiment(id, experimentId, experimentName);
		if (experiment != null) {
			modifyExperimentForWeb(experiment);
		}
		return experiment;
	}

	private static void modifyExperimentForWeb(BizExperiment experiment) {
		experiment.setModelFile(PathUtils.resolveFileName(experiment.getModelFile()));
		experiment.setWeightsFile(PathUtils.resolveFileName(experiment.getWeightsFile()));
	}

	@Override
	public BizExperiment createExperiment(BizExperiment experiment, MultipartFile modelFile, MultipartFile weightsFile) {
		log.info("createExperiment experiment: {}", JacksonUtils.serialize(experiment));

//		if (modelFile == null || modelFile.isEmpty()) {
//			throw new RuntimeException("modelFile is required");
//		}
		SysProgram sysProgram = getProgramByProgramId(experiment.getProgramId());
		if (sysProgram == null) {
			throw new RuntimeException("program not exist");
		}

		ReentrantLock lock = programLock.getLock(sysProgram.getName());
		boolean locked = false;
		try {
			locked = lock.tryLock(WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			if (!locked) {
				throw new RuntimeException("wait program lock timeout");
			}
			log.info("createExperiment lock");
			sysProgram = getProgramByProgramId(experiment.getProgramId());
			if (sysProgram == null) {
				throw new RuntimeException("program not exist");
			}

			checkExperimentNameAvailable(experiment.getProgramId(), experiment.getExperimentName());

			saveExperiment(experiment, modelFile, weightsFile);
		} catch (Throwable throwable) {
			log.error("createExperiment catch exception: {}", throwable.getMessage());
			throw new RuntimeException(throwable.getMessage());
		} finally {
			if (locked) {
				lock.unlock();
				log.info("createExperiment unlock");
			}
		}

		return queryExperimentForWeb(experiment.getId(), null, null);
	}

	private void saveExperiment(BizExperiment experiment, MultipartFile modelFile,
								MultipartFile weightsFile) {
		boolean existFile = false;
		ExperimentFile weightsExperimentFile = extractExperimentFile(weightsFile, CommonConstants.WEIGHTS_BUCKET);
		if (weightsExperimentFile != null) {
			if (StringUtils.hasText(experiment.getRefWeights())) {
				throw new RuntimeException("weightsFile and refWeights can only exist one");
			}
			experiment.setWeightsFile(weightsExperimentFile.getDbFileName());
			existFile = true;
		}
		ExperimentFile modelExperimentFile = extractExperimentFile(modelFile, CommonConstants.MODEL_BUCKET);
		if (modelExperimentFile != null) {
			experiment.setModelFile(modelExperimentFile.getDbFileName());
			existFile = true;
		}
		saveCommonFields(experiment);

		if (existFile) {
			boolean locked = false;
			try {
				locked = fileLock.tryLock(WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
				if (!locked) {
					throw new RuntimeException("wait file lock timeout");
				}
				log.info("saveExperiment file lock");

				saveExperiment(experiment, modelFile, weightsFile, modelExperimentFile, weightsExperimentFile);
			} catch (Throwable throwable) {
				log.error("saveExperiment catch exception: {}", throwable.getMessage());
				throw new RuntimeException(throwable);
			} finally {
				if (locked) {
					fileLock.unlock();
					log.info("saveExperiment file unlock");
				}
			}
		} else {
			saveExperiment(experiment, modelFile, weightsFile, modelExperimentFile, weightsExperimentFile);
		}
	}

	private void saveExperiment(BizExperiment experiment,
								MultipartFile modelFile,
								MultipartFile weightsFile,
								ExperimentFile modelExperimentFile,
								ExperimentFile weightsExperimentFile) {
		if (modelExperimentFile != null) {
			saveFile(modelFile, modelExperimentFile.getSavePath(),
					modelExperimentFile.getFolder(), CommonConstants.MODEL_BUCKET);
		}
		if (weightsExperimentFile != null) {
			saveFile(weightsFile, weightsExperimentFile.getSavePath(),
					weightsExperimentFile.getFolder(), CommonConstants.WEIGHTS_BUCKET);
		}

		int insert = baseMapper.insert(experiment);
		if (insert == 0) {
			throw new RuntimeException("create experiment failed: insert db failed");
		}
	}

	private void saveCommonFields(BizExperiment experiment) {
		LocalDateTime now = LocalDateTime.now();
		experiment.setCreateTime(now);
		experiment.setUpdateTime(now);
		experiment.setStatus(ExperimentStatus.INIT.name());
		String userName = SecurityUtils.getUser().getUsername();
		experiment.setCreateBy(userName);
		experiment.setUpdateBy(userName);
		experiment.setMlflowDel(0);
	}

	private void saveFile(MultipartFile multipartFile, Path savePath, String folder,
						  String bucketName) {
		if (savePath == null) {
			return;
		}

		if (!Files.exists(savePath)) {
			sysFileService.createFolder(bucketName, folder);
			try {
				multipartFile.transferTo(savePath);
			} catch (IOException e) {
				throw new RuntimeException(String.format("Failed to save file: %s", savePath), e);
			}
		}
	}

	private ExperimentFile extractExperimentFile(MultipartFile file, String bucketName) {
		String folder = getFileFolder(file);
		if (!StringUtils.hasText(folder)) {
			return null;
		}
		sysFileService.createBucket(CommonConstants.MODEL_BUCKET);

		ExperimentFile experimentFile = new ExperimentFile();
		experimentFile.setFolder(folder);
		Path savePath = getSavePath(bucketName, folder, file.getOriginalFilename());
		experimentFile.setSavePath(savePath);
		experimentFile.setDbFileName(PathUtils.getLastTwoBranches(savePath.toString()));
		return experimentFile;
	}

	private String calMd5(MultipartFile file) {
		try {
			return DigestUtils.md5Hex(file.getInputStream());
		} catch (IOException e) {
			throw new RuntimeException("Failed to calculate MD5 for model file: ", e);
		}
	}

	private String getFileFolder(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return null;
		}

		String md5 = calMd5(file);
		long size = file.getSize();
		return md5 + "_" + size;
	}

	private Path getSavePath(String bucketName, String folder, String fileName) {
		return sysFileService.getFilePath(bucketName, folder, fileName);
	}

	private Path getSavePath(String bucketName, String file) {
		return sysFileService.getFilePath(bucketName, file);
	}

	private String getBucketPath(String bucketName) {
		return sysFileService.getFilePath(bucketName).toString();
	}

	@Override
	public BizExperiment updateExperimentConfig(BizExperiment bizExperiment, MultipartFile modelFile,
												MultipartFile weightsFile) {
		BizExperiment dbExperiment = queryExperiment(bizExperiment.getId(), null, null);
		if (dbExperiment == null) {
			throw new RuntimeException("experiment not exist");
		}

		ReentrantLock lock = LockUtils.getLock(dbExperiment.getProgramId(),
				dbExperiment.getExperimentName(), experimentLocks);
		boolean locked = false;
		try {
			locked = lock.tryLock(WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			if (!locked) {
				throw new RuntimeException("wait experiment lock timeout");
			}
			log.info("updateExperimentConfig lock");

			dbExperiment = queryExperiment(bizExperiment.getId(), null, null);
			if (dbExperiment == null) {
				throw new RuntimeException("experiment not exist");
			}
			checkExperimentStateCanUpdate(dbExperiment.getStatus());

			if (StringUtils.hasText(bizExperiment.getRefWeights())) {
				bizExperiment.setRefWeights(dbExperiment.getRefWeights());
			}
			updateExperiment(bizExperiment, modelFile, weightsFile);
		} catch (Throwable throwable) {
			log.error("updateExperimentConfig catch exception: {}", throwable.getMessage());
			throw new RuntimeException(throwable.getMessage());
		} finally {
			if (locked) {
				lock.unlock();
				log.info("updateExperimentConfig unlock");
			}
		}

		return queryExperimentForWeb(bizExperiment.getId(), null, null);
	}

	private void checkExperimentStateCanUpdate(String status) {
		if (ExperimentStatus.RUNNING.name().equals(status)) {
			throw new RuntimeException("experiment is running");
		} else if (ExperimentStatus.FINISHED.name().equals(status)) {
			throw new RuntimeException("experiment has been finished");
		} else if (ExperimentStatus.KILLING.name().equals(status)) {
			throw new RuntimeException("experiment is killing");
		}
	}

	private void updateExperiment(BizExperiment experiment, MultipartFile modelFile,
								  MultipartFile weightsFile) {
		boolean existFile = false;
		ExperimentFile weightsExperimentFile = extractExperimentFile(weightsFile, CommonConstants.WEIGHTS_BUCKET);
		if (weightsExperimentFile != null) {
			if (StringUtils.hasText(experiment.getRefWeights())) {
				throw new RuntimeException("weightsFile and refWeights can only exist one");
			}
			experiment.setWeightsFile(weightsExperimentFile.getDbFileName());
			existFile = true;
		}
		ExperimentFile modelExperimentFile = extractExperimentFile(modelFile, CommonConstants.MODEL_BUCKET);
		if (modelExperimentFile != null) {
			experiment.setModelFile(modelExperimentFile.getDbFileName());
			existFile = true;
		}

		updateCommonFields(experiment);

		if (existFile) {
			boolean locked = false;
			try {
				locked = fileLock.tryLock(WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
				if (!locked) {
					throw new RuntimeException("wait file lock timeout");
				}
				log.info("updateExperiment file lock");

				updateExperiment(experiment, modelFile, weightsFile, modelExperimentFile, weightsExperimentFile);
			} catch (Throwable throwable) {
				log.error("updateExperiment catch exception: {}", throwable.getMessage());
				throw new RuntimeException(throwable);
			} finally {
				if (locked) {
					fileLock.unlock();
					log.info("updateExperiment file unlock");
				}
			}
		} else {
			updateExperiment(experiment, modelFile, weightsFile, modelExperimentFile, weightsExperimentFile);
		}
	}

	private void updateCommonFields(BizExperiment experiment) {
		experiment.setUpdateTime(LocalDateTime.now());
		experiment.setExperimentName(null);
		experiment.setStatus(null);
		experiment.setUpdateBy(SecurityUtils.getUser().getUsername());
	}

	private void updateExperiment(BizExperiment experiment,
								  MultipartFile modelFile, MultipartFile weightsFile,
								  ExperimentFile modelExperimentFile,
								  ExperimentFile weightsExperimentFile) {
		if (modelExperimentFile != null) {
			saveFile(modelFile, modelExperimentFile.getSavePath(),
					modelExperimentFile.getFolder(), CommonConstants.MODEL_BUCKET);
		}
		if (weightsExperimentFile != null) {
			saveFile(weightsFile, weightsExperimentFile.getSavePath(),
					weightsExperimentFile.getFolder(), CommonConstants.WEIGHTS_BUCKET);
		}

		int update = baseMapper.updateById(experiment);
		if (update == 0) {
			throw new RuntimeException("updateExperiment failed: update db failed");
		}
	}

	@Override
	public boolean removeByProgramId(String programId) {
		log.info("removeByProgramId programId: {}", programId);
		ReentrantLock[] locks = LockUtils.getLock(programId, experimentLocks);
		Boolean[] lockeds = new Boolean[locks.length];
		Arrays.fill(lockeds, false);

		try {
			for (int i = 0; i < locks.length; ++i) {
				lockeds[i] = locks[i].tryLock(WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
				if (!lockeds[i]) {
					throw new RuntimeException("wait experiment lock timeout");
				}
			}

			return removeAll(programId);
		} catch (InterruptedException e) {
			log.error("removeByProgramId: {} catch exception: {}", programId, e.getMessage());
			throw new RuntimeException(e);
		} finally {
			for (int i = 0; i < locks.length; ++i) {
				if (lockeds[i]) {
					locks[i].unlock();
				}
			}
		}
	}

	private boolean removeAll(String programId) {
		boolean exist = baseMapper.exists(Wrappers.<BizExperiment>lambdaQuery().
				eq(BizExperiment::getProgramId, programId).
				eq(BizExperiment::getDelFlag, "0").
				in(BizExperiment::getStatus,
						ExperimentStatus.RUNNING.name(),
						ExperimentStatus.SCHEDULED.name(),
						ExperimentStatus.KILLING.name()));
		if (exist) {
			return false;
		}

		baseMapper.logicDeleteByProgramId(programId, LocalDateTime.now());
		return true;
	}

	@Override
	public BizExperiment queryExperiment(Long id, String experimentId, String experimentName) {
		LambdaQueryWrapper<BizExperiment> lambdaWrapper = Wrappers.<BizExperiment>lambdaQuery().
				eq(BizExperiment::getDelFlag, "0");

		if (id != null) {
			lambdaWrapper = lambdaWrapper.eq(BizExperiment::getId, id);
		} else if (StringUtils.hasText(experimentId)) {
			lambdaWrapper = lambdaWrapper.eq(BizExperiment::getExperimentId, experimentId);
		} else if (StringUtils.hasText(experimentName)) {
			lambdaWrapper = lambdaWrapper.eq(BizExperiment::getExperimentName, experimentName);
		}
		return baseMapper.selectOne(lambdaWrapper);
	}

	@Override
	public IPage<BizExperiment> getExperimentsByPage(Integer current, Integer size, String programId) {
		log.info("getExperimentsByPage current: {}, size: {}, programId: {}", current, size, programId);
		Page<BizExperiment> page = new Page<>(current, size);
		BizExperiment bizExperiment = new BizExperiment();
		bizExperiment.setProgramId(programId);
		bizExperiment.setDelFlag("0");

		Page<BizExperiment> experimentPage = baseMapper.selectPage(page, Wrappers.query(bizExperiment));
		List<BizExperiment> bizExperiments = experimentPage.getRecords();
		for (BizExperiment experiment : bizExperiments) {
			modifyExperimentForWeb(experiment);
		}

		return experimentPage;
	}

	@Override
	public ExperimentDetailsVO queryDetails(Long id) {
		log.info("queryDetails id: {}", id);
		if (id == null || id <= 0L) {
			throw new RuntimeException("id is invalid");
		}

		BizExperiment bizExperiment = queryExperiment(id, null, null);
		if (bizExperiment == null) {
			throw new RuntimeException("experiment not exist");
		}

		ReentrantLock lock = LockUtils.getLock(bizExperiment.getProgramId(), bizExperiment.getExperimentName(), experimentLocks);
		boolean locked = false;
		try {
			locked = lock.tryLock(WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			if (!locked) {
				throw new RuntimeException("wait experiment lock timeout");
			}
			log.info("queryDetails lock");

			BizExperiment dbExperiment = queryExperiment(bizExperiment.getId(), null, null);
			if (dbExperiment == null) {
				throw new RuntimeException("experiment not exist");
			}

			String experimentId = dbExperiment.getExperimentId();
			if (StringUtils.hasText(experimentId)) {
				List<org.mlflow.api.proto.Service.Run> runs = mlflowWrapper.searchRunsByIds(
						dbExperiment.getProgramId(),
						List.of(experimentId));
				if (runs == null || runs.isEmpty()) {
					throw new RuntimeException("experiment not exist in mlflow");
				} else {
					org.mlflow.api.proto.Service.Run run = runs.get(0);

					ExperimentDetailsVO experimentDetailsVO = new ExperimentDetailsVO();

					fillDetailsCommonMem(experimentDetailsVO, dbExperiment);
					fillMetrics(experimentDetailsVO, run.getData().getMetricsList());
					fillParams(experimentDetailsVO, run.getData().getParamsList());
					fillTags(experimentDetailsVO, run.getData().getTagsList());

					experimentDetailsVO.setStatus(processExperimentStatus(dbExperiment, run));

					fillAlgoServerResource(experimentDetailsVO, dbExperiment.getResourceId());
//					setGpuResource(experimentDetailsVO, dbExperiment.getResourceId());
					return experimentDetailsVO;
				}
			} else {
				ExperimentDetailsVO experimentDetailsVO = new ExperimentDetailsVO();
				fillDetailsCommonMem(experimentDetailsVO, dbExperiment);
				fillAlgoServerResource(experimentDetailsVO, dbExperiment.getResourceId());
				return experimentDetailsVO;
			}
		} catch (InterruptedException e) {
			log.error("queryDetails: {} catch exception: {}", bizExperiment.getId(), e.getMessage());
			throw new RuntimeException(e.getMessage());
		} finally {
			if (locked) {
				lock.unlock();
				log.info("queryDetails unlock");
			}
		}
	}

	private static void fillMetrics(ExperimentDetailsVO experimentDetailsVO,
								   List<org.mlflow.api.proto.Service.Metric> metrics) {
		List<String> metricNames = new ArrayList<>();
		List<Double> metricVals = new ArrayList<>();
		MlflowHelper.parseMetrics(metrics, metricNames, metricVals);
		experimentDetailsVO.setMetricNames(metricNames);
		experimentDetailsVO.setMetricVals(metricVals);
	}

	private static void fillParams(ExperimentDetailsVO experimentDetailsVO,
								  List<org.mlflow.api.proto.Service.Param> params) {
		List<String> paramNames = new ArrayList<>();
		List<String> paramVals = new ArrayList<>();
		MlflowHelper.parseParams(params, paramNames, paramVals);
		experimentDetailsVO.setParamNames(paramNames);
		experimentDetailsVO.setParamVals(paramVals);
	}

	private static void fillTags(ExperimentDetailsVO experimentDetailsVO,
								List<org.mlflow.api.proto.Service.RunTag> tags) {
		List<String> tagNames = new ArrayList<>();
		List<String> tagVals = new ArrayList<>();
		MlflowHelper.parseTags(tags, tagNames, tagVals);
		experimentDetailsVO.setTagNames(tagNames);
		experimentDetailsVO.setTagVals(tagVals);
	}

	private static void fillMetrics(ExperimentDetailsVO experimentDetailsVO, String metricStr) {
		List<String> metricNames = parseMetrics(metricStr);
		if (metricNames != null) {
			experimentDetailsVO.setMetricNames(metricNames);

			List<Double> metricVals = new ArrayList<>();
			for (int i = 0; i < metricNames.size(); ++i) {
				metricVals.add(null);
			}
			experimentDetailsVO.setMetricVals(metricVals);
		}
	}

	private static List<String> parseMetrics(String metricStr) {
		if (StringUtils.hasText(metricStr)) {
			return JacksonUtils.deserializeStrList(metricStr);
		}
		return null;
	}

	private void fillDetailsCommonMem(ExperimentDetailsVO experimentDetailsVO, BizExperiment experiment) {
		ObjectCopyUtils.copyProperties(experiment, experimentDetailsVO);

		experimentDetailsVO.setModelFile(PathUtils.resolveFileName(experiment.getModelFile()));
		experimentDetailsVO.setWeightsFile(PathUtils.resolveFileName(experiment.getWeightsFile()));

		if (StringUtils.hasText(experiment.getRefWeights())) {
			if (StringUtils.hasText(experiment.getRefWeights())) {
				BizExperiment refExperiment = queryExperiment(null, experiment.getRefWeights(), null);
				if (refExperiment == null) {
					throw new RuntimeException("ref experiment not exists");
				}
				experimentDetailsVO.setRefExperimentName(refExperiment.getExperimentName());

				SysProgram sysProgram = getProgramByProgramId(refExperiment.getExperimentId());
				if (sysProgram == null) {
					throw new RuntimeException("ref program not exists");
				}
				experimentDetailsVO.setRefProgramName(sysProgram.getName());
			}
		}
	}

//	private void setGpuResource(ExperimentDetailsVO experimentDetailsVO, Long resourceId) {
//		BizGpuResource gpuResource = bizGpuResourceService.getBizGpuResourceById(resourceId);
//		if (gpuResource == null) {
//			return;
//		}
//		experimentDetailsVO.setResourceId(gpuResource.getId());
//		experimentDetailsVO.setGpuId(gpuResource.getGpuId());
//		experimentDetailsVO.setGpuName(gpuResource.getGpuName());
//		experimentDetailsVO.setGpuUuid(gpuResource.getGpuUuid());
//		experimentDetailsVO.setGpuMemory(gpuResource.getGpuMemory());
//		experimentDetailsVO.setIp(gpuResource.getIp());
//	}

	private void fillAlgoServerResource(ExperimentDetailsVO experimentDetailsVO, Long resourceId) {
		AlgoServerResource algoServerResource = resourceMapperService.getBaseMapper().
				selectOne(Wrappers.<AlgoServerResource>lambdaQuery().
						eq(AlgoServerResource::getId, resourceId).
						in(AlgoServerResource::getDelFlag, "0", "1"));
		if (algoServerResource == null) {
			return;
		}
		experimentDetailsVO.setServer(algoServerResource.getServer());
		experimentDetailsVO.setServerDescription(algoServerResource.getDescription());
		experimentDetailsVO.setServerParams(algoServerResource.getParams());
	}

	private String processExperimentStatus(BizExperiment experiment,
										org.mlflow.api.proto.Service.Run run) {
		org.mlflow.api.proto.Service.RunInfo runInfo = run.getInfo();

		// 因为后台不是实时更新，为了保持状态一致性，需要即使更新
		if (org.mlflow.api.proto.Service.RunStatus.FINISHED.equals(runInfo.getStatus()) &&
				ExperimentStatus.RUNNING.name().equals(experiment.getStatus())) {
			experiment.setUpdateTime(LocalDateTime.now());
			fillFinishData(experiment, run);

			updateRunningData(experiment);
			return ExperimentStatus.FINISHED.name();
		}
		return experiment.getStatus();
	}

	@Override
	public HistoryMetricVO queryMetricHistory(String experimentId, String metricName) {
		log.info("queryMetricHistory experimentId: {}, metricName: {}", experimentId, metricName);
		List<org.mlflow.api.proto.Service.Metric> metrics =
				mlflowWrapper.getMetricHistory(experimentId, metricName);

		HistoryMetricVO historyMetricVO = new HistoryMetricVO();
		if (metrics == null || metrics.isEmpty()) {
			return historyMetricVO;
		}

		List<Long> steps = new ArrayList<>(metrics.size());
		List<Double> values = new ArrayList<>(metrics.size());
		List<Long> timestamps = new ArrayList<>(metrics.size());
		for (org.mlflow.api.proto.Service.Metric metric : metrics) {
			steps.add(metric.getStep());
			values.add(metric.getValue());
			timestamps.add(metric.getTimestamp());
		}
		historyMetricVO.setSteps(steps);
		historyMetricVO.setValues(values);
		historyMetricVO.setTimestamps(timestamps);
		return historyMetricVO;
	}

	@Override
	public List<ExperimentFileVO> listFiles(String experimentId, Integer fileType, String path) {
		log.info("listFiles experimentId: {}, fileType: {}, path: {}", experimentId, fileType, path);
		MLFileType mlFileType = MLFileType.valueOf(fileType);
		if (MLFileType.OUTPUT.equals(mlFileType)) {
			String outputModel = getOutputModel(experimentId);
			if (!StringUtils.hasText(outputModel)) {
				throw new RuntimeException("experiment or outputModel not exist");
			}

			String fileRets = mlflowWrapper.listLMFiles(outputModel, path);
			return fileRet2ExperimentFileVO(fileRets);
		} else if (MLFileType.USER.equals(mlFileType)) {
			List<org.mlflow.api.proto.Service.FileInfo> fileInfos =
					mlflowWrapper.listArtifacts(experimentId, path);
			return fileInfo2ExperimentFileVO(fileInfos);
		} else {
			throw new RuntimeException("fileType is invalid");
		}
	}

	@Override
	public void downloadFile(HttpServletResponse response, String experimentId,
							 Integer fileType, String file) {
		log.info("downloadFile experimentId: {}, fileType: {}, file: {}",
				experimentId, fileType, file);
		MLFileType mlFileType = MLFileType.valueOf(fileType);
		if (MLFileType.OUTPUT.equals(mlFileType)) {
			String outputModel = getOutputModel(experimentId);
			if (!StringUtils.hasText(outputModel)) {
				HttpServletResponseUtils.handleDownloadError(response,
						HttpServletResponse.SC_NOT_FOUND,
						"experiment or outputModel not exist");
				return;
			}

			mlflowWrapper.downloadAndUploadLMFiles(response, outputModel, file);
		} else if (MLFileType.USER.equals(mlFileType)) {
			mlflowWrapper.downloadAndUploadArtifacts(response, experimentId, file);
		} else {
			HttpServletResponseUtils.handleDownloadError(response,
					HttpServletResponse.SC_NOT_FOUND,
					"fileType is invalid");
		}
	}

	private String getOutputModel(String experimentId) {
		BizExperiment experiment = queryExperiment(null, experimentId, null);
		if (experiment == null) {
			return null;
		}
		return experiment.getOutputModel();
	}

	@Override
	public void downloadExperimentFile(HttpServletResponse response, Long id, Integer fileType) {
		log.info("downloadExperimentFile id: {}, fileType: {}", id, fileType);
		BizExperiment experiment = queryExperiment(id, null, null);
		if (experiment == null) {
			HttpServletResponseUtils.handleDownloadError(response,
					HttpServletResponse.SC_NOT_FOUND,
					"experiment not exist");
			return;
		}

		ExperimentFileType experimentFileType = ExperimentFileType.valueOf(fileType);
		switch (experimentFileType) {
			case MODEL -> downloadExperimentFile(response,
					CommonConstants.MODEL_BUCKET,
					experiment.getModelFile(),
					experiment);
			case WEIGHTS -> downloadExperimentFile(response,
					CommonConstants.WEIGHTS_BUCKET,
					experiment.getWeightsFile(),
					experiment);
			default -> HttpServletResponseUtils.handleDownloadError(response,
					HttpServletResponse.SC_BAD_REQUEST, "fileType invalid");
		}
	}

	void downloadExperimentFile(HttpServletResponse response, String bucketName,
								String fileName, BizExperiment experiment) {
		if (!StringUtils.hasText(fileName)) {
			HttpServletResponseUtils.handleDownloadError(response,
					HttpServletResponse.SC_NOT_FOUND,
					"file not exist");
			return;
		}

		boolean locked = false;
		try {
			locked = fileLock.tryLock(WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			if (!locked) {
				HttpServletResponseUtils.handleDownloadError(response,
						HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"wait experiment lock timeout");
				return;
			}
			log.info("downloadExperimentFile file lock");
			String localFile = getSavePath(bucketName, fileName).toString();
			HttpServletResponseUtils.downloadFromLocalFile(response, localFile);
		} catch (Throwable throwable) {
			log.error("downloadExperimentFile catch exception: {}", throwable.getMessage());
			return;
		} finally {
			if (locked) {
				fileLock.unlock();
				log.info("downloadExperimentFile file unlock");
			}
		}
	}

	public static String extractLockStr(String fileName) {
		if (Objects.isNull(fileName) || fileName.isEmpty()) {
			return null;
		}

		String normalizedPath = fileName.replace("\\", "/");
		int lastSlashIndex = normalizedPath.lastIndexOf('/');
		if (lastSlashIndex <= 0) {
			return null;
		}

		String parentDirPath = normalizedPath.substring(0, lastSlashIndex);
		int secondLastSlashIndex = parentDirPath.lastIndexOf('/');
		if (secondLastSlashIndex == -1) {
			return parentDirPath;
		}
		return parentDirPath.substring(secondLastSlashIndex + 1);
	}

	private static List<ExperimentFileVO> fileRet2ExperimentFileVO(String fileRets) {
		if (fileRets == null || fileRets.isBlank()) {
			return new ArrayList<>();
		}

		ModelLoggedFiles modelLoggedFiles = null;
		try {
			modelLoggedFiles = JacksonUtils.getObjectMapper().readValue(fileRets, ModelLoggedFiles.class);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		List<ModelLoggedFiles.File> files = modelLoggedFiles.getFiles();
		if (files == null || files.isEmpty()) {
			return new ArrayList<>();
		}
		List<ExperimentFileVO> experimentFileVOs = new ArrayList<>(files.size());
		for (ModelLoggedFiles.File file : files) {
			ExperimentFileVO experimentFileVO = new ExperimentFileVO();
			experimentFileVO.setFileName(file.getPath());
			experimentFileVO.setDir(file.getDir());
			experimentFileVOs.add(experimentFileVO);
		}
		return experimentFileVOs;
	}

	private static List<ExperimentFileVO> fileInfo2ExperimentFileVO(
			List<org.mlflow.api.proto.Service.FileInfo> fileInfos) {
		if (fileInfos == null || fileInfos.isEmpty()) {
			return new ArrayList<>();
		}

		List<ExperimentFileVO> experimentFileVOs = new ArrayList<>(fileInfos.size());
		for (org.mlflow.api.proto.Service.FileInfo fileInfo : fileInfos) {
			ExperimentFileVO experimentFileVO = new ExperimentFileVO();
			experimentFileVO.setFileName(fileInfo.getPath());
			experimentFileVO.setDir(fileInfo.getIsDir());
			experimentFileVOs.add(experimentFileVO);
		}
		return experimentFileVOs;
	}

	@Override
	public int removeExperimentByIds(List<Long> ids) {
		log.info("removeExperimentByIds ids: {}", JacksonUtils.serialize(ids));
		if (ids == null || ids.isEmpty()) {
			return 0;
		}

		List<String> statuses = getCanRemoveStatus();
		List<BizExperiment> experiments = baseMapper.getExperimentsByIds(ids, statuses);
		if (experiments == null || experiments.isEmpty()) {
			return 0;
		}

		if (experiments.size() > 3) {
			return batchDelete(experiments, ids, statuses);
		} else {
			return deleteOneByOne(experiments, ids, statuses);
		}
	}

	private int batchDelete(List<BizExperiment> experiments, List<Long> ids, List<String> statuses) {
		ReentrantLock[] locks = LockUtils.getLock(experiments.get(0).getProgramId(), experimentLocks);
		Boolean[] lockeds = new Boolean[locks.length];
		Arrays.fill(lockeds, false);

		try {
			for (int i = 0; i < locks.length; ++i) {
				lockeds[i] = locks[i].tryLock(WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
				if (!lockeds[i]) {
					throw new RuntimeException("wait experiment lock timeout");
				}
			}
			log.info("batchDelete lock");

			experiments = baseMapper.getExperimentsByIds(ids, statuses);
			if (experiments == null || experiments.isEmpty()) {
				return 0;
			}

			List<Long> deleteIds = experiments.stream().map(BizExperiment::getId).collect(Collectors.toList());
			return baseMapper.logicDeleteByIds(deleteIds, LocalDateTime.now());
		} catch (InterruptedException e) {
			log.error("experiment batchDelete: {} catch exception: {}", ids, e.getMessage());
			throw new RuntimeException(e);
		} finally {
			for (int i = 0; i < locks.length; ++i) {
				if (lockeds[i]) {
					locks[i].unlock();
				}
			}
			log.info("batchDelete unlock");
		}
	}

	private int deleteOneByOne(List<BizExperiment> experiments, List<Long> ids, List<String> statuses) {
		int count = 0;
		for (BizExperiment experiment : experiments) {
			ReentrantLock lock = LockUtils.getLock(experiment.getProgramId(), experiment.getExperimentName(), experimentLocks);
			boolean locked = false;
			try {
				locked = lock.tryLock(WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
				if (!locked) {
					throw new RuntimeException("wait experiment lock timeout");
				}
				log.info("deleteOneByOne lock");

				List<Long> deleteIds = List.of(experiment.getId());
				experiments = baseMapper.getExperimentsByIds(deleteIds, statuses);
				if (experiments == null || experiments.isEmpty()) {
					continue;
				}

				count += baseMapper.logicDeleteByIds(deleteIds, LocalDateTime.now());
			} catch (InterruptedException e) {
				log.error("removeExperimentByIds: {} catch exception: {}", ids, e.getMessage());
				throw new RuntimeException(e);
			} finally {
				if (locked) {
					lock.unlock();
					log.info("deleteOneByOne unlock");
				}
			}
		}
		return count;
	}

	private static List<String> getCanRemoveStatus() {
		ExperimentStatus[] experimentStatuses = ExperimentStatus.getAllStatus();
		List<String> rets = new ArrayList<>();

		for (ExperimentStatus experimentStatus : experimentStatuses) {
			if (!experimentStatus.equals(ExperimentStatus.RUNNING) &&
					!experimentStatus.equals(ExperimentStatus.SCHEDULED) &&
					!experimentStatus.equals(ExperimentStatus.KILLING)) {
				rets.add(experimentStatus.name());
			}
		}
		return rets;
	}

	/**
	 * 不能用更新前的状态限制充当锁的功能，因为存在多个状态到RUNNING
	 *
	 * @param id          主键
	 * @param resourceId  资源id
	 * @return BizExperiment
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public BizExperiment startRunning(Long id, Long resourceId) {
		log.info("startRunning id: {}, resourceId: {}", id, resourceId);
		BizExperiment experiment = queryExperiment(id, null, null);
		if (experiment == null) {
			throw new RuntimeException("experiment not exist");
		}
		checkExperimentStateCanRun(experiment.getStatus());
		AlgoServerResource algoServerResource = getAndCheckResourceAvailable(resourceId);

		ReentrantLock lock = LockUtils.getLock(experiment.getProgramId(),
				experiment.getExperimentName(), experimentLocks);
		boolean locked = false;
		BizExperiment newExperiment = null;
		try {
			locked = lock.tryLock(WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			if (!locked) {
				throw new RuntimeException("wait experiment lock timeout");
			}
			log.info("startRunning lock");

			experiment = queryExperiment(id, null, null);
			if (experiment == null) {
				throw new RuntimeException("experiment not exist");
			}
			checkExperimentStateCanRun(experiment.getStatus());

			int updated = updateExperimentToRunning(experiment, resourceId);
			if (updated == 0) {
				throw new RuntimeException("experiment status changed");
			}
			newExperiment = queryExperiment(id, null, null);
		} catch (InterruptedException e) {
			log.error("startRunning catch exception: {}", e.getMessage());
			throw new RuntimeException(e);
		} finally {
			if (locked) {
				lock.unlock();
				log.info("startRunning unlock");
			}
		}

		SysProgram program = getProgramByProgramId(newExperiment.getProgramId());
		if (program == null || !StringUtils.hasText(program.getName())) {
			throw new RuntimeException("program or program name not exist");
		}
		innerAlgoService.startRunning(newExperiment, program.getName(), algoServerResource);
		return queryExperimentForWeb(id, null, null);
	}

	private void checkExperimentStateCanRun(String status) {
		if (ExperimentStatus.RUNNING.name().equals(status)) {
			throw new RuntimeException("experiment has been running");
		} else if (ExperimentStatus.FINISHED.name().equals(status)) {
			throw new RuntimeException("experiment has been finished, can't run again");
		} else if (ExperimentStatus.KILLING.name().equals(status)) {
			throw new RuntimeException("experiment has been finished, can't run again");
		}
	}

	private AlgoServerResource getAndCheckResourceAvailable(Long resourceId) {
		AlgoServerResource algoServerResource = resourceMapperService.getBaseMapper().
				selectOne(Wrappers.<AlgoServerResource>lambdaQuery().
						eq(AlgoServerResource::getId, resourceId).
						eq(AlgoServerResource::getStatus, GpuStatus.FREE).
						eq(AlgoServerResource::getDelFlag, "0"));
		if (algoServerResource == null) {
			throw new RuntimeException("algo server not available, please choose another");
		}
		return algoServerResource;
	}

	private int updateExperimentToRunning(BizExperiment experiment, Long resourceId) {
		return baseMapper.update(Wrappers.<BizExperiment>lambdaUpdate().
				eq(BizExperiment::getId, experiment.getId()).
				eq(BizExperiment::getDelFlag, "0").
				set(BizExperiment::getStatus, ExperimentStatus.RUNNING.name()).
				set(BizExperiment::getExperimentId, null).
//				set(BizExperiment::getStartTime, null).  // 不能将startTime设置为null
				set(BizExperiment::getDuration, null).
				set(BizExperiment::getOutputModel, null).
				set(BizExperiment::getProgress, null).
				set(BizExperiment::getResourceId, resourceId).
				set(BizExperiment::getStopType, null));
	}

	/**
	 * 用更新前的状态限制，充当锁的功能。因为只能从RUNNING -> KILLING
	 *
	 * @param id                  主键
	 * @param experimentStopType  停止类型
	 * @return BizExperiment
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public BizExperiment stopRunning(Long id, Integer experimentStopType) {
		log.info("startRunning id: {}, experimentStopType: {}", id, experimentStopType);
		ExperimentStopType stopType = ExperimentStopType.valueOf(experimentStopType);
		if (ExperimentStopType.SAVE_WEIGHTS.equals(stopType) ||
				ExperimentStopType.FORCE.equals(stopType)) {
			BizExperiment experiment = queryExperiment(id, null, null);
			if (experiment == null) {
				throw new RuntimeException("experiment not exist");
			}
			checkExperimentStateCanStop(experiment.getStatus());

			int updated = baseMapper.updateToKilling(ExperimentStatus.KILLING.name(),
					experimentStopType, id, ExperimentStatus.RUNNING.name(), LocalDateTime.now());
			if (updated == 0) {
				throw new RuntimeException("experiment not running, can't stop");
			}

			BizExperiment newExperiment = queryExperiment(id, null, null);
			innerAlgoService.stopRunning(newExperiment.getResourceId(), experimentStopType);

			return queryExperimentForWeb(id, null, null);
		} else {
			throw new RuntimeException("fileType is invalid");
		}
	}

	private void checkExperimentStateCanStop(String status) {
		if (!ExperimentStatus.RUNNING.name().equals(status)) {
			throw new RuntimeException("experiment can't stopped, status: " + status);
		}
	}

	public void startBackendTask() {
		try {
			backendTaskForRunning();
			backendTaskForKilling();
			backendTaskForDelete();
		} catch (Throwable throwable) {
			log.error("startBackendTask catch exception: {}", throwable.getMessage());
		}
	}

	private void backendTaskForRunning() {
		List<BizExperiment> experiments = getRunningExperiments();
		if (experiments == null || experiments.isEmpty()) {
			return;
		}

		for (BizExperiment bizExperiment : experiments) {
			log.info("backendTaskForRunning experiment: {}", JacksonUtils.serialize(bizExperiment));

			ReentrantLock lock = LockUtils.getLock(bizExperiment.getProgramId(), bizExperiment.getExperimentName(), experimentLocks);
			boolean locked = false;
			try {
				locked = lock.tryLock(WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
				if (!locked) {
					throw new RuntimeException("wait experiment lock timeout");
				}
				log.info("backendTaskForRunning lock");

				BizExperiment dbExperiment = queryExperiment(bizExperiment.getId(), null, null);
				if (dbExperiment == null) {
					continue;
				}

				String experimentId = dbExperiment.getExperimentId();
				if (StringUtils.hasText(experimentId)) {
					List<org.mlflow.api.proto.Service.Run> runs = mlflowWrapper.searchRunsByIds(
							dbExperiment.getProgramId(), List.of(experimentId));
					if (runs == null || runs.isEmpty()) {
						continue;
					}
					org.mlflow.api.proto.Service.Run run = runs.get(0);
					updateRunningData(dbExperiment, run);
				} else {
					List<org.mlflow.api.proto.Service.Run> runs = mlflowWrapper.searchRunsByName(
							dbExperiment.getProgramId(), dbExperiment.getExperimentName());
					if (runs == null || runs.isEmpty()) {
						continue;
					}
					org.mlflow.api.proto.Service.Run run = runs.get(0);
					if (availableRun(dbExperiment, run.getInfo())) {
						updateRunningData(dbExperiment, run);
					}
				}

			} catch (InterruptedException e) {
				log.error("backendTaskForRunning: {} catch exception: {}", bizExperiment.getId(), e.getMessage());
				continue;
			} finally {
				if (locked) {
					lock.unlock();
					log.info("backendTaskForRunning unlock");
				}
			}
		}
	}

	private List<BizExperiment> getRunningExperiments() {
		BizExperiment bizExperiment = new BizExperiment();
		bizExperiment.setStatus(ExperimentStatus.RUNNING.name());
		bizExperiment.setDelFlag("0");
		return baseMapper.getExperiments(bizExperiment);
	}

	private static boolean availableRun(BizExperiment experiment, org.mlflow.api.proto.Service.RunInfo runInfo) {
		if (experiment.getStartTime() == null) {
			return false;
		}
		LocalDateTime startTime = TimeUtils.convertLongToDateTime(runInfo.getStartTime());
		return startTime.isAfter(experiment.getStartTime());
	}

	private void updateRunningData(BizExperiment experiment, org.mlflow.api.proto.Service.Run run) {
		org.mlflow.api.proto.Service.RunInfo runInfo = run.getInfo();
		experiment.setExperimentId(runInfo.getRunId());
		experiment.setStartTime(TimeUtils.convertLongToDateTime(runInfo.getStartTime()));
		experiment.setUpdateTime(LocalDateTime.now());

		if (runInfo.getStatus().equals(org.mlflow.api.proto.Service.RunStatus.SCHEDULED) ||
				runInfo.getStatus().equals(org.mlflow.api.proto.Service.RunStatus.RUNNING)) {
			experiment.setStatus(ExperimentStatus.RUNNING.name());
			experiment.setProgress(MlflowHelper.parseProgress(run.getData().getParamsList()));
			updateRunningData(experiment);
		} else if (runInfo.getStatus().equals(org.mlflow.api.proto.Service.RunStatus.FINISHED)) {
			fillFinishData(experiment, run);
			updateRunningData(experiment);
		} else if (runInfo.getStatus().equals(org.mlflow.api.proto.Service.RunStatus.FAILED) ||
				runInfo.getStatus().equals(org.mlflow.api.proto.Service.RunStatus.KILLED)) {
			experiment.setStatus(runInfo.getStatus().name());
			updateRunningData(experiment);
		}
	}

	private static void fillFinishData(BizExperiment experiment, org.mlflow.api.proto.Service.Run run) {
		experiment.setStatus(ExperimentStatus.FINISHED.name());
		experiment.setDuration(MlflowHelper.parseDuration(run.getInfo()));
		experiment.setOutputModel(MlflowHelper.parseModelId(run.getOutputs()));
		experiment.setProgress(100.0);
	}

	/**
	 * 这里不要求项目是否被删除，允许其更新，原因：running状态不允许被删除，即使被删除了，也没有影响
	 */
	private void updateRunningData(BizExperiment experiment) {
		baseMapper.update(experiment,
				Wrappers.<BizExperiment>lambdaUpdate().
				eq(BizExperiment::getId, experiment.getId()).
				eq(BizExperiment::getDelFlag, "0").
				eq(BizExperiment::getStatus, ExperimentStatus.RUNNING.name()));
	}

	private List<BizExperiment> getKillingExperiments() {
		BizExperiment bizExperiment = new BizExperiment();
		bizExperiment.setStatus(ExperimentStatus.KILLING.name());
		bizExperiment.setDelFlag("0");
		return baseMapper.getExperiments(bizExperiment);
	}

	private void backendTaskForKilling() {
		List<BizExperiment> experiments = getKillingExperiments();
		if (experiments == null || experiments.isEmpty()) {
			return;
		}

		for (BizExperiment experiment : experiments) {
			List<org.mlflow.api.proto.Service.Run> runs = null;

			String experimentId = experiment.getExperimentId();
			if (StringUtils.hasText(experimentId)) {
				runs = mlflowWrapper.searchRunsByIds(experiment.getProgramId(),
						List.of(experimentId));
				if (runs == null || runs.isEmpty()) {
					baseMapper.updateToKilled(ExperimentStatus.KILLED.name(),
							experiment.getId(), ExperimentStatus.KILLING.name(), LocalDateTime.now());
					continue;
				}
			} else {
				runs = mlflowWrapper.searchRunsByName(experiment.getProgramId(),
						experiment.getExperimentName());
				if (runs == null || runs.isEmpty()) {
					continue;
				}
			}

			org.mlflow.api.proto.Service.Run run = runs.get(0);
			org.mlflow.api.proto.Service.RunStatus runStatus = run.getInfo().getStatus();
			if (org.mlflow.api.proto.Service.RunStatus.KILLED.equals(runStatus) ||
					org.mlflow.api.proto.Service.RunStatus.FINISHED.equals(runStatus) ||
					org.mlflow.api.proto.Service.RunStatus.FAILED.equals(runStatus)) {
				baseMapper.updateToKilled(ExperimentStatus.KILLED.name(),
						experiment.getId(), ExperimentStatus.KILLING.name(), LocalDateTime.now());
			}
		}
	}

	private void backendTaskForDelete() {
		List<BizExperiment> experiments = baseMapper.getExperimentsToDelete();
		if (experiments == null || experiments.isEmpty()) {
			return;
		}

		int index = 0;
		while (index < experiments.size()) {
			BizExperiment bizExperiment = experiments.get(index);
			log.info("backendTaskForDelete experiment: {}", JacksonUtils.serialize(bizExperiment));
			String experimentId = bizExperiment.getExperimentId();
			if (!StringUtils.hasText(experimentId)) {
				++index;
				updateMlflowDelFlag(bizExperiment.getId());
				continue;
			}

			try {
				mlflowWrapper.deleteRun(experimentId);
				++index;
				updateMlflowDelFlag(bizExperiment.getId());
			} catch (MlflowHttpException e) {
				try {
					Thread.sleep(SLEEP_MILLS);
				} catch (InterruptedException ex) {
					throw new RuntimeException(ex);
				}

			}
		}
	}

	private void updateMlflowDelFlag(Long id) {
		baseMapper.deleteMlflow(id, 1, LocalDateTime.now());
	}

	public void clearExpiredFile() {
		try {
			clearExpiredFile(ExperimentFileType.MODEL);
			clearExpiredFile(ExperimentFileType.WEIGHTS);
		} catch (Throwable throwable) {
			log.error("clearExpiredFile catch exception: {}", throwable.getMessage());
		}
	}

	private void clearExpiredFile(ExperimentFileType fileType) {
		String root = getExperimentBucketPath(fileType);

		try (LazyDirectoryIterator iterator = new LazyDirectoryIterator(root)) {
			while (iterator.hasNext()) {
				String file = iterator.next();
				String dbFile = PathUtils.getLastTwoBranches(file);

				try {
					fileLock.lock();
					log.info("clearExpiredFile file lock");
					Boolean exist = existExperiment(fileType, dbFile);
					if (!exist) {
						LocalDateTime maxUpdateTime = getDelMaxUpdateTime(fileType, dbFile);
						if (fileExpired(maxUpdateTime)) {
							if (PathUtils.cleanFileAndDirectory(Path.of(file))) {
								log.info("file: {} expired and has been clean", file);
							}
						}
					}
				} catch (Throwable throwable) {
					throw new RuntimeException(throwable);
				} finally {
					fileLock.unlock();
					log.info("clearExpiredFile file unlock");
				}

				try {
					Thread.sleep(500);
				} catch (InterruptedException ignored) {

				}
			}
		}
	}

	private String getExperimentBucketPath(ExperimentFileType fileType) {
		if (ExperimentFileType.MODEL.equals(fileType)) {
			return getBucketPath(CommonConstants.MODEL_BUCKET);
		} else {
			return getBucketPath(CommonConstants.WEIGHTS_BUCKET);
		}
	}

	private Boolean existExperiment(ExperimentFileType fileType, String dbFile) {
		if (ExperimentFileType.MODEL.equals(fileType)) {
			return baseMapper.hasExperiments(dbFile, null);
		} else {
			return baseMapper.hasExperiments(null, dbFile);
		}
	}

	private LocalDateTime getDelMaxUpdateTime(ExperimentFileType fileType, String dbFile) {
		if (ExperimentFileType.MODEL.equals(fileType)) {
			return baseMapper.getDelMaxUpdateTime(dbFile, null);
		} else {
			return baseMapper.getDelMaxUpdateTime(null, dbFile);
		}
	}

	private static boolean fileExpired(LocalDateTime maxUpdateTime) {
		if (maxUpdateTime == null) {
			return true;
		}
		LocalDateTime target = maxUpdateTime.plusDays(FILE_EXPIRED_DAYS);
		return LocalDateTime.now().isAfter(target) || LocalDateTime.now().equals(target);
	}
}
