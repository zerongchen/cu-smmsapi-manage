package com.aotain.smmsapi.task.utils;

import com.aotain.common.config.ContextUtil;
import com.aotain.common.utils.redis.DataApproveUtil;
import com.aotain.cu.serviceapi.model.*;
import com.aotain.smmsapi.task.smmsreturn.mapper.SubmitReportFileDao;

import java.util.Date;

public class WriteApproveProcess {

    public static Integer APPROVE_TYPE_HOUSE=2;
    public static Integer APPROVE_TYPE_IDC=1;
    public static Integer APPROVE_TYPE_USER=3;
    private SubmitReportFileDao api;
    private static volatile WriteApproveProcess instance = null;


    private WriteApproveProcess(){
        api = ContextUtil.getContext().getBean(SubmitReportFileDao.class);

    }

    public static WriteApproveProcess getInstance(){
        if (instance==null){
            synchronized (WriteApproveProcess.class){
                instance = new WriteApproveProcess();
            }
        }
        return instance;
    }

    public void writeProcess( WaitApproveProcess waitApproveProcess ) {
        Long approveId = DataApproveUtil.getInstance().getDataApprove(waitApproveProcess.getType()+"_"+waitApproveProcess.getDataId());
        if (approveId!=null){
            waitApproveProcess.setApproveId(approveId);
            if (api.getApproveProcess(waitApproveProcess)>0){
                api.updateApproveProcess(waitApproveProcess);
            }else {
                api.insertApproveProcess(waitApproveProcess);
            }
        }
    }

    public void write( BaseModel baseModel ) {
        Date date = new Date();
        WaitApproveProcess process = new WaitApproveProcess();
        if(baseModel.getDealFlag()==null){
            return;
        }
        if (baseModel.getSubmitId()!=null){
            process.setSubmitId(baseModel.getSubmitId());
        }
        if (!StringUtil.isEmptyString(baseModel.getVerificationResult())){
            process.setWarnData(baseModel.getVerificationResult());
        }
        if (baseModel.getCreateUserId()!=null){
            process.setCreateUser(Long.valueOf(baseModel.getCreateUserId()));
        }
        process.setDealFlag(baseModel.getDealFlag());
        process.setDealTime(DateUtils.getCurrentyyyMMddHHmmss());
        process.setCreateTime(date);
        process.setUpdateTime(date);

        if (baseModel instanceof IdcInformation){
            IdcInformation idcInformation= (IdcInformation)baseModel;
            process.setType(APPROVE_TYPE_IDC);
            process.setDataId(Long.valueOf(idcInformation.getJyzId()));
        }else if (baseModel instanceof HouseInformation){
            HouseInformation houseInformation= (HouseInformation)baseModel;
            process.setType(APPROVE_TYPE_HOUSE);
            process.setDataId(Long.valueOf(houseInformation.getHouseId()));
        }else if(baseModel instanceof UserInformation){
            UserInformation userInformation= (UserInformation)baseModel;
            process.setType(APPROVE_TYPE_USER);
            process.setDataId(Long.valueOf(userInformation.getUserId()));
        }
        writeProcess(process);
    }

}
