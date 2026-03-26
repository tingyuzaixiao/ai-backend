package com.train.platform.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.train.platform.admin.api.entity.BizLabelShortcut;
import com.baomidou.mybatisplus.extension.service.IService;
import com.train.platform.common.core.util.R;

import java.util.List;

/**
* @author li
* @description 针对表【aircas_label_shortcut(用户标注快捷键表)】的数据库操作Service
* @createDate 2025-02-13 16:25:11
*/
public interface BizLabelShortcutService extends IService<BizLabelShortcut> {

	IPage getShortcutByPage(Page page, BizLabelShortcut bizLabelShortcut);

	BizLabelShortcut getLabelShortcutById(Long id);

	R removeLabelShortcutByIds(List<Long> list);

	R saveLabelShortcut(BizLabelShortcut bizLabelShortcut);
}
