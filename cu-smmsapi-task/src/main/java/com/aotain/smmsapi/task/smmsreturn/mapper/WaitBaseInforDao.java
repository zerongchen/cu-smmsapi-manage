package com.aotain.smmsapi.task.smmsreturn.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.aotain.common.config.annotation.MyBatisDao;
import com.aotain.cu.serviceapi.model.HouseInformation;
import com.aotain.cu.serviceapi.model.IdcInformation;
import com.aotain.cu.serviceapi.model.UserInformation;

/**
 * 等待上报表数据库操作接口
 * 
 * @author liuz@aotian.com
 * @date 2018年8月23日 下午8:19:00
 */
@MyBatisDao
public interface WaitBaseInforDao {
	/**
	 * 查询等待表的经营者信息
	 * @param submitId
	 * @param jyzIds 多个用','分隔
	 * @return
	 */
	public List<IdcInformation> getIdcInformation(@Param("submitId")Long submitId,@Param("jyzIds")String jyzIds);	
	
	/**
	 * 查询等待表的机房信息
	 * @param submitId
	 * @param userIds 多个用','分隔
	 * @return
	 */
	public List<HouseInformation> getHouseInformation(@Param("submitId")long submitId,@Param("houseIds")String houseIds);
	
	/**
	 * 查询等待表的用户信息
	 * @param userInfo
	 * @return
	 */
	public List<UserInformation> getUserInformation(@Param("submitId")long submitId,@Param("userIds")String userIds);
}
