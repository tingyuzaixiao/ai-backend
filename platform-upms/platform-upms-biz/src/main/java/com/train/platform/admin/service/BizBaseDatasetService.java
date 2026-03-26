package com.train.platform.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.train.platform.admin.api.entity.BizBaseDataset;
import com.train.platform.admin.api.vo.BaseDatasetVO;
import com.train.platform.common.core.util.R;

import java.util.List;

/**
 * @author lee
 * @description 针对表【aircas_base_dataset】的数据库操作Service
 * @createDate 2024-05-23 14:20:24
 */
public interface BizBaseDatasetService extends IService<BizBaseDataset> {

    Long saveBaseDataset(BizBaseDataset bizBaseDataset);

	IPage<List<BaseDatasetVO>> getBaseDatasetVOsPage(Page page, BizBaseDataset bizBaseDataset);

    List<BaseDatasetVO> listGroup(BizBaseDataset bizBaseDataset);

    List<Long> getImageIds(Long id);

    int updateBaseDataset(BizBaseDataset bizBaseDataset);

    String getLabelListById(Long baseDatasetId);

	List<Long> getFilesByBaseDatasetId(Long baseDatasetId);

    R removeBaseDatasetById(Long id);

	R getState(BizBaseDataset bizBaseDataset);

    Boolean checkMd5(Long baseDatasetId, String md5);
}
