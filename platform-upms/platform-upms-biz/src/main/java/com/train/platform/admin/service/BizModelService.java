package com.train.platform.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.train.platform.admin.api.dto.ModelDTO;
import com.train.platform.admin.api.entity.BizModel;
import com.train.platform.admin.api.vo.ModelVO;
import com.train.platform.common.core.util.R;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * <p>
 * 模型管理 服务类
 * </p>
 *
 * @author zhouzhipeng
 * @since 2024-01-20
 */
public interface BizModelService extends IService<BizModel> {

	/**
	 * 下载模型权重信息
	 *
	 * @param filename 文件名称
	 */
	ResponseEntity<Resource> downloadFile(@PathVariable String filename, HttpServletRequest request);

	R deleteModelByIds(Long[] ids);

	R saveModel(ModelDTO modelDTO);

	String getModelConfigByModelId(Long id, Boolean isBaseModel, String configType, String modelClass);

	void updateModelConfigByModelId(ModelDTO modelDTO);

	R updateModelById(BizModel bizModel);

    IPage<ModelVO> getModelPage(Page page, BizModel bizModel);
}
