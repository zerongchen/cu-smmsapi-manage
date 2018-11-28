package com.aotain.smmsapi.task.prevalidate.service;

import com.aotain.cu.serviceapi.model.HouseInformation;
import com.aotain.cu.serviceapi.model.UserInformation;
import com.aotain.smmsapi.task.constant.DealFlagConstant;

public interface PreValidateUser {

    /**
     * 提交预审核验子流程-用户
     */
    public void handleValidateUser();

    /**
     * 核验单个用户
     * @param userInformation
     * @return
     */
    public DealFlagConstant.StatusEnum handUserInfo( UserInformation userInformation);
}
