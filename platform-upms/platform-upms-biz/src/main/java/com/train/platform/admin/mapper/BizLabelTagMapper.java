package com.train.platform.admin.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.train.platform.admin.api.entity.BizLabelTag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author li
 * @description 针对表【aircas_label_tag(标签)】的数据库操作Mapper
 * @createDate 2024-09-11 10:36:17
 * @Entity com.train.platform.admin.BizLabelTag
 */
@Mapper
public interface BizLabelTagMapper extends BaseMapper<BizLabelTag> {
	List<BizLabelTag> selectLabelTagByGroupId(Long groupId);

    IPage selectLabelTagPage(Page page, @Param("query") BizLabelTag bizLabelTag);

	List<BizLabelTag> selectTagsInTagIds(List<Long> list);

	void removeLabelTagByIds(List<Long> list);
}




