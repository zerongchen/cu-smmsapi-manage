package com.aotain.smmsapi.task.smmsreturn.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.aotain.common.config.ContextUtil;
import com.aotain.common.config.redis.BaseRedisService;
import com.aotain.common.utils.model.report.SmmsResultCache;
import com.aotain.common.utils.redis.DataApproveUtil;
import com.aotain.common.utils.redis.DataSubmitUtil;
import com.aotain.cu.serviceapi.dto.ResultDto;
import com.aotain.cu.serviceapi.model.HouseFrameInformation;
import com.aotain.cu.serviceapi.model.HouseGatewayInformation;
import com.aotain.cu.serviceapi.model.HouseIPSegmentInformation;
import com.aotain.cu.serviceapi.model.HouseInformation;
import com.aotain.cu.serviceapi.model.IdcInformation;
import com.aotain.cu.serviceapi.model.UserBandwidthInformation;
import com.aotain.cu.serviceapi.model.UserInformation;
import com.aotain.cu.serviceapi.model.UserServiceInformation;
import com.aotain.cu.serviceapi.model.UserVirtualInformation;
import com.aotain.cu.serviceapi.model.WaitApproveProcess;
import com.aotain.cu.serviceapi.model.args.ReqSynDelHouseToPassport;
import com.aotain.cu.serviceapi.utils.BaseFeignBuilder;
import com.aotain.cu.serviceapi.utils.BaseFeignBuilder.EncodeDecodeType;
import com.aotain.cu.utils.StringUtil;
import com.aotain.smmsapi.task.QuartzMain;
import com.aotain.smmsapi.task.bean.IsmsWaitSubmitReportFile;
import com.aotain.smmsapi.task.bean.OperatorCache;
import com.aotain.smmsapi.task.bean.SynchConfig;
import com.aotain.smmsapi.task.constant.DealFlagConstant;
import com.aotain.smmsapi.task.interceptor.FeignBasicAuthRequestInterceptor;
import com.aotain.smmsapi.task.serviceapi.SynAuthroityService;
import com.aotain.smmsapi.task.serviceapi.SynAuthroityServiceFailCallback;
import com.aotain.smmsapi.task.serviceapi.preinput.IPreinputService;
import com.aotain.smmsapi.task.serviceapi.preinput.PreinputServiceFallback;
import com.aotain.smmsapi.task.serviceapi.report.IReportHouseService;
import com.aotain.smmsapi.task.serviceapi.report.IReportIdcService;
import com.aotain.smmsapi.task.serviceapi.report.IReportUserService;
import com.aotain.smmsapi.task.serviceapi.report.ReportHouseServiceFallback;
import com.aotain.smmsapi.task.serviceapi.report.ReportIdcServiceFallback;
import com.aotain.smmsapi.task.serviceapi.report.ReportUserServiceFallback;
import com.aotain.smmsapi.task.smmsreturn.mapper.OperatorCacheDao;
import com.aotain.smmsapi.task.smmsreturn.mapper.OperatorStatusDao;
import com.aotain.smmsapi.task.smmsreturn.mapper.OperatorTablesDao;
import com.aotain.smmsapi.task.smmsreturn.mapper.SubmitReportFileDao;
import com.aotain.smmsapi.task.smmsreturn.mapper.WaitBaseInforDao;
import com.aotain.smmsapi.task.smmsreturn.service.SmmsReturnService;
import com.aotain.smmsapi.task.utils.DateUtils;

/**
 * 管局999目录返回之后，需要将待上报表更新到上报表并清理操作表、等待表数据状态
 * 
 * @author liuz@aotian.com
 * @date 2018年8月15日 下午5:38:58
 */
@Service
public class SmmsReturnServiceImpl implements SmmsReturnService {
	private Logger logger = Logger.getLogger(SmmsReturnServiceImpl.class);
	private static final String RIBBON_CLIENT_NAME = "myclient";

	@Autowired
	private SubmitReportFileDao submitReportFileDao;

	@Autowired
	private OperatorCacheDao operatorCacheDao;

	@Autowired
	private OperatorStatusDao operatorStatusDao;

	@Autowired
	private OperatorTablesDao operatorTablesDao;

	private BaseRedisService<String, String, String> rediscluster;

	@Autowired
	private WaitBaseInforDao waitBaseInforDao;
	
	// 记录当前正在处理的文件
	private static List<SmmsResultCache> DOING_LIST = new ArrayList<SmmsResultCache>();
	
	// redis中一个文件处理结果的缓存机制如下：
	// 1. 最多处理10次
	// 2. 满了10次重试,清除缓存
	// 3. 处理的1-6次，至少5分钟以后执行重试；7-9次，1小时后执行下次重试
	private static final String SMMS_999_FILE_RESULT_KEY = "smms_999_file_result";

	@Override
	public void writeRedisCache(long taskId, String fileName, int status, int fileType) {
		// 只有基础数据上报需要执行后续流程
		// 1-基础数据记录类型；2-基础数据监测异常记录数据类型；3-访问日志查询记录数据类型；
		// 4-违法信息监测记录数据类型；5-违法信息过滤记录数据类型；6-基础数据查询指令数据类型；
		// 7-ISMS活动状态数据类型；8-活跃资源监测记录数据类型；9-违法违规网站监测记录类型
		if (fileType != 1) {
			logger.debug("is not basic data xml - taskId=" + taskId + ",fileName=" + fileName + ",status=" + status
					+ ",type=" + fileType);
			return;
		}
		if (rediscluster == null) {
			rediscluster = ContextUtil.getContext().getBean("baseRedisServiceImpl", BaseRedisService.class);
		}
		SmmsResultCache cache = new SmmsResultCache();
		cache.setTaskId(taskId);
		cache.setFileName(fileName);
		cache.setFileType(fileType);
		cache.setStatus(status);
		try {
			String cacheStr = JSON.toJSONString(cache);
			rediscluster.putHash(SMMS_999_FILE_RESULT_KEY, fileName, cacheStr);
			// SmmsResultCache 对象入库，供文件重处理使用
			submitReportFileDao.updateReturnInfor(fileName+".xml",cacheStr); // 初始化为dealTimes=0
		} catch (Exception e) {
			logger.error("write 999 return result to redis cache fail  - taskId=" + taskId + ",fileName=" + fileName
					+ ",status=" + status + ",type=" + fileType, e);
			doProcess(taskId, fileName, status, fileType); // 失败时，直接调用处理流程，否则异步调用
		}
	}

	@Override
	public void redisCacheDoProcess() {
		if (rediscluster == null) {
			rediscluster = ContextUtil.getContext().getBean("baseRedisServiceImpl", BaseRedisService.class);
		}
		Map<String, String> datas = rediscluster.getHashs(SMMS_999_FILE_RESULT_KEY);
		if (datas == null || datas.isEmpty()) {
			logger.debug("no file cache info found");
			return;
		}
		for (String key : datas.keySet()) {
			String value = datas.get(key);
			if (StringUtils.isNotBlank(value)) {
				SmmsResultCache cache = JSON.parseObject(value, SmmsResultCache.class);
				if(DOING_LIST.contains(cache)){ // 检查是否正在处理，正在处理的跳过
					continue;
				}
				logger.info("start deal return file : fileName=" + key+",detail : "+value);
				DOING_LIST.add(cache); 
				// 扫描到后，立马开始执行
				doProcess(cache.getTaskId(), cache.getFileName(), cache.getStatus(), cache.getFileType());
				// 处理完成数据库处理次数加1,删除redis缓存
				cache.setLastDealTime(System.currentTimeMillis()/1000);
				cache.setDealTimes(cache.getDealTimes()+1);
				submitReportFileDao.updateReturnInfor(cache.getFileName()+".xml", JSON.toJSONString(cache));
				rediscluster.removeHash(SMMS_999_FILE_RESULT_KEY, cache.getFileName());
				DOING_LIST.remove(cache); // 清除处理中的对象
				logger.info("finish deal return file : fileName=" + key);
			} else {
				logger.warn("miss value on redis of  return file : fileName=" + key+",detail : "+value);
			}
		}
	}

	@Override
	public void doProcess(long taskId, String fileName, int status, int fileType) {
		try {
			// 1. 此文件在WAIT_ISMS_SUBMIT_REPORTFILE表中是否存在
			// 不存在时，不执行后续处理
			// 存在时，获取提交记录ID，经营者ID、机房ID,用户ID
			IsmsWaitSubmitReportFile rfile = getReportFileInfo(fileName + ".xml");
			if (rfile == null) {
				logger.warn("no sumit record  - taskId=" + taskId + ",fileName=" + fileName + ",status=" + status);
				String content = rediscluster.getHash(SMMS_999_FILE_RESULT_KEY, fileName);
				if (StringUtils.isNotBlank(content)) {
					SmmsResultCache cache = JSON.parseObject(content, SmmsResultCache.class);
					cache.setDealTimes(cache.getDealTimes() + 1);
					cache.setLastDealTime(System.currentTimeMillis() / 1000);
					// 抛出未知异常，即当成文件处理流程失败，更新redis缓存
					rediscluster.putHash(SMMS_999_FILE_RESULT_KEY, fileName, JSON.toJSONString(cache));
				}
				return;
			}
			if (rediscluster == null) {
				rediscluster = ContextUtil.getContext().getBean("baseRedisServiceImpl", BaseRedisService.class);
			}
			logger.info(
					" start deal smms return info - taskId=" + taskId + ",fileName=" + fileName + ",status=" + status);
			DealFlagConstant flag = DealFlagConstant.RPT_SUCCESS;
			String warn = "管局接收成功";
			// 2. 管局返回文件处理成功，调用成功处理流程
			if (status == 0) {
				doSuccessProcess(taskId, fileName, rfile);
			}
			// 3. 管局返回文件处理失败，调用失败处理流程
			else {
				doFailProcess(taskId, fileName, status, rfile);
				flag = DealFlagConstant.RPT_FAIL;
				warn = "管局返回失败编码："+status;
			}
			writeApproveProcess(rfile,warn,flag);
			
			// 4. 检查整个提交批次的文件是否都已处理完成
			checkSubmitFiles(rfile.getSubmitId());
			logger.info(" complete deal smms return info - taskId=" + taskId + ",fileName=" + fileName + ",status="
					+ status);

			// 正常情况下处理完成后，删除redis缓存
			String content = rediscluster.getHash(SMMS_999_FILE_RESULT_KEY, fileName);
			if (StringUtils.isNotBlank(content)) {
				rediscluster.removeHash(SMMS_999_FILE_RESULT_KEY, fileName);
			}
		} catch (Throwable e) {
			logger.error("deal smms return info exception - taskId=" + taskId + ",fileName=" + fileName + ",status="
					+ status, e);
			String content = rediscluster.getHash(SMMS_999_FILE_RESULT_KEY, fileName);
			if (StringUtils.isNotBlank(content)) {
				SmmsResultCache cache = JSON.parseObject(content, SmmsResultCache.class);
				cache.setDealTimes(cache.getDealTimes() + 1);
				cache.setLastDealTime(System.currentTimeMillis() / 1000);
				// 抛出未知异常，即当成文件处理流程失败，更新redis缓存
				rediscluster.putHash(SMMS_999_FILE_RESULT_KEY, fileName, JSON.toJSONString(cache));
			}
		}
	}

	/**
	 * 统计检查submitId对应的所有文件，在所有文件都处理完成的情况下，更新
	 * 
	 * @param submitId
	 */
	private void checkSubmitFiles(Long submitId) {
		// 1. 查询文件总数
		int noDealFileCount = submitReportFileDao.countSubmitFile(submitId, -1);
		int initCount = submitReportFileDao.countSubmitFile(submitId, 0); // 处于初始状态的文件数量
		// 存在文件，且文件都已经处理完成（999已返回且后续流程执行完成）
		if (noDealFileCount > 0 && initCount == 0) {
			// 2. 查询失败文件总数
			int failCount = submitReportFileDao.countSubmitFile(submitId, 1);
			if (failCount > 0) {
				int cnt = submitReportFileDao.updateSubmitLogStatus(submitId, 2); // 处理上报失败
				if (cnt == 0) {
					logger.warn("update submit log dealFlag fail : submitId=" + submitId + ",dealFlag=" + 2);
				}
			} else {
				int cnt = submitReportFileDao.updateSubmitLogStatus(submitId, 3); // 处理上报成功
				if (cnt == 0) {
					logger.warn("update submit log dealFlag fail : submitId=" + submitId + ",dealFlag=" + 3);
				}
			}
		}
	}
	
	private List<Long> split2Long(String value, String spchar) {
		List<String> list = StringUtil.split(value, spchar);
		List<Long> llist = new ArrayList<Long>();
		for (String l : list) {
			llist.add(Long.parseLong(l));
		}
		return llist;
	}
	
	private void writeApproveProcess(IsmsWaitSubmitReportFile winfo, String warn, DealFlagConstant flag) {
		try {
			List<Long> idcs = split2Long(winfo.getJyzId(), ",");
			List<Long> houses = split2Long(winfo.getHouseId(), ",");
			List<Long> users = split2Long(winfo.getUserId(), ",");
			writeApproveProcessIdc(idcs, warn, flag);
			writeApproveProcessHouse(houses, warn, flag);
			writeApproveProcessUser(users, warn, flag);
		} catch (Exception e) {
			logger.error("writeApproveProcess exception ", e);
		}
	}
	
	private void writeApproveProcessIdc(List<Long> dataIds,String warn,DealFlagConstant flag) {
		if(dataIds != null){
			dataIds.stream().forEach(dataId -> writeApproveProcess(1,dataId,warn,flag));
		}
	}
	
	private void writeApproveProcessHouse(List<Long> dataIds,String warn,DealFlagConstant flag) {
		if(dataIds != null){
			dataIds.stream().forEach(dataId -> writeApproveProcess(2,dataId,warn,flag));
		}
	}
	
	private void writeApproveProcessUser(List<Long> dataIds,String warn,DealFlagConstant flag) {
		if(dataIds != null){
			dataIds.stream().forEach(dataId -> writeApproveProcess(3,dataId,warn,flag));
		}
	}
	
	private void writeApproveProcess(int type,long dataId,String warn,DealFlagConstant flag) {
		try {
			WaitApproveProcess wap = new WaitApproveProcess();
			String format = "%d_%d";
			wap.setApproveId(DataApproveUtil.getInstance().getDataApprove(String.format(format, type, dataId)));
			wap.setDataId(dataId);
			wap.setCreateTime(new Date());
			wap.setCreateUser(-999L);
			wap.setSubmitId(DataSubmitUtil.getInstance().getDataSubmit(String.format(format, type, dataId)));
			wap.setType(type); // 都为1：基础数据
			wap.setUpdateTime(new Date());
			wap.setWarnData(warn == null ? "" : warn);
			wap.setDealFlag(flag.getDealFlag());
			wap.setDealTime(StringUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
			submitReportFileDao.insertApproveProcess(wap);
		} catch (Exception e) {
			logger.error("writeApproveProcess exception ", e);
		}
	}

	/**
	 * 管局返回失败的处理流程
	 * 
	 * @param taskId
	 * @param fileName
	 * @param status
	 * @param rfile
	 */
	private void doFailProcess(long taskId, String fileName, int status, IsmsWaitSubmitReportFile rfile) {
		// 1. 调用缓存处理提交预审函数
		refreshCache(rfile);
		// 2. 更新文件上报状态为失败
		int cnt = submitReportFileDao.updateDealFlag(rfile.getReportFileName(), 1); // 文件上报失败
		if (cnt == 0) {
			logger.warn("update submitRpoertFile dealFlag fail - no submit file record found : fileName="
					+ rfile.getReportFileName());
		}
		// 3. 将操作表的记录改成上报失败
		// 3.1 idc_isms_base_idc.deal_flag改成失败
		UpdateResult ur = new UpdateResult();
		ur.appendError("管局返回失败：returnCode="+status);
		
		String error = ur.toString();
		// 经营者新增
		List<IdcInformation> idcList = waitBaseInforDao.getIdcInformation(rfile.getSubmitId(),
				rfile.getJyzId());
		if(idcList != null && idcList.size() > 0){
			if (jyzHasModify(idcList.get(0))) {
				cnt = operatorStatusDao.updateJyzDealFlag(rfile.getJyzId(), 6,error);	// 上报失败
				if (cnt == 0) {
					logger.warn("update jyz.dealFlag fail - no jyz record found : fileName=" + rfile.getReportFileName()
							+ ",jyzId=" + rfile.getJyzId());
				}
			}
		}
		// 3.2 idc_isms_base_house.DEAL_FLAG改成上报失败
		if (StringUtils.isNotBlank(rfile.getHouseId())) {
			cnt = operatorStatusDao.updateHouseDealFlag(rfile.getHouseId(), 6,error);
			if (cnt == 0) {
				logger.warn("update house.dealFlag fail - no house record found : fileName=" + rfile.getReportFileName()
						+ ",houseId=" + rfile.getHouseId());
			}
		}
		// 3.3 idc_isms_base_user.DEAL_FLAG改成上报失败
		if (StringUtils.isNotBlank(rfile.getUserId())) {
			cnt = operatorStatusDao.updateUserDealFlag(rfile.getUserId(), 6,error);
			if (cnt == 0) {
				logger.warn("update user.dealFlag fail - no user record found : fileName=" + rfile.getReportFileName()
						+ ",userId=" + rfile.getUserId());
			}
		}
	}

	/**
	 * 判断是否为经营者上报（若只有jyzId不为空，或者jzyId,userId,houseId三者都不为空时，判定为经营者上报）
	 * 
	 * @param rfile
	 * @return
	 */
	@SuppressWarnings("unused")
	private boolean isJyzReport(IsmsWaitSubmitReportFile rfile) {
		if (!StringUtils.isBlank(rfile.getJyzId())) {
			if (StringUtils.isBlank(rfile.getHouseId()) && StringUtils.isBlank(rfile.getUserId())) {
				return true;
			}
			if (StringUtils.isNotBlank(rfile.getUserId()) && StringUtils.isNotBlank(rfile.getUserId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 管局返回成功的处理流程
	 * 
	 * @param taskId
	 * @param fileName
	 * @param rfile
	 */
	private void doSuccessProcess(long taskId, String fileName, IsmsWaitSubmitReportFile rfile) {
		logger.info("start doSuccessProcess : taskId=" + taskId + ",fileName=" + fileName);
		// 1. 新增、更新流程，调用缓存处理提交预审函数
		if (rfile.getReportType() == 1 || rfile.getReportType() == 2) { // 1新增；2变更；3删除
			logger.info("doSuccessProcess - refreshCache ： taskId=" + taskId + ",fileName=" + fileName);
			refreshCache(rfile);
		} else { // 删除流程，直接删除所有缓存记录
			logger.info("doSuccessProcess - deleteCache ： taskId=" + taskId + ",fileName=" + fileName);
			deleteCache(rfile);
		}
		logger.info("doSuccessProcess - doUpadateProcess ： taskId=" + taskId + ",fileName=" + fileName);
		// 2. 执行更新流程
		UpdateResult uresult = doUpadateProcess(rfile);
		logger.info("doSuccessProcess - updateDealFlag ： taskId=" + taskId + ",fileName=" + fileName);
		int fileStatus = 2; // 上报成功
//		int status = 5; // 上报成功
//		String message = null;
		// 判断调用api是否失败，失败需要些异常提示
		if(!uresult.isSuccess()){
			fileStatus = 1; // 上报失败
//			status = 6;	// 上报失败
//			message = uresult.toString();
		}
		// 3. 将操作表的记录改成上报成功(如果调用api出错)
		int cnt = submitReportFileDao.updateDealFlag(rfile.getReportFileName(), fileStatus); // 文件上报成功
		if (cnt == 0) {
			logger.warn("update submitRpoertFile dealFlag fail - no submit file record found : fileName="
					+ rfile.getReportFileName());
		}

/*		// 3.1 idc_isms_base_idc.deal_flag改成成功
		List<IdcInformation> idcList = waitBaseInforDao.getIdcInformation(rfile.getSubmitId(),
				rfile.getJyzId());
		if(idcList != null && idcList.size() > 0){
			if (jyzHasModify(idcList.get(0))) {
				cnt = operatorStatusDao.updateJyzDealFlag(rfile.getJyzId(), status,message);	// 上报成功
				if (cnt == 0) {
					logger.warn("update jyz.dealFlag fail - no jyz record found : fileName=" + rfile.getReportFileName()
							+ ",jyzId=" + rfile.getJyzId());
				}
			}
		}
		// 3.2 idc_isms_base_house.DEAL_FLAG改成上报成功
		if (StringUtils.isNotBlank(rfile.getHouseId())) {
			cnt = operatorStatusDao.updateHouseDealFlag(rfile.getHouseId(), status,message);
			if (cnt == 0) {
				logger.warn("update house.dealFlag fail - no house record found : fileName=" + rfile.getReportFileName()
						+ ",houseId=" + rfile.getHouseId());
			}
		}
		// 3.3 idc_isms_base_user.DEAL_FLAG改成上报成功
		if (StringUtils.isNotBlank(rfile.getUserId())) {
			cnt = operatorStatusDao.updateUserDealFlag(rfile.getUserId(), status,message);
			if (cnt == 0) {
				logger.warn("update user.dealFlag fail - no user record found : fileName=" + rfile.getReportFileName()
						+ ",userId=" + rfile.getUserId());
			}
		}*/
		logger.info("finish doSuccessProcess : taskId=" + taskId + ",fileName=" + fileName);
	}

	/**
	 * 处理更新流程
	 * 
	 * @param rfile
	 * @return 
	 */
	private UpdateResult doUpadateProcess(IsmsWaitSubmitReportFile rfile) {
		IReportIdcService idcApi = BaseFeignBuilder.getCacheFeign(IReportIdcService.class, RIBBON_CLIENT_NAME,
				new ReportIdcServiceFallback(), BaseFeignBuilder.EncodeDecodeType.EDT_JSON_JSON,new FeignBasicAuthRequestInterceptor());
		IReportHouseService houseApi = BaseFeignBuilder.getCacheFeign(IReportHouseService.class, RIBBON_CLIENT_NAME,
				new ReportHouseServiceFallback(), BaseFeignBuilder.EncodeDecodeType.EDT_JSON_JSON,new FeignBasicAuthRequestInterceptor());
		IReportUserService userApi = BaseFeignBuilder.getCacheFeign(IReportUserService.class, RIBBON_CLIENT_NAME,
				new ReportUserServiceFallback(), BaseFeignBuilder.EncodeDecodeType.EDT_JSON_JSON,new FeignBasicAuthRequestInterceptor());
		UpdateResult uresult = new UpdateResult();
		final int reportType = rfile.getReportType();
		// 新增流程
		if (reportType == 1) {
			if (!StringUtils.isBlank(rfile.getJyzId())) {
				// 经营者新增
				List<IdcInformation> idcList = waitBaseInforDao.getIdcInformation(rfile.getSubmitId(),
						rfile.getJyzId());
				logger.info("start insert idc information ... ：idcSize=" + idcList == null ? 0 : idcList.size());
				if (idcList != null && idcList.size() > 0) {
					for (IdcInformation idc : idcList) {
						if (!jyzHasModify(idc)) { // 因用户或机房上报而写的jyz信息不用新增
							continue;
						}
						logger.info("start insert idc information ... ：" + JSON.toJSONString(idc));
						ResultDto result = idcApi.insert(idc);
						int status = 6; // 6上报失败，5上报成功
						String message = "新增经营者["+idc.getIdcName()+"]失败";
						UpdateResult cresult = new UpdateResult();
						if (result != null) {
							if (result.getResultCode() != 0) {
								cresult.appendError(message);
								logger.error("call api fail - api return error : resultCode=" + result.getResultCode()
										+ ",statusCode=" + result.getStatusCode() + ",resultMsg="
										+ result.getResultMsg());
							} else {
								message = "新增经营者["+idc.getIdcName()+"]成功";
								cresult.appendWarn(message);
								status = 5;
							}
						}else{
							cresult.appendError(message+" - API异常");
							logger.error("call api fail - api return empty : idc="+idc.getIdcId());
						}
						uresult.append(cresult);
						int cnt = operatorStatusDao.updateJyzDealFlag(String.valueOf(idc.getJyzId()), status,cresult.toString());	// 上报成功或者失败
						if (cnt == 0) {
							logger.warn("update jyz.dealFlag fail - no jyz record found : fileName=" + rfile.getReportFileName()
									+ ",jyzId=" + idc.getJyzId());
						}
						logger.info("finished insert idc information, result is " + JSON.toJSONString(idc));
					}
				}
			}
			if (!StringUtils.isBlank(rfile.getHouseId())) {
				List<HouseInformation> houseList = waitBaseInforDao.getHouseInformation(rfile.getSubmitId(),
						rfile.getHouseId());
				logger.info("start insert house information ... ：houseSize" + houseList == null ? 0 : houseList.size());
				if (houseList != null && houseList.size() > 0) {
					// 过滤掉不是新增的机房相关子节点
					houseList.forEach(list -> cleanHouseByOperatorType(list,reportType));
					
					logger.info("start insert house information ... ：" + JSON.toJSONString(houseList));
					List<ResultDto> resultList = houseApi.insert(houseList);
					if (resultList != null && resultList.size() > 0) {
						int index = 0;
						for (ResultDto result : resultList) {
							HouseInformation hinfor = houseList.get(index);
							int status = 6; // 6上报失败，5上报成功
							String message = "机房信息新增["+hinfor.getHouseId()+"-"+hinfor.getHouseName()+"]失败";
							UpdateResult cresult = new UpdateResult();
							if (result != null) {
								if (result.getResultCode() != 0) {
									cresult.appendError(message);
									logger.error("call api fail - api return error : resultCode="
											+ result.getResultCode() + ",statusCode=" + result.getStatusCode()
											+ ",resultMsg=" + result.getResultMsg());
								}else{
									message = "机房信息新增["+hinfor.getHouseId()+"-"+hinfor.getHouseName()+"]成功";
									status = 5;
									cresult.appendWarn(message);
								}
							}else{
								cresult.appendError(message+" - API异常");
								logger.error("call api fail - api return empty : houseId="+hinfor.getHouseId());
							}
							// 更新子节点信息
							doUpdateHouseChildrenStatus(hinfor, result,cresult);
							index++;
							uresult.append(cresult);
							
							// 更新主体信息
							int cnt = operatorStatusDao.updateHouseDealFlag(String.valueOf(hinfor.getHouseId()), status,cresult.toString());
							if (cnt == 0) {
								logger.warn("update house.dealFlag fail - no house record found : fileName=" + rfile.getReportFileName()
										+ ",houseId=" + hinfor.getHouseId());
							}
						}
					}
					logger.info("finished insert house information, result is " + JSON.toJSONString(houseList));
				}
			}
			if (!StringUtils.isBlank(rfile.getUserId())) {
				// 用户新增
				List<UserInformation> userList = waitBaseInforDao.getUserInformation(rfile.getSubmitId(),
						rfile.getUserId());
				logger.info("start insert user information ... ：userSize" + userList == null ? 0 : userList.size());
				if (userList != null && userList.size() > 0) {
					// 过滤掉不是新增的机房相关子节点
					userList.forEach(list -> cleanUserByOperatorType(list,reportType));
					
					logger.info("start insert user information ... ：" + JSON.toJSONString(userList));
					List<ResultDto> resultList = userApi.insert(userList);
					if (resultList != null && resultList.size() > 0) {
						int index = 0;
						for (ResultDto result : resultList) {
							UserInformation userInfor = userList.get(index);
							int status = 6; // 6上报失败，5上报成功
							String message = "用户信息新增["+userInfor.getUserId()+"-"+userInfor.getUnitName()+"]失败";
							UpdateResult cresult = new UpdateResult();
							if (result != null) {
								if (result.getResultCode() != 0) {
									cresult.appendError(message);
									logger.error("call api fail - api return error : resultCode="
											+ result.getResultCode() + ",statusCode=" + result.getStatusCode()
											+ ",resultMsg=" + result.getResultMsg());
								}else{
									message = "用户信息新增["+userInfor.getUserId()+"-"+userInfor.getUnitName()+"]成功";
									status = 5;
									cresult.appendWarn(message);
								}
							}else{
								cresult.appendError(message+" - API异常");
								logger.error("call api fail - api return error : userId="+userInfor.getUserId());
							}
							// 更新子节点
							doUpdateUserChildrenStatus(userInfor, result,cresult);
							index++;
							uresult.append(cresult);
							// 更新用户主体
							int cnt = operatorStatusDao.updateUserDealFlag(String.valueOf(userInfor.getUserId()), status,cresult.toString());
							if (cnt == 0) {
								logger.warn("update user.dealFlag fail - no user record found : fileName=" + rfile.getReportFileName()
										+ ",userId=" + userInfor.getUserId());
							}
						}
					}
					logger.info("finished insert house information : " + JSON.toJSONString(userList));
				}
			}
		}
		// 变更流程
		else if (reportType == 2) {
			if (!StringUtils.isBlank(rfile.getJyzId())) {
				// 经营者修改
				List<IdcInformation> idcList = waitBaseInforDao.getIdcInformation(rfile.getSubmitId(),
						rfile.getJyzId());
				logger.info("start update idc information ... ：idcSize=" + idcList == null ? 0 : idcList.size());
				if (idcList != null && idcList.size() > 0) {
					for (IdcInformation idc : idcList) {
						if (!jyzHasModify(idc)) { // 因用户或机房上报而写的jyz信息不用更新
							continue;
						}
						int status = 6; // 6上报失败，5上报成功
						String message = "修改经营者["+idc.getIdcName()+"]失败";
						UpdateResult cresult = new UpdateResult();
						logger.info("start update idc information ... ：" + JSON.toJSONString(idc));
						ResultDto result = idcApi.update(idc);
						if (result != null) {
							if (result.getResultCode() != 0) {
								cresult.appendError(message);
								logger.error("call api fail - api return error : resultCode=" + result.getResultCode()
										+ ",statusCode=" + result.getStatusCode() + ",resultMsg="
										+ result.getResultMsg());
							}else{
								message = "修改经营者["+idc.getIdcName()+"]成功";
								status = 5;
								cresult.appendWarn(message);
							}
						}else{
							cresult.appendError(message+" - API异常");
							logger.error("call api fail - api return error : idcId="+idc.getIdcId());
						}
						uresult.append(cresult);
						int cnt = operatorStatusDao.updateJyzDealFlag(String.valueOf(idc.getJyzId()), status,cresult.toString());	// 上报成功或者失败
						if (cnt == 0) {
							logger.warn("update jyz.dealFlag fail - no jyz record found : fileName=" + rfile.getReportFileName()
									+ ",jyzId=" + idc.getJyzId());
						}
						logger.info("finished update idc information, result is " + JSON.toJSONString(idc));
					}
				}
			}
			if (!StringUtils.isBlank(rfile.getHouseId())) {
				// 机房修改
				List<HouseInformation> houseList = waitBaseInforDao.getHouseInformation(rfile.getSubmitId(),
						rfile.getHouseId());
				logger.info("start update house information ... ：houseSize" + houseList == null ? 0 : houseList.size());
				if(houseList != null ){
					// 过滤掉不是新增的机房相关子节点
					houseList.forEach(list -> cleanHouseByOperatorType(list,reportType));
				}
				houseUpdate(houseList,uresult,houseApi);
			}
			if (!StringUtils.isBlank(rfile.getUserId())) {
				// 用户修改
				List<UserInformation> userList = waitBaseInforDao.getUserInformation(rfile.getSubmitId(),
						rfile.getUserId());
				logger.info("start update user information ... ：userSize" + userList == null ? 0 : userList.size());
				if(userList != null ){
					// 过滤掉不是新增的机房相关子节点
					userList.forEach(list -> cleanUserByOperatorType(list,reportType));
				}
				userUpdate(userList, uresult,userApi);
			}
		}// 删除流程
		else if (reportType == 3) {
			if (!StringUtils.isBlank(rfile.getJyzId())) {
				// 经营者删除
				List<IdcInformation> idcList = waitBaseInforDao.getIdcInformation(rfile.getSubmitId(),
						rfile.getJyzId());
				logger.info("start delete idc information ... ：idcSize=" + idcList == null ? 0 : idcList.size());
				if (idcList != null && idcList.size() > 0) {
					for (IdcInformation idc : idcList) {
						if (idc.getOperateType() != 3) { // 必须等待表为删除，才能删除经营者
							continue;
						}
						if (!jyzHasModify(idc)) { // 因用户或机房上报而写的jyz信息不用删除
							continue;
						}
						int status = 6;
						String message = "删除经营者["+idc.getIdcName()+"]失败";
						UpdateResult cresult = new UpdateResult();
						// 删除上报表
						if (idc.getJyzId() != null && idc.getJyzId() > 0) {
							logger.info("start delete idc information ... ：" + JSON.toJSONString(idc));
							ResultDto result = idcApi.delete(idc.getJyzId());
							if (result != null) {
								if (result.getResultCode() != 0) {
									cresult.appendError(message);
									logger.error("call api fail - api return error : resultCode="
											+ result.getResultCode() + ",statusCode=" + result.getStatusCode()
											+ ",resultMsg=" + result.getResultMsg());
								}else{
									message = "删除经营者["+idc.getIdcName()+"]成功";
									status = 5;
									cresult.appendWarn(message);
									
									// 删除操作表
									logger.info("start delete idc information of report db(clear all basic data) ... ："
											+ JSON.toJSONString(idc));
									operatorTablesDao.deleteJyz(idc.getJyzId().longValue());
									try{
										// 删除成功时，需要更新同步删除权限系统的机房信息
										SynAuthroityService synService = BaseFeignBuilder.getCacheFeign(SynAuthroityService.class, RIBBON_CLIENT_NAME,
												new SynAuthroityServiceFailCallback(), BaseFeignBuilder.EncodeDecodeType.EDT_JSON_JSON,new FeignBasicAuthRequestInterceptor());
										// 构造删除机房请求
										if(QuartzMain.SYNCH_CONFIG != null){
											SynchConfig sc = QuartzMain.SYNCH_CONFIG;
											ReqSynDelHouseToPassport req = new ReqSynDelHouseToPassport();
											req.setAppId(sc.getAppId());
											req.setHouseId(0L); // 删除所有机房
											req.setSynDelUrl(sc.getPreurl()+sc.getDp_deleteUrl());
											req.setSynQueryUrl(sc.getPreurl()+sc.getDp_queryUrl());
											logger.info("tring to delete house of passport ："+req.toString());
											ResultDto rs = synService.synDelHouse(req);
											if (rs.getResultCode() != 0) {
												logger.error("tring to delete house of passport fail - resultCode="
														+ rs.getResultCode() + ",message=" + rs.getResultMsg() + " : "
														+ req.toString());
											} else {
												message = "机房信息删除[所有机房]成功";
												cresult.appendWarn(message);
												status = 5;
												logger.info("tring to delete house of passport success : "
														+ req.toString());
											}
										}else{
											cresult.appendError(message+" - 删除权限系统中的机房失败");
											logger.error("tring to delete house of passport fail - no passport config information found");
										}
									}catch(Exception e){
										logger.error("tring to delete house of passport fail - call service api exception",e);
									}
									uresult.appendWarn("删除操作经营者["+idc.getIdcName()+"]所有数据完成");
								}
							}else{
								cresult.appendError(message+" - API异常");
								logger.error("call api fail - api return error : idcId="+idc.getJyzId());
							}
							// 更新主节点状态信息（删除时，所有数据都会清空，此处更新数量必然为0）
							int cnt = operatorStatusDao.updateJyzDealFlag(rfile.getJyzId(), status,cresult.toString());	// 上报成功或者失败
							if (cnt != 0) {
								logger.warn("update jyz.dealFlag fail - no jyz record found : fileName=" + rfile.getReportFileName()
										+ ",jyzId=" + rfile.getJyzId());
							}
							logger.info("finished delete idc information : " + idc.getJyzId());
						}
						logger.info("start delete idc information of report db(clear all basic data) ... ："
								+ JSON.toJSONString(idc));
					}
				}
			}
			if (!StringUtils.isBlank(rfile.getHouseId())) {
				// 机房删除
				List<HouseInformation> houseListAll = waitBaseInforDao.getHouseInformation(rfile.getSubmitId(),
						rfile.getHouseId());
				// 执行更新操作
				List<HouseInformation> houseListUpdate = houseListAll.stream().filter(h -> h.getOperateType() == 2).collect(Collectors.toList());
				// 子节点的删除需要调用更新接口
				if(houseListUpdate != null && houseListUpdate.size() > 0){
					// 此处查询出所有需要执行删除的子接点；并清空机房名称，防止serviceapi对上报表主体进行重复更新
					houseListUpdate.forEach(list -> {cleanHouseByOperatorType(list,3); list.setHouseName(null);});
					houseUpdate(houseListUpdate, uresult, houseApi);
				}
				// 执行删除操作
				List<HouseInformation> houseList = houseListAll.stream().filter(h -> h.getOperateType() == 3).collect(Collectors.toList());
				if (houseList != null && houseList.size() > 0) {
					// 过滤掉不是新增的机房相关子节点
					houseList.forEach(list -> cleanHouseByOperatorType(list,reportType));
					
					logger.info(
							"start delete house information ... ：houseSize" + houseList == null ? 0 : houseList.size());
					List<Integer> houseIds = houseList.stream().map(house -> house.getHouseId().intValue())
							.collect(Collectors.toList());
					if (houseIds != null && houseIds.size() > 0) {
						logger.info("start delete house information ... : houseIds = " + houseIds);
						List<ResultDto> resultList = houseApi.delete(houseIds);
						if (resultList != null && resultList.size() > 0) {
							int index = 0;
							for (ResultDto result : resultList) {
								HouseInformation hinfor = houseList.get(index);
								int status = 6;
								String message = "机房信息删除[" + hinfor.getHouseId() + "-" + hinfor.getHouseName() + "]失败";
								UpdateResult cresult = new UpdateResult();
								
								if (result != null) {
									if (result.getResultCode() != 0) {
										cresult.appendError(message);
										logger.error("call api fail - api return error : resultCode="
												+ result.getResultCode() + ",statusCode=" + result.getStatusCode()
												+ ",resultMsg=" + result.getResultMsg());
									} else {
										
										try{
											// 删除成功时，需要更新同步删除权限系统的机房信息
											SynAuthroityService synService = BaseFeignBuilder.getCacheFeign(SynAuthroityService.class, RIBBON_CLIENT_NAME,
													new SynAuthroityServiceFailCallback(), BaseFeignBuilder.EncodeDecodeType.EDT_JSON_JSON,new FeignBasicAuthRequestInterceptor());
											// 构造删除机房请求
											if(QuartzMain.SYNCH_CONFIG != null){
												SynchConfig sc = QuartzMain.SYNCH_CONFIG;
												ReqSynDelHouseToPassport req = new ReqSynDelHouseToPassport();
												req.setAppId(sc.getAppId());
												req.setHouseId(hinfor.getHouseId());
												req.setSynDelUrl(sc.getPreurl()+sc.getDp_deleteUrl());
												req.setSynQueryUrl(sc.getPreurl()+sc.getDp_queryUrl());
												logger.info("tring to delete house of passport ："+req.toString());
												ResultDto rs = synService.synDelHouse(req);
												if (rs.getResultCode() != 0) {
													logger.error("tring to delete house of passport fail - resultCode="
															+ rs.getResultCode() + ",message=" + rs.getResultMsg() + " : "
															+ req.toString());
												} else {
													message = "机房信息删除[" + hinfor.getHouseId() + "-" + hinfor.getHouseName() + "]成功";
													cresult.appendWarn(message);
													status = 5;
													logger.info("tring to delete house of passport success : "
															+ req.toString());
												}
											}else{
												cresult.appendError(message+" - 删除权限系统中的机房失败");
												logger.error("tring to delete house of passport fail - no passport config information found");
											}
										}catch(Exception e){
											logger.error("tring to delete house of passport fail - call service api exception",e);
										}
									}
								}else{
									cresult.appendError(message+" - API异常");
									logger.error("call api fail - api return error : houseId="+hinfor.getHouseId());
								}
								// 更新子节点信息
								doUpdateHouseChildrenStatus(hinfor, result, cresult);
								index++;
								uresult.append(cresult);
								// 更新主体信息
								int cnt = operatorStatusDao.updateHouseDealFlag(String.valueOf(hinfor.getHouseId()), status,cresult.toString());
								if (cnt == 0) {
									logger.warn("update house.dealFlag fail - no house record found : fileName="+rfile.getReportFileName()
											+ ",houseId=" + String.valueOf(hinfor.getHouseId()));
								}
							}
						}
						// 删除操作表
						logger.info("delete house informations from operator tables : houseIds="+houseIds);
						operatorTablesDao.deleteHouses(houseIds);
						logger.info("finished delete house information : houseIds = " + houseIds);
					}
				}
			}
			if (!StringUtils.isBlank(rfile.getUserId())) {
				// 用户删除
				List<UserInformation> userListAll = waitBaseInforDao.getUserInformation(rfile.getSubmitId(),
						rfile.getUserId());
				
				// 执行更新操作
				List<UserInformation> userListUpdate = userListAll.stream().filter(u -> u.getOperateType() == 2).collect(Collectors.toList());
				if(userListUpdate != null && userListUpdate.size() > 0){
					// 此处查询出所有需要执行删除的子接点；并清空用户名称，防止serviceapi对上报表主体进行重复更新
					userListUpdate.forEach(list -> {cleanUserByOperatorType(list,3); list.setUnitName(null);});
					userUpdate(userListUpdate, uresult, userApi); 
				}
				// 执行删除操作
				List<UserInformation> userList= userListAll.stream().filter(u -> u.getOperateType() == 3).collect(Collectors.toList());
				if(userList != null && userList.size() > 0){
					// 过滤掉不是新增的机房相关子节点
					userList.forEach(list -> cleanUserByOperatorType(list,reportType));
					
					logger.info("start delete user information ... ：userSize" + userList == null ? 0 : userList.size());
					List<Long> userIds = userList.stream().map(user -> user.getUserId()).collect(Collectors.toList());
					if (userIds != null && userIds.size() > 0) {
						logger.info("start delete user information ... : userIds = " + userIds);
						List<ResultDto> resultList = userApi.delete(userIds);
						if (resultList != null && resultList.size() > 0) {
							int index = 0;
							for (ResultDto result : resultList) {
								UserInformation userInfor = userList.get(index);
								int status = 6;
								String message = "用户信息删除[" + userInfor.getUserId() + "-"+ userInfor.getUnitName() + "]失败";
								UpdateResult cresult = new UpdateResult();
								if (result != null) {
									if (result.getResultCode() != 0) {
										cresult.appendError(message);
										logger.error("call api fail - api return error : resultCode="
												+ result.getResultCode() + ",statusCode=" + result.getStatusCode()
												+ ",resultMsg=" + result.getResultMsg());
									} else {
										status = 5;
										message = "用户信息删除[" + userInfor.getUserId() + "-"+ userInfor.getUnitName() + "]成功";
										cresult.appendWarn(message);
									}
								}else{
									cresult.appendError(message+" - API异常");
									logger.error("call api fail - api return error : userId="+userInfor.getUserId());
								}
								doUpdateUserChildrenStatus(userInfor, result, cresult);
								index++;
								uresult.append(cresult);
								// 更新用户主体
								int cnt = operatorStatusDao.updateUserDealFlag(String.valueOf(userInfor.getUserId()), status,cresult.toString());
								if (cnt == 0) {
									logger.warn("update user.dealFlag fail - no user record found : userId=" + userInfor.getUserId());
								}
							}
						}
						logger.info("finished delete user information : userIds = " + userIds);

						// 删除操作表
						operatorTablesDao.deleteUsers(userIds);
					}
				}
			}
		}
		return uresult;
	}
	
	/**
	 * 用户更新
	 * @param userList
	 * @param uresult
	 * @param userApi
	 */
	private void userUpdate(List<UserInformation> userList,UpdateResult uresult, IReportUserService userApi) {
		if (userList != null && userList.size() > 0) {
			logger.info("start update user information ... ：" + JSON.toJSONString(userList));
			List<ResultDto> resultList = userApi.update(userList);
			if (resultList != null && resultList.size() > 0) {
				int index = 0;
				for (ResultDto result : resultList) {
					UserInformation userInfor = userList.get(index);
					int status = 6;
					String message = "用户信息修改["+userInfor.getUserId()+"-"+userInfor.getUnitName()+"]失败";
					UpdateResult cresult = new UpdateResult();
					
					if (result != null) {
						if (result.getResultCode() != 0) {
							cresult.appendError(message);
							logger.error("call api fail - api return error : resultCode="
									+ result.getResultCode() + ",statusCode=" + result.getStatusCode()
									+ ",resultMsg=" + result.getResultMsg());
						}else{
							status = 5;
							message = "用户信息修改["+userInfor.getUserId()+"-"+userInfor.getUnitName()+"]成功";
							cresult.appendWarn(message);
						}
					}else{
						cresult.appendError(message+" - API异常");
						logger.error("call api fail - api return error : userId="+userInfor.getUserId());
					}
					doUpdateUserChildrenStatus(userInfor, result,cresult);
					index++;
					
					uresult.append(cresult);
					// 更新用户主体
					int cnt = operatorStatusDao.updateUserDealFlag(String.valueOf(userInfor.getUserId()), status,cresult.toString());
					if (cnt == 0) {
						logger.warn("update user.dealFlag fail - no user record found : userId=" + userInfor.getUserId());
					}
				}
			}
			logger.info("finished update house information : " + JSON.toJSONString(userList));
		}
	}
	
	/**
	 * 更新机房信息
	 * @param houseList
	 * @param uresult
	 * @param houseApi
	 */
	private void houseUpdate(List<HouseInformation> houseList,UpdateResult uresult, IReportHouseService houseApi){
		if (houseList != null && houseList.size() > 0) {
			
			logger.info("start update house information ... ：" + JSON.toJSONString(houseList));
			List<ResultDto> resultList = houseApi.update(houseList);
			if (resultList != null && resultList.size() > 0) {
				int index = 0;

				for (ResultDto result : resultList) {
					HouseInformation hinfor = houseList.get(index);
					int status = 6;
					String message = "机房信息修改["+hinfor.getHouseId()+"-"+hinfor.getHouseName()+"]失败";
					UpdateResult cresult = new UpdateResult();
					if (result != null) {
						if (result.getResultCode() != 0) {
							cresult.appendError(message);
							logger.error("call api fail - api return error : resultCode="
									+ result.getResultCode() + ",statusCode=" + result.getStatusCode()
									+ ",resultMsg=" + result.getResultMsg());
						}else{
							status = 5;
							message = "机房信息修改["+hinfor.getHouseId()+"-"+hinfor.getHouseName()+"]成功";
							cresult.appendWarn(message);
						}
					}else{
						cresult.appendError(message+" - API异常");
						logger.error("call api fail - api return error : "+hinfor.getHouseId());
					}
					// 更新子节点信息
					doUpdateHouseChildrenStatus(hinfor, result,cresult);
					index++;
					
					uresult.append(cresult);
					// 更新主体信息
					int cnt = operatorStatusDao.updateHouseDealFlag(String.valueOf(hinfor.getHouseId()), status,cresult.toString());
					if (cnt == 0) {
						logger.warn("update house.dealFlag fail - no house record found : "
								+ "houseId=" + String.valueOf(hinfor.getHouseId()));
					}
				}
			}
			logger.info("finished update house information : " + JSON.toJSONString(houseList));
		}
	}

	/**
	 * 判断是否是否时经营者信息需要更新
	 * 
	 * @param idc
	 * @return
	 */
	private boolean jyzHasModify(IdcInformation idc) {
		if (idc == null) {
			return false;
		}
		if (idc.getJyzId() == null) {
			return false;
		}
		if (StringUtils.isBlank(idc.getIdcName())) {
			return false;
		}
		return true;
	}

	public static void main(String[] args) {
		List<String> list = new ArrayList<>();
		list.add("one");
		list.add("two");
		list.add("three");
		List<String> strList = list.stream().filter(a -> !a.equals("two")).collect(Collectors.toList());
		System.out.print(StringUtils.join(strList, ","));
	}

	/**
	 * 更新机房下的子节点状态
	 * 
	 * @param hinfor
	 * @param result
	 */
	private void doUpdateHouseChildrenStatus(HouseInformation hinfor, ResultDto result,UpdateResult uresult) {
		int status = 0; // 0-未上报；1-已上报
		if (result != null && result.getResultCode() == 0) {
			status = 1;
		}
		List<HouseIPSegmentInformation> ipSegList = hinfor.getIpSegList();
		if (ipSegList != null) {
			List<Long> ipsegIdUpdateList = ipSegList.stream().map(a -> a.getOperateType() != 3 ? a.getIpSegId() : null)
					.filter(a -> a != null).collect(Collectors.toList());
			int update = operatorStatusDao.updateHouseIpsegDealFlag(ipsegIdUpdateList, status);
			logger.info("update house ipseg deal_flag : houseId = " + hinfor.getHouseId() + ",dealFlag=" + status
					+ ",updateCount=" + update);
//			uresult.appendWarn("更新IP段状态[houseId="+hinfor.getHouseId()+",status="+status+",updateCount="+update+",ipSegList="+StringUtils.join(ipsegIdUpdateList,",")+"]");

			if (status == 1) {
				List<Long> ipsegIdDeleteList = ipSegList.stream()
						.map(a -> a.getOperateType() == 3 ? a.getIpSegId() : null).filter(a -> a != null)
						.collect(Collectors.toList());
				int delete = operatorStatusDao.deleteHouseIpsegByIds(StringUtils.join(ipsegIdDeleteList, ","));
//				uresult.appendWarn("删除操作表IP段[houseId="+hinfor.getHouseId()+",deleteCount="+delete+",ipsegIdDeleteList="+StringUtils.join(ipsegIdUpdateList,",")+"]");
				logger.info("delete house ipseg deal_flag : houseId = " + hinfor.getHouseId() + ",dealFlag=" + status
						+ ",deleteCount=" + delete);
			}
		}
		List<HouseFrameInformation> frameList = hinfor.getFrameList();
		if (frameList != null) {
			List<Long> frameIdUpdateList = frameList.stream().map(a -> a.getOperateType() != 3 ? a.getFrameId() : null)
					.filter(a -> a != null).collect(Collectors.toList());
			int update = operatorStatusDao.updateHouseFrameDealFlag(frameIdUpdateList, status);
			logger.info("update house frame deal_flag : houseId = " + hinfor.getHouseId() + ",dealFlag=" + status
					+ ",updateCount=" + update);
//			uresult.appendWarn("更新机架状态[houseId="+hinfor.getHouseId()+",status="+status+",updateCount="+update+",frameList="+StringUtils.join(frameList,",")+"]");
			if (status == 1) {
				List<Long> frameIdDeleteList = frameList.stream()
						.map(a -> a.getOperateType() == 3 ? a.getFrameId() : null).filter(a -> a != null)
						.collect(Collectors.toList());
				int delete = operatorStatusDao.deleteHouseFrameByIds(StringUtils.join(frameIdDeleteList, ","));
//				uresult.appendWarn("删除操作表机架[houseId="+hinfor.getHouseId()+",deleteCount="+delete+",frameIdDeleteList="+StringUtils.join(frameIdDeleteList,",")+"]");
				logger.info("delete house frame deal_flag : houseId = " + hinfor.getHouseId() + ",dealFlag=" + status
						+ ",deleteCount=" + delete);
			}
		}
		List<HouseGatewayInformation> gatewayInfoList = hinfor.getGatewayInfoList();
		if (gatewayInfoList != null) {
			List<Long> gatewayUpdateIdList = gatewayInfoList.stream()
					.map(a -> a.getOperateType() != 3 ? a.getGatewayId() : null).filter(a -> a != null)
					.collect(Collectors.toList());
			int update = operatorStatusDao.updateHouseGatewayDealFlag(gatewayUpdateIdList, status);
			logger.info("update house gateway deal_flag : houseId = " + hinfor.getHouseId() + ",dealFlag=" + status
					+ ",updateCount=" + update);
//			uresult.appendWarn("更新链路状态[houseId="+hinfor.getHouseId()+",updateCount="+update+",status="+status+",gatewayUpdateIdList="+StringUtils.join(gatewayUpdateIdList,",")+"]");
			
			if (status == 1) {
				List<Long> gatewayDeleteIdList = gatewayInfoList.stream()
						.map(a -> a.getOperateType() == 3 ? a.getGatewayId() : null).filter(a -> a != null)
						.collect(Collectors.toList());
				int delete = operatorStatusDao.deleteHouseLinkByIds(StringUtils.join(gatewayDeleteIdList, ","));
//				uresult.appendWarn("删除操作表链路[houseId="+hinfor.getHouseId()+",deleteCount="+delete+",gatewayDeleteIdList="+StringUtils.join(gatewayDeleteIdList,",")+"]");
				logger.info("delete house gateway deal_flag : houseId = " + hinfor.getHouseId() + ",dealFlag=" + status
						+ ",deleteCount=" + delete);
			}
		}
	}

	/**
	 * 更新用户下的子节点状态
	 * 
	 * @param userInfor
	 * @param result
	 */
	private void doUpdateUserChildrenStatus(UserInformation userInfor, ResultDto result,UpdateResult uresult) {
		int status = 0; // 0-未上报；1-已上报
		if (result != null && result.getResultCode() == 0) {
			status = 1;
		}
		List<UserServiceInformation> serviceList = userInfor.getServiceList();
		if (serviceList != null) {
			List<Long> serviceIdList = serviceList.stream().map(a -> a.getOperateType() != 3 ? a.getServiceId() : null)
					.filter(a -> a != null).collect(Collectors.toList());
			
			int cnt = operatorStatusDao.updateUserServiceDealFlag(serviceIdList, status);
			logger.info("update user service deal_flag : userId = " + userInfor.getUserId() + ",dealFlag=" + status
					+ ",updateCount=" + cnt);
//			uresult.appendWarn("更新用户服务状态[userId="+userInfor.getUserId()+",updateCount="+cnt+",status="+status+",serviceIdList="+StringUtils.join(serviceIdList,",")+"]");
			List<Long> domainIdList= serviceList.stream().map(a -> a.getOperateType() != 3 ? a : null).filter(a -> a != null)
					.flatMap(a -> a.getDomainList().stream()).map(a -> a.getDomainId()).filter(a -> a != null)
					.collect(Collectors.toList());
			// 更新服务域名状态
			operatorStatusDao.updateUserServiceDomainDealFlag(domainIdList, status);
			
			if (status == 1) {
				List<Long> serviceDeleteIdList = serviceList.stream()
						.map(a -> a.getOperateType() == 3 ? a.getServiceId() : null).filter(a -> a != null)
						.collect(Collectors.toList());
				int delete = operatorStatusDao.deleteUserServiceByIds(StringUtils.join(serviceDeleteIdList, ","));
//				uresult.appendWarn("删除操作表用户服务[houseId="+userInfor.getUserId()+",deleteCount="+delete+",serviceDeleteIdList="+StringUtils.join(serviceDeleteIdList,",")+"]");
				logger.info("delete user service deal_flag : userId = " + userInfor.getUserId() + ",dealFlag=" + status
						+ ",deleteCount=" + delete);
			}
		}
		List<UserBandwidthInformation> bandwidthList = userInfor.getBandwidthList();
		if (bandwidthList != null) {
			List<Long> hhIdList = bandwidthList.stream().map(a -> a.getOperateType() != 3 ? a.getHhId() : null)
					.filter(a -> a != null).collect(Collectors.toList());
			int cnt = operatorStatusDao.updateUserHHDealFlag(hhIdList, status);
			logger.info("update user bandwidth deal_flag : userId = " + userInfor.getUserId() + ",dealFlag=" + status
					+ ",updateCount=" + cnt);
//			uresult.appendWarn("更新用户带宽状态[userId="+userInfor.getUserId()+",updateCount="+cnt+",status="+status+",hhIdList="+StringUtils.join(hhIdList,",")+"]");
			if (status == 1) {
				List<Long> bandWidthDeleteIdList = bandwidthList.stream()
						.map(a -> a.getOperateType() == 3 ? a.getHhId() : null).filter(a -> a != null)
						.collect(Collectors.toList());
				int delete = operatorStatusDao.deleteBandWidthByIds(StringUtils.join(bandWidthDeleteIdList, ","));
//				uresult.appendWarn("删除操作表用户带宽[houseId="+userInfor.getUserId()+",deleteCount="+delete+",bandWidthDeleteIdList="+StringUtils.join(bandWidthDeleteIdList,",")+"]");
				logger.info("delete user bandwidth deal_flag : userId = " + userInfor.getUserId() + ",dealFlag="
						+ status + ",deleteCount=" + delete);
			}
		}
		List<UserVirtualInformation> virtualList = userInfor.getVirtualList();
		if (virtualList != null) {
			List<Long> virtualIdList = virtualList.stream().map(a -> a.getOperateType() != 3 ? a.getVirtualId() : null)
					.filter(a -> a != null).collect(Collectors.toList());
			int cnt = operatorStatusDao.updateUserVirtualDealFlag(virtualIdList, status);
//			uresult.appendWarn("更新虚拟机[userId="+userInfor.getUserId()+",updateCount="+cnt+",status="+status+",virtualIdList="+StringUtils.join(virtualIdList,",")+"]");
			logger.info("update user virtual deal_flag : userId = " + userInfor.getUserId() + ",dealFlag=" + status
					+ ",updateCount=" + cnt);

			if (status == 1) {
				List<Long> virtualDeleteIdList = virtualList.stream()
						.map(a -> a.getOperateType() == 3 ? a.getVirtualId() : null).filter(a -> a != null)
						.collect(Collectors.toList());
				int delete = operatorStatusDao.deletUserVirtualByIds(StringUtils.join(virtualDeleteIdList, ","));
//				uresult.appendWarn("删除操作表虚拟机[houseId="+userInfor.getUserId()+",deleteCount="+delete+",virtualIdList="+StringUtils.join(virtualIdList,",")+"]");
				logger.info("delete user virtual deal_flag : userId = " + userInfor.getUserId() + ",dealFlag=" + status
						+ ",deleteCount=" + delete);
			}
		}
	}

	/**
	 * 清除缓存
	 * 
	 * @param rfile
	 */
	private void deleteCache(IsmsWaitSubmitReportFile rfile) {
		try {
			// 1. 查询缓存记录表记录(与rfile中的记录取交集)
			List<OperatorCache> oclist = operatorCacheDao.getOperatorCache(rfile.getJyzId(), rfile.getHouseId(),
					rfile.getUserId());
			if (oclist == null || oclist.isEmpty()) {
				// 无交集，不用重新调用预审模块
				return;
			}
			List<Long> tlist = parseHouseId(oclist);
			// 机房：删除缓存记录表中的交集部分记录
			int cn = operatorCacheDao.deleteOperatorCacheByHouseId(tlist);
			logger.info("delete " + cn + " operator cache by houseId : " + tlist);
			tlist = parseUserId(oclist);
			// 用户：除缓存记录表中的交集部分记录
			cn = operatorCacheDao.deleteOperatorCacheByUserId(tlist);
			logger.info("delete " + cn + " operator cache by userId : " + tlist);
		} catch (Throwable e) {
			logger.warn("delete oprator cache found exception", e);
		}
	}

	/**
	 * 缓存刷新
	 * 
	 * @param rfile
	 */
	private void refreshCache(IsmsWaitSubmitReportFile rfile) {
		try {
			// 1. 查询缓存记录表记录(与rfile中的记录取交集)
			List<OperatorCache> oclist = operatorCacheDao.getOperatorCache(rfile.getJyzId(), rfile.getHouseId(),
					rfile.getUserId());
			if (oclist == null || oclist.isEmpty()) {
				// 无交集，不用重新调用预审模块
				return;
			}
			IPreinputService ipService = BaseFeignBuilder.getCacheFeign(IPreinputService.class,
					IPreinputService.CLIENT_NAME, new PreinputServiceFallback(), EncodeDecodeType.EDT_PLAIN_JSON,new FeignBasicAuthRequestInterceptor());

			// 经营者自己不会写缓存表，因此此时不做自动预审
			/*
			 * // 经营者:交集部分调用预审API List<Long> tlist = parseJyzId(oclist);
			 * for(Long jyzId : tlist){ ResultDto rs =
			 * ipService.idcApprove(jyzId.intValue()); if(rs == null ||
			 * rs.getResultCode() == ResultDto.ResultCodeEnum.ERROR.getCode()){
			 * logger.error("idcinfo approve fail : " + rs); } } //
			 * 经营者：除缓存记录表中的交集部分记录 int cn =
			 * operatorCacheDao.deleteOperatorCacheByJyzId(tlist);
			 * logger.info("delete " + cn + " operator cache by jyzId : " +
			 * tlist);
			 */

			// 机房：交集部分调用预审API
			List<Long> tlist = parseHouseId(oclist);
			for (Long houseId : tlist) {
				ResultDto rs = ipService.houseApprove(houseId.toString());
				// 机房提交预审失败，后依然需要删除缓存
				if (rs == null || rs.getResultCode() == ResultDto.ResultCodeEnum.ERROR.getCode()) {
					logger.error("house approve fail : " + rs);
				}
			}
			// 机房：删除缓存记录表中的交集部分记录
			int cn = operatorCacheDao.deleteOperatorCacheByHouseId(tlist);
			logger.info("delete " + cn + " operator cache by houseId : " + tlist);

			// 用户:交集部分调用预审API
			tlist = parseUserId(oclist);
			for (Long userId : tlist) {
				ResultDto rs = ipService.userApprove(userId.toString());
				if (rs == null || rs.getResultCode() == ResultDto.ResultCodeEnum.ERROR.getCode()) {
					logger.error("idcinfo approve fail : " + rs);
				}
			}
			// 用户：除缓存记录表中的交集部分记录
			cn = operatorCacheDao.deleteOperatorCacheByUserId(tlist);
			logger.info("delete " + cn + " operator cache by userId : " + tlist);
		} catch (Throwable e) { // 此模块的异常不能影响后续逻辑的执行，此处捕获所有异常，并写日志
			logger.error("clear operator cache fail", e);
		}
	}

	@SuppressWarnings("unused")
	private List<Long> parseJyzId(List<OperatorCache> oclist) {
		List<Long> list = new ArrayList<Long>();
		if (oclist == null || oclist.isEmpty()) {
			return list;
		}
		for (OperatorCache oc : oclist) {
			if (oc.getJyzId() != null && oc.getJyzId() > 0) {
				list.add(oc.getJyzId());
			}
		}
		return list;
	}

	private List<Long> parseUserId(List<OperatorCache> oclist) {
		List<Long> list = new ArrayList<Long>();
		if (oclist == null || oclist.isEmpty()) {
			return list;
		}
		for (OperatorCache oc : oclist) {
			if (oc.getUserId() != null && oc.getUserId() > 0) {
				list.add(oc.getUserId());
			}
		}
		return list;
	}

	private List<Long> parseHouseId(List<OperatorCache> oclist) {
		List<Long> list = new ArrayList<Long>();
		if (oclist == null || oclist.isEmpty()) {
			return list;
		}
		for (OperatorCache oc : oclist) {
			if (oc.getHouseId() != null && oc.getHouseId() > 0) {
				list.add(oc.getHouseId());
			}
		}
		return list;
	}
	
	private void cleanHouseByOperatorType(HouseInformation hinfor, int otype) {
		if (hinfor == null) {
			return;
		}

		int[] types;
		int[] frameTypes; // 机架的清洗与其它子节点不一样
		if (otype == 2) { // 变更时，取出新增/修改的子节点
			types = new int[] { 1, 2 }; 
			frameTypes = new int[] { 1, 2 ,3 };
		} else if (otype == 1) {	// 新增时，取出新增的子节点
			types = new int[] { 1 };
			frameTypes = types;
		} else { // 删除时，只取删除的子节点
			types = new int[] { otype };
			frameTypes = new int[] { };
		}
		
		List<HouseIPSegmentInformation> ipSegList = hinfor.getIpSegList();
		if (ipSegList != null) {
			List<?> dst = ipSegList.stream().filter(a -> a.getOperateType() != null && !contains(types,a.getOperateType()))
					.collect(Collectors.toList());
			if (dst != null) {
				ipSegList.removeAll(dst);
			}
		}
		List<HouseFrameInformation> frameList = hinfor.getFrameList();
		if (frameList != null) {
			List<?> dst = frameList.stream().filter(a -> a.getOperateType() != null && !contains(frameTypes,a.getOperateType()))
					.collect(Collectors.toList());
			if (dst != null) {
				frameList.removeAll(dst);
			}
		}
		List<HouseGatewayInformation> gatewayInfoList = hinfor.getGatewayInfoList();
		if (gatewayInfoList != null) {
			List<?> dst = gatewayInfoList.stream()
					.filter(a -> a.getOperateType() != null && !contains(types,a.getOperateType()))
					.collect(Collectors.toList());
			if (dst != null) {
				gatewayInfoList.removeAll(dst);
			}
		}
	}

	private boolean contains(int[] values,int value){
		if(values == null){
			return false;
		}
		for(int val : values){
			if(value == val){
				return true;
			}
		}
		return false;
	}
	
	private void cleanUserByOperatorType(UserInformation uinfor, int otype) {
		if (uinfor == null) {
			return;
		}
		int[] types;
		if (otype == 2) {
			types = new int[] { 1, 2 };
		} else if (otype == 1) {
			types = new int[] { 1 };
		} else {
			types = new int[] { otype };
		}
		List<UserServiceInformation> serviceList = uinfor.getServiceList();
		if (serviceList != null) {
			List<?> dst = serviceList.stream().filter(a -> a.getOperateType() != null && !contains(types,a.getOperateType()))
					.collect(Collectors.toList());
			if (dst != null) {
				serviceList.removeAll(dst);
			}
		}
		List<UserBandwidthInformation> bandwidthList = uinfor.getBandwidthList();
		if (bandwidthList != null) {
			List<?> dst = bandwidthList.stream().filter(a -> a.getOperateType() != null && !contains(types,a.getOperateType()))
					.collect(Collectors.toList());
			if (dst != null) {
				bandwidthList.removeAll(dst);
			}
		}
		List<UserVirtualInformation> virtualList = uinfor.getVirtualList();
		if (virtualList != null) {
			List<?> dst = virtualList.stream().filter(a -> a.getOperateType() != null && !contains(types,a.getOperateType()))
					.collect(Collectors.toList());
			if (dst != null) {
				virtualList.removeAll(dst);
			}
		}
	}

	/**
	 * 查询管局返回的某个文件在WAIT_ISMS_SUBMIT_REPORTFILE表中存的记录
	 * 
	 * @param fileName
	 * @return
	 */
	private IsmsWaitSubmitReportFile getReportFileInfo(String fileName) {
		return submitReportFileDao.getReportFileInfo(fileName);
	}

	/**
	 * 更新结果
	 * 
	 * @author liuz@aotian.com
	 * @date 2018年9月28日 下午6:50:06
	 */
	private class UpdateResult {
		private boolean success = true; // 默认成功
		private List<String> msgList = new ArrayList<String>(); // 提示信息

		public void appendWarn(String message) {
			msgList.add(DateUtils.getCurrentyyyMMddHHmmss() + " " + message);
		}
		
		public void append(UpdateResult cresult) {
			if(cresult == null){
				return;
			}
			if(!cresult.success){
				success = false;
			}
			if(cresult.msgList != null){
				msgList.addAll(cresult.msgList);
			}
		}

		public void appendError(String message) {
			success = false;
			msgList.add(DateUtils.getCurrentyyyMMddHHmmss() + " " + message);
		}
		
		public boolean isSuccess(){
			return success;
		}
		
		public String toString() {
			return msgList.stream().reduce("", (a, b) -> a + "\r\n" + b);
		}
	}

}
