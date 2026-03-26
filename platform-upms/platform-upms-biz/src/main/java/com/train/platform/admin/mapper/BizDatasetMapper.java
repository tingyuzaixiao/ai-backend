package com.train.platform.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.train.platform.admin.api.entity.BizBaseDataset;
import com.train.platform.admin.api.entity.BizDataset;
import com.train.platform.admin.api.vo.DatasetVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author lee
 * @description 针对表【aircas_dataset】的数据库操作Mapper
 * @createDate 2024-05-23 14:24:59
 * @entity com.train.platform.admin.api.entity.BizDataset
 */
@Mapper
public interface BizDatasetMapper extends BaseMapper<BizDataset> {

	List<DatasetVO> listBizDatasetsByBaseDatasetId(Long id);

	IPage<DatasetVO> getDatasetsPage(Page page, @Param("query") BizDataset bizDataset);

	String getLabelListById(Long datasetId);

    DatasetVO getDatasetVOById(Long id);

	int updateDataset(BizBaseDataset bizBaseDataset);

	int updateDatasetByBaseDataset(BizBaseDataset bizBaseDataset);
}
