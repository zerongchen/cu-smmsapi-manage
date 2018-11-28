package com.aotain.smmsapi.task.serviceapi;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.aotain.cu.auth.AuthQuery;
import com.aotain.cu.auth.ResponseData;

public class AuthRemoteClientFailCallback implements AuthRemoteClient{
	private Logger logger = Logger.getLogger(AuthRemoteClientFailCallback.class);
	
	@Override
	public ResponseData auth(AuthQuery query) {
		logger.error("call idc synDelHouse api fail : data = "+ JSON.toJSONString(query));
		return null;
	}

}
