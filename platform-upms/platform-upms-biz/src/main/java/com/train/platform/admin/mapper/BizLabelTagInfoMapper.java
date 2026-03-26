package com.train.platform.admin.mapper;

import com.train.platform.admin.api.entity.BizLabelTagInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.train.platform.admin.api.vo.LabelTagInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author li
* @description 针对表【aircas_label_tag_info】的数据库操作Mapper
* @createDate 2024-09-27 10:52:04
* @Entity com.train.platform.admin.domain.BizLabelTagInfo
*/
@Mapper
public interface BizLabelTagInfoMapper extends BaseMapper<BizLabelTagInfo> {
	List<LabelTagInfoVO> selectByLabelId(@Param("labelId") Long labelId);

    void deleteByLabelId(Long labelId);

	List<LabelTagInfoVO> selectByLabelIds(@Param("list") List<Long> labelIds);
}




