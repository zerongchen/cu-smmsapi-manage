package com.aotain.smmsapi.task.serviceapi;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import com.aotain.cu.auth.AuthQuery;
import com.aotain.cu.auth.ResponseData;

import feign.Headers;
import feign.RequestLine;

/**
 * Demo class
 *
 * @author bang
 * @date 2018/11/20
 */
@Headers({ "Content-Type: application/json", "Accept: application/json" })
@Component
public interface AuthRemoteClient {
    /**
     * 调用认证,获取token
     * @param query
     * @return
     */
    @RequestLine("POST /serviceapi/oauth/token")
    ResponseData auth(@RequestBody AuthQuery query);
}
