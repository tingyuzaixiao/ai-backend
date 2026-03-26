package com.train.platform.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.train.platform.admin.api.dto.LabelTagDTO;
import com.train.platform.admin.api.entity.BizLabelTag;
import com.baomidou.mybatisplus.extension.service.IService;
import com.train.platform.common.core.util.R;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;

/**
* @author li
* @description 针对表【aircas_label_tag(标签)】的数据库操作Service
* @createDate 2024-09-11 10:36:17
*/
public interface BizLabelTagService extends IService<BizLabelTag> {

	IPage getLabelTagByPage(Page page, BizLabelTag bizLabelTag);

	R saveLabelTag(BizLabelTag bizLabelTag);

    R removeLabelTagByIds(List<Long> list);

    int updateLabelTagById(@Valid LabelTagDTO bizLabelTag);
}
