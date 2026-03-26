package com.train.platform.admin.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.train.platform.admin.api.dto.LabelTagDTO;
import com.train.platform.admin.api.entity.BizLabel;
import com.train.platform.admin.api.entity.BizLabelTag;
import com.train.platform.admin.api.entity.BizLabelTagInfo;
import com.train.platform.admin.api.entity.BizLabelTask;
import com.train.platform.admin.mapper.BizLabelMapper;
import com.train.platform.admin.mapper.BizLabelTagInfoMapper;
import com.train.platform.admin.mapper.BizLabelTaskMapper;
import com.train.platform.admin.service.BizLabelTagInfoService;
import com.train.platform.admin.service.BizLabelTagService;
import com.train.platform.admin.mapper.BizLabelTagMapper;
import com.train.platform.common.core.util.R;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.train.platform.common.core.constant.PlatformConstants.CLASSIFY_MODE;


/**
 * @author li
 * @description 针对表【aircas_label_tag(标签)】的数据库操作Service实现
 * @createDate 2024-09-11 10:36:17
 */
@Service
public class BizLabelTagServiceImpl extends ServiceImpl<BizLabelTagMapper, BizLabelTag>
		implements BizLabelTagService {

	@Autowired
	private BizLabelTagInfoMapper bizLabelTagInfoMapper;

	@Override
	public IPage getLabelTagByPage(Page page, BizLabelTag bizLabelTag) {
		return baseMapper.selectLabelTagPage(page, bizLabelTag);
	}

	@Override
	public R saveLabelTag(BizLabelTag bizLabelTag) {
		//根据groupId和tagName判断是否存在
		int insert = baseMapper.insert(bizLabelTag);
		if (insert > 0) {
			return R.ok(bizLabelTag.getTagId());
		} else {
			return R.failed("保存失败");
		}
	}

	@Override
	public R removeLabelTagByIds(List<Long> list) {
		int del = baseMapper.deleteBatchIds(list);
		if (del > 0) {
			return R.ok("删除成功");
		} else {
			return R.failed("删除失败");
		}
	}

	@Override
	public int updateLabelTagById(LabelTagDTO labelTagDTO) {
		if (labelTagDTO.getTaskId() != null) {
			//修改分类 标签 tagInfo
			if (Objects.equals(labelTagDTO.getLabelType(), CLASSIFY_MODE)) {
				//修改tagInfo表
				bizLabelTagInfoMapper.update(Wrappers.<BizLabelTagInfo>lambdaUpdate()
						.eq(BizLabelTagInfo::getTagId, labelTagDTO.getTagId())
						.set(BizLabelTagInfo::getTagInfo, labelTagDTO.getTagName()));
			}
		}
		return baseMapper.update(Wrappers.<BizLabelTag>lambdaUpdate().
				eq(BizLabelTag::getTagId, labelTagDTO.getTagId()).
				set(BizLabelTag::getTagName, labelTagDTO.getTagName()));
	}
}





