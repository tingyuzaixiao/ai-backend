package com.train.platform.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.train.platform.admin.api.dto.ExperimentDTO;
import com.train.platform.admin.api.entity.BizExperiment;
import com.train.platform.admin.api.entity.BizGpuResource;
import com.train.platform.admin.api.entity.SysProgram;
import com.train.platform.admin.api.vo.*;
import com.train.platform.common.core.util.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author lee
 * @description 针对表【aircas_train(训练记录表)】的数据库操作Service
 * @createDate 2024-05-24 14:36:51
 */
public interface BizExperimentService extends IService<BizExperiment> {
	BizExperiment queryExperiment(Long id, String experimentId, String name);

	BizExperiment queryExperimentForWeb(Long id, String experimentId, String experimentName);

	IPage<BizExperiment> getExperimentsByPage(Integer current, Integer size, String programId);

	Boolean checkExperimentNameAvailable(String experimentName, String programId);

	ExperimentDetailsVO queryDetails(Long id);

	HistoryMetricVO queryMetricHistory(String experimentId, String metricName);

	List<ExperimentFileVO> listFiles(String experimentId, Integer fileType, String path);

	void downloadFile(HttpServletResponse response, String experimentId, Integer fileType, String file);

	void downloadExperimentFile(HttpServletResponse response, Long id, Integer fileType);

	BizExperiment createExperiment(BizExperiment bizExperiment, MultipartFile modelFile, MultipartFile weightsFile);

	BizExperiment updateExperimentConfig(BizExperiment bizExperiment, MultipartFile modelFile, MultipartFile weightsFile);

	int removeExperimentByIds(List<Long> ids);

	boolean removeByProgramId(String programId);

	BizExperiment stopRunning(Long id, Integer stopType);

	BizExperiment startRunning(Long id, Long resourceId);

	SysProgram getProgramByProgramId(String programId);
}
