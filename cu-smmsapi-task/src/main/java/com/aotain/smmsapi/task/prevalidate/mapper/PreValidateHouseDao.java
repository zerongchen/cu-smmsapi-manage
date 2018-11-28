package com.aotain.smmsapi.task.prevalidate.mapper;

import com.aotain.common.config.annotation.MyBatisDao;
import com.aotain.cu.serviceapi.model.*;

import java.util.List;

@MyBatisDao
public interface PreValidateHouseDao {

	/**
	 * 获取机房主体信息
	 * @param houseInformation
	 * @return
	 */
	List<HouseInformation> getValidateHouseList( HouseInformation houseInformation );


	/**
	 * 正式表机房主体数量
	 * @param houseInformation
	 * @return
	 */
	int houseNum(HouseInformation houseInformation);
	/**
	 * 更新机房主体
	 * @param houseInformation
	 * @return
	 */
	int submitHouse( HouseInformation houseInformation );

	/**
	 * 更新机房机架
	 * @param houseFrameInformation
	 * @return
	 */
	int submitHouseFrame( HouseFrameInformation houseFrameInformation );

	/**
	 * 更新机房IP
	 * @param houseIPSegmentInformation
	 * @return
	 */
	int submitHouseIpseg( HouseIPSegmentInformation houseIPSegmentInformation );

	/**
	 * 更新机房链路
	 * @param houseGatewayInformation
	 * @return
	 */
	int submitHouseGateway( HouseGatewayInformation houseGatewayInformation );


	/**
	 * 获取机房下面的用户
	 * @param houseInformation
	 * @return
	 */
	List<UserInformation> getPreUsers(HouseInformation houseInformation);

	/**
	 * 获取正式表中的机房信息
	 * @param houseInformation
	 * @return
	 */
	HouseInformation getRptHouse(HouseInformation houseInformation);

	/**
	 * 获取正式表机房下面的用户
	 * @param houseInformation
	 * @return
	 */
	List<UserInformation> getRptUsers(HouseInformation houseInformation);

	/**
	 * 根据上报用户获取操作表的用户
	 * @param list
	 * @return
	 */
	List<UserInformation> getPreUsersByRptUser(List<UserInformation> list);


	/**
	 * 写基础等待表
	 */
	void writeHouseLog(HouseInformation houseInformation);
	void writeHouseIpLog(HouseIPSegmentInformation houseIPSegmentInformation);
	void writeHouseUserFrameLog( HouseUserFrameInformation houseUserFrameInformation);
	void writeHouseFrameLog( HouseFrameInformation houseFrameInformation);
	void writeHouseGatewayLog(HouseGatewayInformation houseGatewayInformation);


}
