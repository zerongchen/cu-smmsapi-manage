package com.aotain.smmsapi.task.serviceapi.preinput;

import org.apache.log4j.Logger;

import com.aotain.cu.serviceapi.dto.ResultDto;

/**
 * IPreinputService熔断处理
 * 
 * @author liuz@aotian.com
 * @date 2018年8月21日 下午5:49:21
 */
public class PreinputServiceFallback implements IPreinputService {
	private Logger logger = Logger.getLogger(PreinputServiceFallback.class);
	
	@Override
	public ResultDto houseApprove(String houseId) {
		logger.error("call houseApprove api fail : houseId="+ houseId);
		return null;
	}

	@Override
	public ResultDto idcApprove(Integer jyzId) {
		logger.error("call idcApprove api fail : jyzId="+ jyzId);
		return null;
	}

	@Override
	public ResultDto userApprove(String userId) {
		logger.error("call userApprove api fail : userId="+ userId);
		return null;
	}

}
