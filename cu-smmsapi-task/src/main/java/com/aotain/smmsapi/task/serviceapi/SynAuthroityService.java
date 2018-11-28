package com.aotain.smmsapi.task.serviceapi;

import com.aotain.cu.serviceapi.dto.ResultDto;
import com.aotain.cu.serviceapi.model.args.ReqSynDelHouseToPassport;

import feign.Headers;
import feign.RequestLine;

/**
 * 权限同步接口
 * 
 * @author liuz@aotian.com
 * @date 2018年9月30日 下午6:25:45
 */
@Headers({ "Content-Type: application/json", "Accept: application/json" })
public interface SynAuthroityService {
	/**
	 * 同步删除权限系统中的机房
	 * @param req
	 * @return
	 */
	@RequestLine("POST /serviceapi/authroity/synDelHouse")
	public ResultDto synDelHouse(ReqSynDelHouseToPassport req);
}
