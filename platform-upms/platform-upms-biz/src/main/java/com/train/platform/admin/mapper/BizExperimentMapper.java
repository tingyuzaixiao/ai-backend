package com.train.platform.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.train.platform.admin.api.entity.BizExperiment;
import com.train.platform.admin.api.entity.BizGpuResource;
import com.train.platform.admin.api.vo.CheckpointEndVO;
import com.train.platform.admin.api.vo.ExperimentEndVO;
import com.train.platform.admin.api.vo.ExperimentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author lee
 * @description 针对表【aircas_train(训练记录表)】的数据库操作Mapper
 * @createDate 2024-05-24 14:36:51
 * @entity com.train.platform.admin.api.entity.BizTrain
 */
@Mapper
public interface BizExperimentMapper extends BaseMapper<BizExperiment> {

    List<BizGpuResource> selectFreeGpu();

	void updateGpuResource(BizGpuResource bizGpuResource);

	List<BizExperiment> getExperiments(@Param("query") BizExperiment bizExperiment);

	List<BizExperiment> getExperimentsToDelete();

	List<String> getExperimentIdsByProgramId(String programId);

	List<BizExperiment> getExperimentsByIds(@Param("ids") List<Long> ids,
											@Param("statuses") List<String> statuses);

	void updateStatus(@Param("dstStatus") String dstStatus, @Param("srcStatus") String srcStatus,
					  @Param("id") Long id, @Param("updateTime") LocalDateTime updateTime);

	int updateToRunning(@Param("dstStatus") String dstStatus, @Param("srcStatus") String srcStatus,
						 @Param("id") Long id, @Param("resourceId") Long resourceId,
						 @Param("updateTime") LocalDateTime updateTime);

	int updateToKilling(@Param("dstStatus") String dstStatus, @Param("stopType") Integer stopType,
						@Param("id") Long id, @Param("srcStatus") String srcStatus,
						@Param("updateTime") LocalDateTime updateTime);

	void updateToKilled(@Param("dstStatus") String dstStatus, @Param("id") Long id,
						@Param("srcStatus") String srcStatus,
						@Param("updateTime") LocalDateTime updateTime);

	void logicDeleteByProgramId(@Param("programId") String programId, @Param("updateTime") LocalDateTime updateTime);
	int logicDeleteByIds(@Param("ids") List<Long> ids, @Param("updateTime") LocalDateTime updateTime);
	void deleteMlflow(@Param("id") Long id, @Param("mlflowDel") Integer mlflowDel,
					  @Param("updateTime") LocalDateTime updateTime);

	Boolean hasRepeatName(@Param("name") String name, @Param("programId") String programId);
	Boolean hasExperiments(@Param("modelFile") String modelFile, @Param("weightsFile") String weightsFile);
	LocalDateTime getDelMaxUpdateTime(@Param("modelFile") String modelFile, @Param("weightsFile") String weightsFile);
}
