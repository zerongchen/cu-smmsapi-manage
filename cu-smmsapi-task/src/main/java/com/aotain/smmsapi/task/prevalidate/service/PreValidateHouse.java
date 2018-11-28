package com.aotain.smmsapi.task.prevalidate.service;

import com.aotain.cu.serviceapi.model.HouseInformation;
import com.aotain.smmsapi.task.constant.DealFlagConstant;

public interface PreValidateHouse {

    /**
     * 提交预审核验子流程-机房
     */
    public void handleValidateHouse();

    /**
     * 核验单个机房
     * @param houseInformation
     * @return
     */
    public DealFlagConstant.StatusEnum handHouseInfo( HouseInformation houseInformation);
}
