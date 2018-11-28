package com.aotain.smmsapi.task.serviceapi;

import com.alibaba.fastjson.JSON;
import com.aotain.cu.serviceapi.dto.ResultDto;
import com.aotain.cu.serviceapi.model.WaitApproveProcess;
import feign.Headers;
import feign.RequestLine;
import org.apache.log4j.Logger;

public class WriteProcessServiceCallback implements WriteProcessService{

    private Logger logger = Logger.getLogger(SynAuthroityServiceFailCallback.class);

    @Override
    public ResultDto write( WaitApproveProcess waitApproveProcess ) {
        logger.error("write approve process api fail : data = "+ JSON.toJSONString(waitApproveProcess));
        return null;
    }
}
