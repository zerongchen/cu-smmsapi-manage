package com.aotain.smmsapi.task.serviceapi.report;

import java.util.List;

import com.aotain.cu.serviceapi.dto.ResultDto;
import com.aotain.cu.serviceapi.model.UserInformation;

import feign.Headers;
import feign.RequestLine;

/**
 * 经营者上报api调用
 * 
 * @author liuz@aotian.com
 * @date 2018年8月25日 下午4:45:17
 */
@Headers({ "Content-Type: application/json", "Accept: application/json" })
public interface IReportUserService {
	public final static String CLIENT_NAME = "myclient";
	
	@RequestLine("POST /serviceapi/report/user/insert")
	public List<ResultDto> insert(List<UserInformation> list);
	
	@RequestLine("POST /serviceapi/report/user/update")
	public List<ResultDto> update(List<UserInformation> list);

	@RequestLine("POST /serviceapi/report/user/delete")
	public List<ResultDto> delete(List<Long> list);
}
