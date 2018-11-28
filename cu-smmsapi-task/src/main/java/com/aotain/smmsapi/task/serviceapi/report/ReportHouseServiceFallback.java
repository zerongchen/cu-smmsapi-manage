package com.aotain.smmsapi.task.serviceapi.report;

import java.util.List;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.aotain.cu.serviceapi.dto.ResultDto;
import com.aotain.cu.serviceapi.model.HouseInformation;

/**
 * 上报模块机房api熔断处理
 * 
 * @author liuz@aotian.com
 * @date 2018年8月25日 下午5:02:02
 */
public class ReportHouseServiceFallback implements IReportHouseService {
	private Logger logger = Logger.getLogger(ReportHouseServiceFallback.class);
	
	@Override
	public List<ResultDto> insert(List<HouseInformation> list) {
		logger.error("call idc insert api fail : data = "+ JSON.toJSONString(list));
		return null;
	}

	@Override
	public List<ResultDto> update(List<HouseInformation> list) {
		logger.error("call idc update api fail : data = "+ JSON.toJSONString(list));
		return null;
	}

	@Override
	public List<ResultDto> delete(List<Integer> list) {
		logger.error("call idc delete api fail : houseIds = "+ list);
		return null;
	}

}
