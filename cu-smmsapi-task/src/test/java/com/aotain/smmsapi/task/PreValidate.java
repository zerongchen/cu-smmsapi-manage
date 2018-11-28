package com.aotain.smmsapi.task;

import com.aotain.cu.serviceapi.model.*;
import com.aotain.smmsapi.task.constant.DealFlagConstant;
import com.aotain.smmsapi.task.prevalidate.mapper.PreValidateHouseDao;
import com.aotain.smmsapi.task.prevalidate.mapper.PreValidateIdcDao;
import com.aotain.smmsapi.task.prevalidate.mapper.PreValidateUserDao;
import com.aotain.smmsapi.task.prevalidate.service.PreValidateUser;
import com.aotain.smmsapi.task.prevalidate.service.impl.PreValidateIdcImpl;
import com.aotain.smmsapi.task.quartz.PreValidateService;
import net.bytebuddy.asm.Advice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mortbay.util.ajax.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring-smmstask-quartz.xml" })
public class PreValidate {

	@Autowired
	private PreValidateIdcDao preValidateIdcDao;

	@Autowired
	private PreValidateHouseDao preValidateHouseDao;

//	@Autowired
//	private PreValidateService preValidateService;

	@Autowired
	private PreValidateUser preValidateUser;

	@Autowired
	private PreValidateUserDao preValidateUserDao;


	@Test
	public void validate(){

		List<IdcInformation> list = preValidateIdcDao.getValidateIdcList(new IdcInformation());
		System.out.println(JSON.toString(list));
	}

	@Test
	public void house(){
		List<HouseInformation> houseInformations = preValidateHouseDao.getValidateHouseList(new HouseInformation());
		if(houseInformations!=null && !houseInformations.isEmpty()){
			for (HouseInformation houseInformation:houseInformations){
				List<UserInformation> list = preValidateHouseDao.getPreUsers(houseInformation);
				System.out.println(JSON.toString(list));
			}
		}
	}

	@Test
	public void testService(){
//		preValidateService.handlePreValidate();
//		preValidateUser.handleValidateUser();

		UserInformation param = new UserInformation();
//		param.setDealFlag(DealFlagConstant.RPT_VARIFY.getDealFlag());
		param.setUserId(124319l);
		List<UserInformation> users = preValidateUserDao.getValidateUserList(param);
		for (UserInformation userInformation:users){
			List<HouseInformation> houseInformations =  preValidateUserDao.getPreHouse(userInformation);
			System.out.println(houseInformations.size());
		}
	}
}
