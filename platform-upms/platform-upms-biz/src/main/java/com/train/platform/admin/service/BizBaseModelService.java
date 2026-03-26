package com.train.platform.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.train.platform.admin.api.dto.BaseModelDTO;
import com.train.platform.admin.api.entity.BizBaseDataset;
import com.train.platform.admin.api.entity.BizBaseModel;
import com.train.platform.admin.api.vo.BaseModelVO;
import com.train.platform.common.core.util.R;

import java.util.List;

/**
 * <p>
 * 基础模型管理 服务类
 * </p>
 *
 * @author zhouzhipeng
 * @since 2024-06-20
 */
public interface BizBaseModelService extends IService<BizBaseModel> {

    R saveBaseModel();

	List<BaseModelVO> listGroup(BizBaseModel bizBaseModel);

	R deleteModelByIds(Long[] ids);

	boolean updateBaseModelConfigFile(BizBaseModel bizBaseModel);

    R getBaseModelConfig(Long id);

	R deleteBaseModelById(Long id);

	void updateBaseModelConfig(BaseModelDTO baseModelDTO);

	void saveModel(BizBaseModel bizBaseModel);

	R updateBaseModelById(BizBaseModel bizBaseModel);

}
