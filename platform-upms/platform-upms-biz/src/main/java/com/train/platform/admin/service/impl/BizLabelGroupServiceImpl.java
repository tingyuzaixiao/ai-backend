package com.train.platform.admin.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.train.platform.admin.api.entity.BizLabelGroup;
import com.train.platform.admin.api.entity.BizLabelTag;
import com.train.platform.admin.api.vo.LabelGroupVO;
import com.train.platform.admin.mapper.BizLabelTagMapper;
import com.train.platform.admin.service.BizLabelGroupService;
import com.train.platform.admin.mapper.BizLabelGroupMapper;
import com.train.platform.common.core.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.train.platform.common.core.constant.PlatformConstants.COLOR;
import static com.train.platform.common.core.constant.PlatformConstants.TAG_NAME;


/**
 * @author li
 * @description 针对表【aircas_label_group(标签组)】的数据库操作Service实现
 * @createDate 2024-09-11 10:18:13
 */
@Service
public class BizLabelGroupServiceImpl extends ServiceImpl<BizLabelGroupMapper, BizLabelGroup>
		implements BizLabelGroupService {

	@Autowired
	private BizLabelTagMapper bizLabelTagMapper;

	@Override
	public IPage getGroupByPage(Page page, BizLabelGroup bizLabelGroup) {
		return baseMapper.selectLabelGroupPage(page, bizLabelGroup);
	}

	@Override
	public LabelGroupVO getLabelGroupById(Long id) {
		return baseMapper.selectLabelGroupById(id);
	}

	@Override
	public R saveLabelGroup(BizLabelGroup bizLabelGroup) {
		int insert = baseMapper.insert(bizLabelGroup);
		if (insert > 0) {
			return R.ok(bizLabelGroup.getGroupId());
		} else {
			return R.failed("保存失败");
		}

	}

	@Override
	public R removeLabelGroupByIds(List<Long> groupIds) {
		int del1 = baseMapper.deleteBatchIds(groupIds);
		int del2 = bizLabelTagMapper.delete(Wrappers.lambdaQuery(BizLabelTag.class).in(BizLabelTag::getGroupId, groupIds));
		return del1 + del2 > 0 ? R.ok() : R.failed("删除失败");
	}

	@Override
	public void copy(Map<String, Object> params) {
		BizLabelGroup bizLabelGroup = JSONObject.parseObject(JSONObject.toJSONString(params), BizLabelGroup.class);
		baseMapper.insert(bizLabelGroup);
		String tagNames = params.get(TAG_NAME).toString();
		String colors = params.get(COLOR).toString();
		String[] tagNameList = tagNames.split(",");
		String[] colorList = colors.split(",");

//		List<BizLabelTag> list = new ArrayList<>();
		for (int i = 0; i < tagNameList.length; i++) {
			BizLabelTag labelTag = new BizLabelTag();
			labelTag.setGroupId(bizLabelGroup.getGroupId());
			labelTag.setTagName(tagNameList[i]);
			labelTag.setColor(colorList[i]);
			bizLabelTagMapper.insert(labelTag);
		}
	}

	@Override
	public List<LabelGroupVO> listLabelGroup(BizLabelGroup bizLabelGroup) {
		return baseMapper.listLabelGroup(bizLabelGroup);
	}
}




