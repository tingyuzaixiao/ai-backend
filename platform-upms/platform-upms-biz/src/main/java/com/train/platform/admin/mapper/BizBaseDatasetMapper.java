package com.train.platform.admin.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.train.platform.admin.api.entity.BizBaseDataset;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.train.platform.admin.api.vo.BaseDatasetVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author lee
 * @description 针对表【aircas_base_dataset】的数据库操作Mapper
 * @createDate 2024-05-23 14:20:24
 * @entity com.train.platform.admin.api.entity.BizBaseDataset
 */
@Mapper
public interface BizBaseDatasetMapper extends BaseMapper<BizBaseDataset> {

	IPage<List<BaseDatasetVO>> getBaseDatasetVOsPage(Page page, @Param("query") BizBaseDataset bizBaseDataset);

	Long insertBizBaseDataset(BizBaseDataset bizBaseDataset);

	List<BaseDatasetVO> selectListGroup(@Param("query") BizBaseDataset bizBaseDataset);

    List<Long> selectImageIds(Long id);

    String getLabelListById(Long datasetId);

	List<Long> getFilesByBaseDatasetId(Long baseDatasetId);

	void updateState(@Param("baseDatasetId") Long baseDatasetId, @Param("state") String state);
}
