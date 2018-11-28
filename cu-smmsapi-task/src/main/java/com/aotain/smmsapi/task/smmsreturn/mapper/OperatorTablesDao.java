package com.aotain.smmsapi.task.smmsreturn.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.aotain.common.config.annotation.MyBatisDao;
import com.aotain.cu.serviceapi.model.HouseInformation;
import com.aotain.cu.serviceapi.model.UserInformation;

/**
 * 基础数据操作表数据库操作接口
 * 
 * @author liuz@aotian.com
 * @date 2018年8月16日 下午2:36:46
 */
@MyBatisDao
public interface OperatorTablesDao {
	public int deleteJyz(@Param("jyzId")Long jyzId);

	public int deleteHouses(@Param("houseIdList")List<Integer> houseIdList);

	public int deleteUsers(@Param("userIdList")List<Long> userIdList);

	/**
	 * 删除用户下的子节点信息（操作类型为3）
	 * @param userListUpdate
	 */
//	public void deleteUserChildren(@Param("userList")List<UserInformation> userListUpdate);
	
	/**
	 * 删除机房下的子节点信息（操作类型为3）
	 * @param userListUpdate
	 */
//	public void deleteHouseChildren(@Param("houseList")List<HouseInformation> houseListUpdate);
}
