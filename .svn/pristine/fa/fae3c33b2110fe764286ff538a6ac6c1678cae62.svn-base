package com.aotain.smmsapi.task.serviceapi.report;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.aotain.cu.serviceapi.dto.ResultDto;
import com.aotain.cu.serviceapi.model.IdcInformation;

/**
 * 经营者上报模块熔断处理
 * 
 * @author liuz@aotian.com
 * @date 2018年8月25日 下午4:58:31
 */
public class ReportIdcServiceFallback implements IReportIdcService {
	private Logger logger = Logger.getLogger(ReportIdcServiceFallback.class);
	
	@Override
	public ResultDto insert(IdcInformation idcInfo) {
		logger.error("call idc insert api fail : data = "+ JSON.toJSONString(idcInfo));
		return null;
	}

	@Override
	public ResultDto update(IdcInformation idcInfo) {
		logger.error("call idc update api fail : data = "+ JSON.toJSONString(idcInfo));
		return null;
	}

	@Override
	public ResultDto delete(Integer jyzId) {
		logger.error("call idc delete api fail : jyzId = "+ jyzId);
		return null;
	}

}
