package com.train.platform.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.train.platform.admin.api.entity.BizGpuResource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * gpu资源管理 Mapper 接口
 * </p>
 *
 * @author zhouzhipeng
 * @since 2014-06-14
 */
@Mapper
public interface BizGpuResourceMapper extends BaseMapper<BizGpuResource> {
	List<BizGpuResource> getBizGpuResources(@Param("query") BizGpuResource bizGpuResource);

    void updateGpuState(BizGpuResource bizGpuResource);
}
