package com.aotain.smmsapi.task.serviceapi;

import com.aotain.cu.serviceapi.dto.ResultDto;
import com.aotain.cu.serviceapi.model.WaitApproveProcess;
import feign.Headers;
import feign.RequestLine;

@Headers({ "Content-Type: application/json", "Accept: application/json" })
public interface WriteProcessService {

    @RequestLine("POST /serviceapi/insertApproveProcess")
    public ResultDto write( WaitApproveProcess waitApproveProcess);
}
