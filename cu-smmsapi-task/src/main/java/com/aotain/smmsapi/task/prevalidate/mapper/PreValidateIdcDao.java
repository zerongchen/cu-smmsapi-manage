package com.aotain.smmsapi.task.prevalidate.mapper;

import com.aotain.common.config.annotation.MyBatisDao;
import com.aotain.cu.serviceapi.model.HouseInformation;
import com.aotain.cu.serviceapi.model.IdcInformation;
import com.aotain.cu.serviceapi.model.UserInformation;

import java.util.List;

@MyBatisDao
public interface PreValidateIdcDao {

	/**
	 * 获取经营者信息
	 * @param information
	 * @return
	 */
	List<IdcInformation> getValidateIdcList( IdcInformation information );

	/**
	 * jyz 数量
	 * @param information
	 * @return
	 */
	int jyzNum(IdcInformation information);


	/**
	 * 更新JYZ
	 * @param information
	 * @return
	 */
	int submitJyz(IdcInformation information);

	/**
	 * 更新机房
	 * @param houseInformation
	 * @return
	 */
	int submitHoset( HouseInformation houseInformation);

	/**
	 *  更新用户
	 * @param userInformation
	 * @return
	 */
	int submitUser( UserInformation userInformation);

	/**
	 * 写经营者记录表
	 * @param idcInformation
	 * @return
	 */
	int writeIdcLog(IdcInformation idcInformation);

	/**
	 * 机房用户，未提交预审的直接提交预审
	 * @return
	 */
	int setUnvalObj2Val();

}
