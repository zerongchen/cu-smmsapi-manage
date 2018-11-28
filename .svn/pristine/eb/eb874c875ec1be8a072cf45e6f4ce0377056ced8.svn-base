package com.aotain.smmsapi.task.serviceapi;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.aotain.cu.serviceapi.dto.ResultDto;
import com.aotain.cu.serviceapi.model.args.ReqSynDelHouseToPassport;

public class SynAuthroityServiceFailCallback implements SynAuthroityService {
	private Logger logger = Logger.getLogger(SynAuthroityServiceFailCallback.class);
	@Override
	public ResultDto synDelHouse(ReqSynDelHouseToPassport req) {
		logger.error("call idc synDelHouse api fail : data = "+ JSON.toJSONString(req));
		return null;
	}

}
