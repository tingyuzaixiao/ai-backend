package com.train.platform.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.train.platform.admin.api.entity.AlgoServerResource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AlgoServerResourceMapper extends BaseMapper<AlgoServerResource> {
	List<AlgoServerResource> getAlgoServerResources(@Param("query") AlgoServerResource algoServerResource);

	void logicDeleteByHeartbeat(@Param("updateTime") LocalDateTime updateTime,
								@Param("heartbeatTime") LocalDateTime heartbeatTime);

	void updateHeartbeat(@Param("query") AlgoServerResource algoServerResource);

	void updateByRegister(@Param("query") AlgoServerResource algoServerResource);
}
