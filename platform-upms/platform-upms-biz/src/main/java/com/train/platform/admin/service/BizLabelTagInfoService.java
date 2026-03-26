package com.train.platform.admin.service;

import com.train.platform.admin.api.entity.BizLabelTagInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author li
* @description 针对表【aircas_label_tag_info】的数据库操作Service
* @createDate 2024-09-27 10:52:04
*/
public interface BizLabelTagInfoService extends IService<BizLabelTagInfo> {
	public void removeByLabelId(Long labelId);
}
