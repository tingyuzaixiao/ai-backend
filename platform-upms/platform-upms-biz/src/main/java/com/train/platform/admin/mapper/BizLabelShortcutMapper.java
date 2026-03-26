package com.train.platform.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.train.platform.admin.api.entity.BizLabelShortcut;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author li
* @description 针对表【aircas_label_shortcut(用户标注快捷键表)】的数据库操作Mapper
* @createDate 2025-02-13 16:25:11
* @Entity com.train.platform.admin.BizLabelShortcut
*/
@Mapper
public interface BizLabelShortcutMapper extends BaseMapper<BizLabelShortcut> {

	IPage<List<BizLabelShortcut>> getShortcutByPage(Page page, BizLabelShortcut bizLabelShortcut);
}




