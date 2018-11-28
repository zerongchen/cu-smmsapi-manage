package com.aotain.smmsapi.task.serviceapi.report;

import com.aotain.cu.serviceapi.dto.ResultDto;
import com.aotain.cu.serviceapi.model.IdcInformation;

import feign.Headers;
import feign.RequestLine;

/**
 * 经营者上报api调用
 * 
 * @author liuz@aotian.com
 * @date 2018年8月25日 下午4:45:17
 */
@Headers({ "Content-Type: application/json", "Accept: application/json" })
public interface IReportIdcService {
	public final static String CLIENT_NAME = "myclient";
	
	@RequestLine("POST /serviceapi/report/idc/insert")
	public ResultDto insert(IdcInformation idcInfo);
	
	@RequestLine("POST /serviceapi/report/idc/update")
	public ResultDto update(IdcInformation idcInfo);

	@RequestLine("POST /serviceapi/report/idc/delete")
	public ResultDto delete(Integer jyzId);
}
