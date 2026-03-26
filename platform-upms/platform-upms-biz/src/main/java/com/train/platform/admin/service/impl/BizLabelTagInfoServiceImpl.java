package com.train.platform.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.train.platform.admin.api.entity.BizLabelTagInfo;
import com.train.platform.admin.service.BizLabelTagInfoService;
import com.train.platform.admin.mapper.BizLabelTagInfoMapper;
import org.springframework.stereotype.Service;

/**
* @author li
* @description 针对表【aircas_label_tag_info】的数据库操作Service实现
* @createDate 2024-09-27 10:52:04
*/
@Service
public class BizLabelTagInfoServiceImpl extends ServiceImpl<BizLabelTagInfoMapper, BizLabelTagInfo>
    implements BizLabelTagInfoService{

	@Override
	public void removeByLabelId(Long labelId) {
		baseMapper.deleteByLabelId(labelId);
	}
}




