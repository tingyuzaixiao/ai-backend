package com.train.platform.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.train.platform.admin.api.dto.LabelDTO;
import com.train.platform.admin.api.entity.BizLabel;
import com.baomidou.mybatisplus.extension.service.IService;
import com.train.platform.admin.api.entity.BizLabelTagInfo;
import com.train.platform.admin.api.vo.LabelVO;
import com.train.platform.common.core.util.R;

import java.util.List;

/**
 * @author li
 * @description 针对表【aircas_label】的数据库操作Service
 * @createDate 2024-07-12 13:57:59
 */
public interface BizLabelService extends IService<BizLabel> {
	void insertBatches(List<BizLabel> bizLabels);

	void insertLabelVOBatches(List<LabelVO> labelVOS);

	void updateBatches(List<BizLabel> bizLabels);

	IPage getByPage(Page page, BizLabel bizLabel);

	R saveLabel(LabelDTO bizLabel);

	R saveLabels(LabelDTO bizLabel);

	R updateBizLabelById(LabelDTO bizLabel);

	LabelVO getLabelById(Long id);

	LabelVO getDetails(BizLabel query);

	R syncLabel(Long taskId, String labelType, String subLabelType);

	R autoAsyncLabel();

	void insertTagInfoBatches(List<BizLabelTagInfo> bizLabelTagInfos);
}
