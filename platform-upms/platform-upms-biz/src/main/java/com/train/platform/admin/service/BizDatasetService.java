package com.train.platform.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.train.platform.admin.api.dto.BizDatasetDTO;
import com.train.platform.admin.api.dto.DatasetImgDTO;
import com.train.platform.admin.api.entity.BizDataset;
import com.train.platform.admin.api.vo.DatasetVO;
import com.train.platform.admin.api.vo.FilePageVO;
import com.train.platform.common.core.util.R;


/**
 * @author lee
 * @description 针对表【aircas_dataset】的数据库操作Service
 * @createDate 2024-05-23 14:24:59
 */
public interface BizDatasetService extends IService<BizDataset> {

    Long saveBizDataset(BizDatasetDTO bizDataset);

	IPage<DatasetVO> getDatasetsPage(Page page, BizDataset bizDataset);

	FilePageVO getDatasetImgPage(DatasetImgDTO datasetImgDTO);

    void updateDatasetLabelProgress(BizDataset bizDataset);

	boolean updateById(BizDatasetDTO bizDatasetDTO);

	String getLabelListById(Long datasetId);

	R deleteById(Long id);
}
