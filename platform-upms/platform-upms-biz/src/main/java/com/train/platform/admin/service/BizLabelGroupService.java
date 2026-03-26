package com.train.platform.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.train.platform.admin.api.entity.BizLabelGroup;
import com.baomidou.mybatisplus.extension.service.IService;
import com.train.platform.admin.api.vo.LabelGroupVO;
import com.train.platform.common.core.util.R;

import java.util.List;
import java.util.Map;


/**
* @author li
* @description 针对表【aircas_label_group(标签组)】的数据库操作Service
* @createDate 2024-09-11 10:18:13
*/
public interface BizLabelGroupService extends IService<BizLabelGroup> {

	IPage getGroupByPage(Page page, BizLabelGroup bizLabelGroup);

    LabelGroupVO getLabelGroupById(Long id);

    R saveLabelGroup(BizLabelGroup bizLabelGroup);

    R removeLabelGroupByIds(List<Long> groupIds);

    void copy(Map<String, Object> params);

	List<LabelGroupVO> listLabelGroup(BizLabelGroup bizLabelGroup);
}
