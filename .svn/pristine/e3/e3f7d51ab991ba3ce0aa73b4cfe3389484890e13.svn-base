package com.aotain.smmsapi.task.smmsreturn.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.aotain.common.config.annotation.MyBatisDao;
import com.aotain.smmsapi.task.bean.OperatorCache;

/**
 * 基础数据预审缓存表（CACHE_ISMS_BASE_INFO）操作接口
 * 
 * @author liuz@aotian.com
 * @date 2018年8月16日 下午2:36:46
 */
@MyBatisDao
public interface OperatorCacheDao {
	/**
	 * 根据jyzId,houseId,userId查询操作缓存记录
	 * @param jyzId
	 * @param houseId 多个用逗号分隔
	 * @param userId 多个用逗号分隔
	 * @return
	 */
	public List<OperatorCache> getOperatorCache(@Param("jyzId")String jyzId,@Param("houseId")String houseId,@Param("userId")String userId);

	public int deleteOperatorCacheByJyzId(@Param("jyzIds")List<Long> jyzIds);

	public int deleteOperatorCacheByUserId(@Param("userIds")List<Long> userIds);

	public int deleteOperatorCacheByHouseId(@Param("houseIds")List<Long> houseIds);
}
