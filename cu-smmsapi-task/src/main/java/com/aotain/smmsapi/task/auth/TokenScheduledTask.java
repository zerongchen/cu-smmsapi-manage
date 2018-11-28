package com.aotain.smmsapi.task.auth;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.aotain.cu.auth.AuthQuery;
import com.aotain.cu.auth.ResponseData;
import com.aotain.cu.serviceapi.utils.BaseFeignBuilder;
import com.aotain.smmsapi.task.serviceapi.AuthRemoteClient;
import com.aotain.smmsapi.task.serviceapi.AuthRemoteClientFailCallback;

/**
 * 用于定期获取token
 *
 * @author bang
 * @date 2018/11/20
 */
@Component
//@Service(value = "tokenScheduledTask")
public class TokenScheduledTask {
    private static Logger logger = LoggerFactory.getLogger(TokenScheduledTask.class);

    public final static long HALF_HOUR = 1000 * 60 *30 ;

    /**
     * 刷新Token
     */
    @Scheduled(fixedDelay = HALF_HOUR)
    public void reloadApiToken() {
        String token = this.getToken();
        while (StringUtils.isBlank(token)) {
            try {
                Thread.sleep(1000);
                token = this.getToken();
            } catch (InterruptedException e) {
                logger.error("", e);
            }
        }
        System.setProperty("serviceapi.auth.token", token);
    }

    public String getToken() {
        AuthQuery query = new AuthQuery();
        query.setAccessKey("admin123");
        query.setSecretKey("1qaz@WSX");
        AuthRemoteClient authRemoteClient =  BaseFeignBuilder.getCacheFeign(AuthRemoteClient.class, "myclient",
				new AuthRemoteClientFailCallback(), BaseFeignBuilder.EncodeDecodeType.EDT_JSON_JSON,null);
        ResponseData response = authRemoteClient.auth(query);
        return response.getData() == null ? "" : response.getData().toString();
    }
}
