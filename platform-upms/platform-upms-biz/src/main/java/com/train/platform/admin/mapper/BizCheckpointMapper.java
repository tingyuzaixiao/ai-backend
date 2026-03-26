package com.train.platform.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.train.platform.admin.api.entity.BizCheckpoint;
import org.apache.ibatis.annotations.Mapper;

/**
* @author lee
* @description 针对表【aircas_checkpoint(权重信息表)】的数据库操作Mapper
* @createDate 2024-06-17 15:45:10
* @Entity com.train.platform.admin.BizCheckpoint
*/
@Mapper
public interface BizCheckpointMapper extends BaseMapper<BizCheckpoint> {

    int deleteByPrimaryKey(Long id);

    int insert(BizCheckpoint record);

    int insertSelective(BizCheckpoint record);

    BizCheckpoint selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(BizCheckpoint record);

    int updateByPrimaryKey(BizCheckpoint record);

}
