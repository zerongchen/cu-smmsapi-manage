package com.aotain.smmsapi.task.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * service api 鉴权拦截器
 *
 * @author bang
 * @date 2018/11/20
 */
public class FeignBasicAuthRequestInterceptor implements RequestInterceptor {

    public FeignBasicAuthRequestInterceptor() {

    }

    @Override
    public void apply(RequestTemplate template) {
        template.header("Authorization", System.getProperty("serviceapi.auth.token"));
    }

}
