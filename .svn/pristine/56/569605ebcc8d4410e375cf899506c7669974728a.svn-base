package com.aotain.smmsapi.task.serviceapi.report;

import java.util.List;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.aotain.cu.serviceapi.dto.ResultDto;
import com.aotain.cu.serviceapi.model.HouseInformation;
import com.aotain.cu.serviceapi.model.UserInformation;

/**
 * 上报模块用户api熔断处理
 * 
 * @author liuz@aotian.com
 * @date 2018年8月25日 下午5:05:15
 */
public class ReportUserServiceFallback implements IReportUserService {
	private Logger logger = Logger.getLogger(ReportUserServiceFallback.class);
	
	@Override
	public List<ResultDto> insert(List<UserInformation> list) {
		logger.error("call idc insert api fail : data = "+ JSON.toJSONString(list));
		return null;
	}

	@Override
	public List<ResultDto> update(List<UserInformation> list) {
		logger.error("call idc update api fail : data = "+ JSON.toJSONString(list));
		return null;
	}

	@Override
	public List<ResultDto> delete(List<Long> list) {
		logger.error("call idc delete api fail : userIds = " + list);
		return null;
	}

}
