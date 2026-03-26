package com.train.platform.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.train.platform.admin.api.dto.FileDTO;
import com.train.platform.admin.api.dto.LabelTaskDTO;
import com.train.platform.admin.api.entity.BizLabelTask;
import com.baomidou.mybatisplus.extension.service.IService;
import com.train.platform.admin.api.vo.FileVO;
import com.train.platform.admin.api.vo.LabelFileVO;
import com.train.platform.admin.api.vo.LabelTaskVO;
import com.train.platform.common.core.util.R;

import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * @author li
 * @description 针对表【aircas_label_task(标注任务表)】的数据库操作Service
 * @createDate 2024-09-12 16:40:34
 */
public interface BizLabelTaskService extends IService<BizLabelTask> {

	IPage getLabelTaskByPage(Page page, BizLabelTask bizLabelTask);

	IPage getLabelTaskByPageNew(Page page, BizLabelTask bizLabelTask);

	LabelTaskVO getLabelTaskById(Long taskId);

	R saveLabelTask(LabelTaskDTO labelTaskDTO);

	Long saveLabelTask(BizLabelTask bizLabelTask);

	R removeLabelTaskByIds(List<Long> taskIds);

	R updateLabelTaskById(LabelTaskDTO labelTaskDTO);

    List<LabelFileVO> exportLabelTask(LabelTaskDTO bizLabelTask);

	IPage getLabeledTotal(Page page, FileDTO file);

	void exportLabelsToCsv(Writer writer, List<LabelFileVO> labelFileVOS);

	void exportLabelsToCsv(Writer writer, LabelTaskDTO bizLabelTask);

	String exportLabelsToJSON(LabelTaskDTO bizLabelTask);

	List<String> getJsonLabels(Long baseDatasetId, Long taskId);
}
