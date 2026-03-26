package com.train.platform.admin.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.train.platform.admin.api.dto.LabelTaskDTO;
import com.train.platform.admin.api.entity.BizLabelTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.train.platform.admin.api.vo.BaseDatasetVO;
import com.train.platform.admin.api.vo.LabelFileVO;
import com.train.platform.admin.api.vo.LabelTaskListVO;
import com.train.platform.admin.api.vo.LabelTaskVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author li
* @description 针对表【aircas_label_task(标注任务表)】的数据库操作Mapper
* @createDate 2024-09-12 16:40:34
* @Entity com.train.platform.admin.BizLabelTask
*/
@Mapper
public interface BizLabelTaskMapper extends BaseMapper<BizLabelTask> {

	LabelTaskVO selectLabelTaskById(Long taskId);

    IPage getLabelTaskByPage(Page page, @Param("query") BizLabelTask bizLabelTask);

	IPage<BaseDatasetVO> getLabelTaskByPageNew(Page page, @Param("query") BizLabelTask bizLabelTask);

	List<LabelTaskListVO> listAllByBaseDatasetId(Long id);

    List<LabelFileVO> selectExportLabelTask(LabelTaskDTO bizLabelTask);

	List<LabelFileVO> selectDetectSegExportLabelTask(BizLabelTask bizLabelTask);

	List<LabelTaskListVO> selectLabelTaskByBaseDatasetId(@Param("baseDatasetId") Long baseDatasetId);

	List<LabelTaskListVO> selectLabelTaskByIds(@Param("query") BizLabelTask bizLabelTask, @Param("list") List<Long> ids);
}




