package com.aotain.smmsapi.task.serviceapi.preinput;

import com.aotain.cu.serviceapi.dto.ResultDto;

import feign.Headers;
import feign.RequestLine;

/**
 * 预录入模块serverapi服务接口
 * 
 * @author liuz@aotian.com
 * @date 2018年8月21日 下午5:21:47
 */
@Headers({ "Content-Type: application/json", "Accept: application/json" })
public interface IPreinputService {
	public final static String CLIENT_NAME = "myclient";
	
	/**
	 * 经营者预审
	 * @param jyzId
	 * @return
	 */
	@RequestLine("POST /serviceapi/pre/idcinfo/preValidate")
	public ResultDto idcApprove(Integer jyzId);
	
	/**
	 * 机房预审
	 * @param houseId
	 * @return
	 */
	@RequestLine("POST /serviceapi/pre/house/approve")
    public ResultDto houseApprove(String houseId);

	/**
	 * 用户预审
	 * @param string
	 * @return
	 */
	@RequestLine("POST /serviceapi/pre/user/approve")
	public ResultDto userApprove(String userId);
}
