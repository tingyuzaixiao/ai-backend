package com.train.platform.admin.mapper;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.train.platform.admin.api.entity.BizLabelGroup;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.train.platform.admin.api.vo.LabelGroupVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author li
 * @description 针对表【aircas_label_group(标签组)】的数据库操作Mapper
 * @createDate 2024-09-11 10:18:13
 * @Entity com.train.platform.admin.BizLabelGroup
 */
@Mapper
public interface BizLabelGroupMapper extends BaseMapper<BizLabelGroup> {
	LabelGroupVO selectLabelGroupById(Long groupId);

	IPage selectLabelGroupPage(Page page, @Param("query") BizLabelGroup bizLabelGroup);

	List<LabelGroupVO> listLabelGroup(@Param("query") BizLabelGroup bizLabelGroup);

}




