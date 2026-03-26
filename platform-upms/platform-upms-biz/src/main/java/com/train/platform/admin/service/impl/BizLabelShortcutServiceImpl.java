package com.train.platform.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.train.platform.admin.api.entity.BizLabelShortcut;
import com.train.platform.admin.service.BizLabelShortcutService;
import com.train.platform.admin.mapper.BizLabelShortcutMapper;
import com.train.platform.common.core.util.R;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author li
* @description 针对表【aircas_label_shortcut(用户标注快捷键表)】的数据库操作Service实现
* @createDate 2025-02-13 16:25:11
*/
@Service
public class BizLabelShortcutServiceImpl extends ServiceImpl<BizLabelShortcutMapper, BizLabelShortcut>
    implements BizLabelShortcutService{

	@Override
	public IPage<List<BizLabelShortcut>> getShortcutByPage(Page page, BizLabelShortcut bizLabelShortcut) {
		return baseMapper.getShortcutByPage(page, bizLabelShortcut);
	}

	@Override
	public BizLabelShortcut getLabelShortcutById(Long id) {
		return baseMapper.selectById(id);
	}

	@Override
	public R removeLabelShortcutByIds(List<Long> list) {
		return R.ok(baseMapper.deleteBatchIds(list));
	}

	@Override
	public R saveLabelShortcut(BizLabelShortcut bizLabelShortcut) {
		return R.ok(baseMapper.insert(bizLabelShortcut));
	}
}




