package com.train.platform.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.train.platform.admin.api.entity.BizLabel;
import com.train.platform.admin.api.vo.LabelVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author li
 * @description 针对表【aircas_label】的数据库操作Mapper
 * @createDate 2024-07-12 13:57:59
 * @Entity com.train.platform.admin.BizLabel
 */
@Mapper
public interface BizLabelMapper extends BaseMapper<BizLabel> {

	List<LabelVO> selectLabelListByFileId(Long fileId, String labelFileName, Long taskId, Long tagId);

	List<LabelVO> selectLabelListByTaskId(Long taskId);

    IPage selectLabelPage(Page page, @Param("query") BizLabel bizLabel);

	List<Long> selectLabelIdsByTaskIds(List<Long> list);

	void updateBizLabels(List<BizLabel> list);

    void updateLabelByLabelTag(BizLabel bizLabel);

	void updateReferenceLabel(BizLabel bizLabel);

    LabelVO getDetails(BizLabel query);

	void insertLabelVO(LabelVO labelVO);

	int insertLabel(BizLabel bizLabel);
}




