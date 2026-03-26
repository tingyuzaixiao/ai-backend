package com.train.platform.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.train.platform.admin.api.entity.BizModel;
import com.train.platform.admin.api.vo.ModelVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author li
* @description 针对表【aircas_model(机器学习模型表)】的数据库操作Mapper
* @createDate 2024-06-19 09:13:55
* @Entity com.train.platform.admin.api.entity.BizModel
*/
@Mapper
public interface BizModelMapper extends BaseMapper<BizModel> {

    int deleteByPrimaryKey(Long id);

    int insert(BizModel record);

    int insertSelective(BizModel record);

    ModelVO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(BizModel record);

    int updateByPrimaryKey(BizModel record);

	ModelVO getModelInfoById(Long id);

	List<ModelVO> listModelVOsByBaseModelId(Long baseModelId);

	List<Long> selectModelIdsByBaseModelId(Long baseModelId);

    IPage<ModelVO> getModelPage(Page page, @Param("query") BizModel bizModel);

    List<ModelVO> getModelVOList(List<Long> list);
}
