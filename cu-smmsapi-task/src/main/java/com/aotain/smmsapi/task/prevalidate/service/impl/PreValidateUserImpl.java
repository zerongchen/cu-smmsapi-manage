package com.aotain.smmsapi.task.prevalidate.service.impl;

import com.aotain.cu.serviceapi.dto.AjaxValidationResult;
import com.aotain.cu.serviceapi.dto.ResultDto;
import com.aotain.cu.serviceapi.model.*;
import com.aotain.cu.utils.ThreadLocalUtil;
import com.aotain.smmsapi.task.constant.DealFlagConstant;
import com.aotain.smmsapi.task.prevalidate.mapper.PreValidateUserDao;
import com.aotain.smmsapi.task.prevalidate.service.PreValidateUser;
import com.aotain.smmsapi.task.utils.WriteApproveProcess;
import org.apache.curator.shaded.com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PreValidateUserImpl implements PreValidateUser {

    private static Logger logger =  LoggerFactory.getLogger(PreValidateUserImpl.class);

	@Autowired
	private PreValidateUserDao preValidateUserDao;

    private IdcInformation idcInformation;

    private List<HouseInformation> houseBatchConstructs =new ArrayList<HouseInformation>();
    private List<UserInformation> userBatchConstructs =new ArrayList<UserInformation>();

    private Map<String,HouseInformation> HouseHash =null;

	public void setIdcInformation( IdcInformation idcInformation ) {
        this.idcInformation = idcInformation;
    }

	public void setHouseBatchConstructs( List<HouseInformation> list){
		this.houseBatchConstructs.clear();
		houseBatchConstructs.addAll(list);
	}

    public List<UserInformation> getUserBatchConstructs() {
        return userBatchConstructs;
    }

	@Override
	public void handleValidateUser() {
		try {
			UserInformation param = new UserInformation();
			param.setDealFlag(DealFlagConstant.RPT_VARIFY.getDealFlag());
			if (idcInformation != null)
				param.setJyzId(idcInformation.getJyzId());
			List<UserInformation> userInformations = getValidateObj(param);
			if (userInformations != null && !userInformations.isEmpty()) {
				HouseHash = constructHashHouse(houseBatchConstructs);
				for (UserInformation userInformation : userInformations) {
					// 这一部分用户对应的机房将会是上报失败的或者上报成功的,失败的已经走更新流程,接下来处理成功的
					DealFlagConstant.StatusEnum status = handUserInfo(userInformation);
					if (status.getStatus() == DealFlagConstant.StatusEnum.SUCCESS.getStatus()) {
						userBatchConstructs.add(userInformation);
					}
				}
			}
			ThreadLocalUtil.set("userSuccessCnt", userBatchConstructs.size());
			ThreadLocalUtil.set("userTotalCnt", userInformations.size());
		} catch (Exception e) {
			logger.error("handle validate User error ", e);
		}
	}

    public List<UserInformation> getValidateObj( UserInformation userInformation){
        try {
            List<UserInformation> userInformations = preValidateUserDao.getValidateUserList(userInformation);
            return userInformations;
        }catch (Exception e){
            logger.error("get validate User error ",e);
        }
        return null;
    }

    /**
     * 核验用户(成功的不操作，失败，等待的当场返回结果)
     * @param userInformation
     * @return
     */
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	@Override
	public DealFlagConstant.StatusEnum handUserInfo(UserInformation userInformation) {
		DealFlagConstant.StatusEnum resultStatus = DealFlagConstant.StatusEnum.WAITING;
		if (userInformation == null) {
			return resultStatus;
		}
		int userNum;
		UserInformation user = new UserInformation();
		List<HouseInformation> waitinghouse;
		List<HouseInformation> onverrifyHouse;
		switch (userInformation.getOperateType()) {
		// 新增
		case 1:
			logger.info("[DataValidate]The unit name [" + userInformation.getUnitName() + "] newly validate starting......");
			user.setUnitName(userInformation.getUnitName());
			if (userInformation.getIdType() != null) {
				user.setIdType(userInformation.getIdType());
			}
			if (userInformation.getIdNumber() != null) {
				user.setIdNumber(userInformation.getIdNumber());
			}
			userNum = preValidateUserDao.userNum(user);
			if (userNum > 0) {
				// 正式表存在该机房 核验失败 写数据库
				userInformation.setVerificationResult(" 上报表存在单位名称[" + userInformation.getUnitName() + "]信息，新增上报失败");
				userInformation.setDealFlag(DealFlagConstant.RPT_VARIFY_FAIL.getDealFlag());
				handVarifyFail(userInformation);
				resultStatus = DealFlagConstant.StatusEnum.FAIL;
			} else {
				resetFrameUser(userInformation);
				List<HouseInformation> houseInformations = preValidateUserDao.getPreHouse(userInformation);
				if (houseInformations != null && !houseInformations.isEmpty()) {
					waitinghouse = new ArrayList<HouseInformation>();
					onverrifyHouse = new ArrayList<HouseInformation>();
					for (HouseInformation houseInformation : houseInformations) {
						if (!(houseInformation.getDealFlag().intValue() == DealFlagConstant.RPT_SUCCESS.getDealFlag()
						)) {
							//如果是上报审核中
							if (houseInformation.getDealFlag().intValue() == DealFlagConstant.RPT_VARIFY.getDealFlag()){
								onverrifyHouse.add(houseInformation);
							}else {
								//如果不是提交上报或者上报成功的,又不是上报审核中的,等待状态
								waitinghouse.add(houseInformation);
							}
						}
						resultStatus = handleWaiting(userInformation, waitinghouse,onverrifyHouse);
							// 核验用户是否具备上报条件（用户的IP、机架等信息）
						if (resultStatus== DealFlagConstant.StatusEnum.SUCCESS) {
							ResultDto resultDto = userIntegrityVerification(userInformation, houseInformation, houseInformations);
							if (ResultDto.ResultCodeEnum.ERROR.getCode().equals(resultDto.getResultCode())) {
								//用户信息不完整，上报审核不通过
								userInformation.setDealFlag(DealFlagConstant.RPT_VARIFY_FAIL.getDealFlag());
								if (resultDto.getAjaxValidationResult() != null) {
									userInformation.setVerificationResult(resultDto.getAjaxValidationResult().getErrorsToString());
								} else {
									userInformation.setVerificationResult("");
								}
								handVarifyFail(userInformation);
								return DealFlagConstant.StatusEnum.FAIL;
							}
						}
					}

				} else {
					// 用户所在机房为空
					userInformation.setVerificationResult(" 用户[" + userInformation.getUnitName() + "]找不到对应的机房");
					userInformation.setDealFlag(DealFlagConstant.RPT_VARIFY_FAIL.getDealFlag());
					handVarifyFail(userInformation);
					resultStatus = DealFlagConstant.StatusEnum.FAIL;
				}
			}
			logger.info("[DataValidate]The unit name [" + userInformation.getUnitName() + "] newly validate end, status is [" + resultStatus + "]......");
			return resultStatus;
		case 2:
			logger.info("[DataValidate]The unit name [" + userInformation.getUnitName() + "] update validate starting......");
			// 修改
			user.setUserId(userInformation.getUserId());
			userNum = preValidateUserDao.userNum(user);
			if (userNum == 0) {
				// 正式表不存在该用户 核验失败 写数据库
				userInformation.setVerificationResult(" 上报表不存在单位名称[" + userInformation.getUnitName() + "]信息，变更上报失败");
				userInformation.setDealFlag(DealFlagConstant.RPT_VARIFY_FAIL.getDealFlag());
				handVarifyFail(userInformation);
				return DealFlagConstant.StatusEnum.FAIL;
			} else {
				// 提取用户的占用机房信息
				resetFrameUser(userInformation);
				List<HouseInformation> preHouses = preValidateUserDao.getPreHouse(userInformation);
				waitinghouse = new ArrayList<HouseInformation>();
				onverrifyHouse = new ArrayList<HouseInformation>();
				if (!preHouses.isEmpty()) {
					for (HouseInformation houseInformation : preHouses) {
						if (!(houseInformation.getDealFlag().intValue() == DealFlagConstant.RPT_SUCCESS.getDealFlag()
						)) {
							//如果是上报审核中
							if (houseInformation.getDealFlag().intValue() == DealFlagConstant.RPT_VARIFY.getDealFlag()){
								onverrifyHouse.add(houseInformation);
							}else {
								//如果不是提交上报或者上报成功的,又不是上报审核中的,等待状态
								waitinghouse.add(houseInformation);
							}
						}
						resultStatus = handleWaiting(userInformation, waitinghouse,onverrifyHouse);
						if (resultStatus==DealFlagConstant.StatusEnum.SUCCESS){
							// 核验用户是否具备上报条件（用户的IP、机架等信息）
							ResultDto resultDto = userIntegrityVerification(userInformation, houseInformation, preHouses);
							if (ResultDto.ResultCodeEnum.ERROR.getCode().equals(resultDto.getResultCode())) {
								//用户信息不完整，上报审核不通过
								userInformation.setDealFlag(DealFlagConstant.RPT_VARIFY_FAIL.getDealFlag());
								if (resultDto.getAjaxValidationResult() != null) {
									userInformation.setVerificationResult(resultDto.getAjaxValidationResult().getErrorsToString());
								} else {
									userInformation.setVerificationResult("");
								}
								handVarifyFail(userInformation);
								return DealFlagConstant.StatusEnum.FAIL;
							} 
						}
					}

				} else {
					// 用户所在机房为空
					userInformation.setVerificationResult(" 用户[" + userInformation.getUnitName() + "]找不到对应的机房");
					userInformation.setDealFlag(DealFlagConstant.RPT_VARIFY_FAIL.getDealFlag());
					handVarifyFail(userInformation);
					resultStatus = DealFlagConstant.StatusEnum.FAIL;
				}
			}
			logger.info("[DataValidate]The unit name [" + userInformation.getUnitName() + "] update validate end, status is [" + resultStatus + "]......");
			return resultStatus;
		case 3:
			logger.info("[DataValidate]The unit name [" + userInformation.getUnitName() + "] delete validate starting......");
			// 删除
			user.setUserId(userInformation.getUserId());
			userNum = preValidateUserDao.userNum(user);
			if (userNum != 1) {
				// 正式表存在该机房 核验失败 写数据库
				userInformation.setVerificationResult(" [用户名称:"+userInformation.getUnitName()+"]未找到上报信息，删除失败");
				userInformation.setDealFlag(DealFlagConstant.RPT_VARIFY_FAIL.getDealFlag());
				handVarifyFail(userInformation);
				return DealFlagConstant.StatusEnum.FAIL;
			}
			// 提取用户的占用机房信息
			resetFrameUser(userInformation);
			List<HouseInformation> preHouseInformations = preValidateUserDao.getPreHouse(userInformation);
			if (preHouseInformations != null && !preHouseInformations.isEmpty()) {
				waitinghouse = new ArrayList<HouseInformation>();
				onverrifyHouse = new ArrayList<HouseInformation>();

				for (HouseInformation houseInformation : preHouseInformations) {
					// 非上报审核中 或者非上报成功的
					if (!(
							 houseInformation.getDealFlag().intValue() == DealFlagConstant.RPT_SUCCESS.getDealFlag())) {
						//如果是上报审核中
						if (houseInformation.getDealFlag().intValue() == DealFlagConstant.RPT_VARIFY.getDealFlag()){
							onverrifyHouse.add(houseInformation);
						}else {
							//如果不是提交上报或者上报成功的,又不是上报审核中的,等待状态
							waitinghouse.add(houseInformation);
						}
					}
					return handleWaiting(userInformation, waitinghouse,onverrifyHouse);
				}
			} else {
				// 用户所在机房为空
				userInformation.setVerificationResult(" 用户[" + userInformation.getUnitName() + "]找不到对应的机房");
				userInformation.setDealFlag(DealFlagConstant.RPT_VARIFY_FAIL.getDealFlag());
				handVarifyFail(userInformation);
				return DealFlagConstant.StatusEnum.FAIL;
			}
			logger.info("[DataValidate]The unit name [" + userInformation.getUnitName() + "] delete validate end, status is [" + resultStatus + "]......");
			return resultStatus;
		}
		return resultStatus;
	}

    private ResultDto userIntegrityVerification(UserInformation userInformation, HouseInformation houseInformation, List<HouseInformation> houseInformations) {
    	ResultDto result = new ResultDto();
		result.setResultCode(ResultDto.ResultCodeEnum.ERROR.getCode());
		AjaxValidationResult ajaxValidationResult = new AjaxValidationResult();
		if (userInformation == null || houseInformation == null) {
			return result;
		}
		int nature = userInformation.getNature();
		long userId = userInformation.getUserId();
		long houseId = houseInformation.getHouseId();
		int bandwidthCount = preValidateUserDao.integrityVerificationForUserBandwidth(userId, houseId);
		boolean passed = true;
		if (bandwidthCount > 0 && nature == 2) {
			//其他用户没有服务信息，核验用户IP信息
			userInformation.setHouseIDs(houseId + "");
			int userIpCount = preValidateUserDao.integrityVerificationForUserIp(userInformation);
			if (userIpCount == 0) {
				ajaxValidationResult.getErrorsArgsMap().put("user.bandwidth", new String[] {" [用户名称:" + userInformation.getUnitName() + "]缺失[机房名称:"+houseInformation.getHouseName()+"]的IP地址段信息,请联系机房管理员补充后再预审。"});
				passed = false;
			}
		} else {
			if (bandwidthCount == 0) {
				ajaxValidationResult.getErrorsArgsMap().put("user.bandwidth", new String[] {" [用户名称:" + userInformation.getUnitName() + "]缺失[机房名称" + houseInformation.getHouseName() + "]的网络资源信息,请联系机房管理员补充后再预审。"});
				passed = false;
			} else {
				userInformation.setHouseIDs(houseId + "");
				int userIpCount = preValidateUserDao.integrityVerificationForUserIp(userInformation);
				if (userIpCount == 0) {
					ajaxValidationResult.getErrorsArgsMap().put("user.bandwidth", new String[] {" [用户名称:" + userInformation.getUnitName() + "]缺失[机房名称:" + houseInformation.getHouseName() + "]的IP地址段信息，请联系机房管理员补充后再预审。"});
					passed = false;
				}
			}
			if (nature == 1) {
				List<UserServiceInformation> services = userInformation.getServiceList();
				//提供互联网服务的客户必须要有服务信息
				if (services == null || services.size() == 0) {
					ajaxValidationResult.getErrorsArgsMap().put("user.service", new String[] {" 缺失服务信息，请补充后再预审。"});
					passed = false;
				} else {
					boolean existIdcBussiness = false;
					boolean existVirtualSetmode = false;
					for (UserServiceInformation service : services) {
						//存在IDC业务，校验用户的机架信息
						if (service.getBusiness() == 1) {
							existIdcBussiness = true;
						}
						if (service.getSetmode() == 1) {
							existVirtualSetmode = true;
						}
					}
					if (existIdcBussiness && (houseInformation.getIdentity() == 1)) {
						userInformation.setHouseIDs(houseId + "");
						int userFrames = preValidateUserDao.integrityVerificationForUserFrames(userInformation);
						if (userFrames == 0) {
							ajaxValidationResult.getErrorsArgsMap().put("user.frames", new String[] {" [用户名称:" + userInformation.getUnitName() + "]缺失[机房名称:" + houseInformation.getHouseName() + "]的机架信息,请联系机房管理员补充后再预审"});
							passed = false;
						}
					}
					if (existVirtualSetmode) {
						List<UserVirtualInformation> virtuals = userInformation.getVirtualList();
						if (virtuals == null || virtuals.size() == 0) {
							ajaxValidationResult.getErrorsArgsMap().put("user.virtual", new String[] {" [用户名称:" + userInformation.getUnitName() + "]缺失[机房名称:" + houseInformation.getHouseName() + "]的虚拟主机信息,请联系机房管理员补充后再预审"});
							passed = false;
						} else {
							boolean flag = true;
							List<Long> houseIds = Lists.newArrayList();
							for (HouseInformation houseInfo : houseInformations) {
								houseIds.add(houseInfo.getHouseId());
							}
							for (UserVirtualInformation virtual : virtuals) {
								if (houseIds.contains(virtual.getHouseId())) {
									flag = false;
									break;
								}
							}
							if (flag) {
								ajaxValidationResult.getErrorsArgsMap().put("user.virtual", new String[] {" [用户名称:" + userInformation.getUnitName() + "]缺失[机房名称:" + houseInformation.getHouseName() + "]的虚拟主机信息,请联系机房管理员补充后再预审"});
								passed = false;
							}
						}
					}
				}
			} 
		}
		result.setResultCode(passed ? ResultDto.ResultCodeEnum.SUCCESS.getCode() : ResultDto.ResultCodeEnum.ERROR.getCode());
		result.setAjaxValidationResult(ajaxValidationResult);
		return result;
	}

	/**
     * handle 核验不用通过的用户，没有太多逻辑，主要更新核验结果（或者加上deal_flag）
     * @param userInformation
     */
    public void handVarifyFail( UserInformation userInformation) {
        preValidateUserDao.submitUser(userInformation);
		WriteApproveProcess.getInstance().write(userInformation);
    }

    /**
     * 需要等待的用户处理
     * @param userInformation
     * @param waitingHouse 等待
     * @param onverrifyHouse 待核验
     * @return
     */
    public DealFlagConstant.StatusEnum handleWaiting(UserInformation userInformation ,List<HouseInformation> waitingHouse,List<HouseInformation> onverrifyHouse){

		/**
		 * 机房是上报审核中的时候，查看该机房是不是核验通过的
		 */
		if (!onverrifyHouse.isEmpty()){
			for (HouseInformation house:onverrifyHouse){
				if (HouseHash.get(house.getHouseName())==null){
					waitingHouse.add(house);
				}
			}
		}
		/**
		 * 需要等待的机房为空
		 */
        if (waitingHouse.isEmpty()){
			return DealFlagConstant.StatusEnum.SUCCESS;
        }else {
			StringBuilder sb = new StringBuilder();
			for (HouseInformation house : waitingHouse) {
				sb.append(constructErrorMsg(house.getDealFlag(), house.getHouseName())).append("\r\n");
			}
			UserInformation param = new UserInformation();
			param.setUserId(userInformation.getUserId());
			param.setVerificationResult(sb.toString());
			handVarifyFail(param);
			return DealFlagConstant.StatusEnum.WAITING;
		}
    }

	/**
	 * 2）如果机房预审不通过，则提示：用户占用[机房名称:XXXX]预审不通过，请联系机房管理员核验该机房信息并预审成功，系统将自动关联机房上报。
	 3）如果机房未预审，则提示：用户占用[机房名称:XXXX]未预审，请联系机房管理员核验该机房信息并预审成功，系统将自动关联机房上报。
	 6）如果上报失败，则提示：XXXX，请联系系统维护人员处理。
	 * @return
	 */
	private String constructErrorMsg(int dealFlag,String houseName){
		if (dealFlag==DealFlagConstant.PRE_VARIFY_FAIL.getDealFlag()){
			return String.format("用户占用[机房名称:%s]预审不通过，请联系机房管理员核验该机房信息并预审成功，系统将自动关联机房上报。",houseName);
		}else if(dealFlag==DealFlagConstant.UN_PRE_VARIFY.getDealFlag()) {
			return String.format("用户占用[机房名称:%s]未预审，请联系机房管理员核验该机房信息并预审成功，系统将自动关联机房上报。",houseName);
		}else if (dealFlag==DealFlagConstant.RPT_FAIL.getDealFlag()) {
			return String.format("用户占用[机房名称:%s]预审不通过，请联系机房管理员核验该机房信息并预审成功，系统将自动关联机房上报。", houseName);
		}else if (dealFlag==DealFlagConstant.RPT_VARIFY.getDealFlag()){
			return String.format("用户占用[机房名称：%s]上报审核中，请联系机房管理员核验该机房信息并预审成功，系统将自动关联机房上报。", houseName);
		}else if (dealFlag==DealFlagConstant.RPT_VARIFY_FAIL.getDealFlag()){
			return String.format("用户占用[机房名称：%s]上报审核不通过，请联系机房管理员核验该机房信息并预审成功，系统将自动关联机房上报。", houseName);
		}else if (dealFlag==DealFlagConstant.SUB_RPT.getDealFlag()){
			return String.format("用户占用[机房名称：%s]提交上报中，需等待机房上报成功后重新预审成功，系统将自动关联机房上报。", houseName);
		}
		return "";
	}

	/**
	 * 重置用户机架的关系(切确到IDType and IdNumber)
	 * @param userInformation
	 */
	private void resetFrameUser(UserInformation userInformation){
		if (userInformation!=null && userInformation.getUserFrame()!=null && !userInformation.getUserFrame().isEmpty()){
			List<HouseUserFrameInformation> sureDto = new ArrayList<>();
			for (HouseUserFrameInformation userFrameInformation:userInformation.getUserFrame()){
				try {
					if (userFrameInformation.getIdType()==null && userFrameInformation.getIdNumber()==null ){
						sureDto.add(userFrameInformation);
					}else if(userInformation.getIdType().intValue()==userFrameInformation.getIdType().intValue()
							&& userInformation.getIdNumber().equals(userFrameInformation.getIdNumber())){
						sureDto.add(userFrameInformation);
					}
				}catch (Exception e){
					logger.error("",e);
				}
			}
		}
	}

	/**
	 * 将List转成Map, key机房名称,value 机房对象
	 * @param houses
	 * @return
	 */
	private Map<String,HouseInformation> constructHashHouse(List<HouseInformation> houses){
		Map<String,HouseInformation> map = new HashMap<>();
		if (houses==null && houses.isEmpty()) return map;
		for (HouseInformation houseInformation:houses){
			map.put(houseInformation.getHouseName(),houseInformation);
		}
		return map;
	}


}
