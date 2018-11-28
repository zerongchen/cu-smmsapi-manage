package com.aotain.smmsapi.task.quartz.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.aotain.common.config.LocalConfig;
import com.aotain.common.config.model.IdcHouses;
import com.aotain.common.utils.kafka.JobQueueUtil;
import com.aotain.common.utils.model.msg.JobQueue;
import com.aotain.common.utils.model.msg.RedisTaskStatus;
import com.aotain.common.utils.model.taskcmd.ActiveResourceMonitor;
import com.aotain.common.utils.model.taskcmd.BasicMonitor;
import com.aotain.common.utils.model.taskcmd.IllegalWeb;
import com.aotain.common.utils.monitorstatistics.ModuleConstant;
import com.aotain.common.utils.redis.TaskIdUtil;
import com.aotain.common.utils.redis.TaskMessageUtil;
import com.aotain.common.utils.tools.MonitorStatisticsUtils;
import com.aotain.smmsapi.task.QuartzMain;
import com.aotain.smmsapi.task.constant.GlobalParams;
import com.aotain.smmsapi.task.quartz.FileUploadService;
import com.aotain.smmsapi.task.utils.Tools;

@Component
@Service(value = "fileUpload")
public class FileUploadServiceImpl implements FileUploadService {

	private static Logger log = LoggerFactory.getLogger(FileUploadServiceImpl.class.getName());
	
	@Override
	public void createActiveStateTask() {
		if(QuartzMain.LSELECTOR == null || !QuartzMain.LSELECTOR.getLeader()){
			log.debug("service node(" + QuartzMain.SERVICE_NODE_NAME + ") is sleeping ...");
			return;
		}else{
			log.info("service node(" + QuartzMain.SERVICE_NODE_NAME + ") is working ...");
		}
		log.info("create active state monitor tasks .");
		taskToJobQueueAndRedis(GlobalParams.DT_ACTIVE_STATE,0,null, 0,null);
	}
	
	@Override
	public void createActiveResourcesMonitorTask() {
		if(QuartzMain.LSELECTOR == null || !QuartzMain.LSELECTOR.getLeader()){
			log.debug("service node(" + QuartzMain.SERVICE_NODE_NAME + ") is sleeping ...");
			return;
		}else{
			log.info("service node(" + QuartzMain.SERVICE_NODE_NAME + ") is working ...");
		}
		log.info("create active resources monitor tasks .");
		Map<String, IdcHouses> map = LocalConfig.getInstance().getAllIdcHouses();
		Iterator<String> it = map.keySet().iterator();
		while (it.hasNext()) {
			String houseIdStr = it.next();
			IdcHouses idcHouses = map.get(houseIdStr); 
			taskToJobQueueAndRedis(GlobalParams.DT_ACTIVE_RESOURCES, idcHouses.getHouseId(),houseIdStr, Integer.parseInt(getYesterday()), 0);
			taskToJobQueueAndRedis(GlobalParams.DT_ACTIVE_RESOURCES, idcHouses.getHouseId(),houseIdStr, Integer.parseInt(getYesterday()), 1);
		}
	}
	
	@Override
	public void createIllegalWebMonitorTask() {
		if(QuartzMain.LSELECTOR == null || !QuartzMain.LSELECTOR.getLeader()){
			log.debug("service node(" + QuartzMain.SERVICE_NODE_NAME + ") is sleeping ...");
			return;
		}else{
			log.info("service node(" + QuartzMain.SERVICE_NODE_NAME + ") is working ...");
		}
		Map<String, IdcHouses> map = LocalConfig.getInstance().getAllIdcHouses();
		Iterator<String> it = map.keySet().iterator();
		log.info("create illegal web monitor tasks .");
		while (it.hasNext()) {
			String houseIdStr = it.next();
			IdcHouses idcHouses = map.get(houseIdStr); 
			taskToJobQueueAndRedis(GlobalParams.DT_ILLEGAL_WEB, idcHouses.getHouseId(),houseIdStr, Integer.parseInt(getYesterday()),null);
		}
	}
	
	@Override
	public void createBasicMonitorTask() {
		if(QuartzMain.LSELECTOR == null || !QuartzMain.LSELECTOR.getLeader()){
			log.debug("service node(" + QuartzMain.SERVICE_NODE_NAME + ") is sleeping ...");
			return;
		}else{
			log.info("service node(" + QuartzMain.SERVICE_NODE_NAME + ") is working ...");
		}
		Map<String, IdcHouses> map = LocalConfig.getInstance().getAllIdcHouses();
		Iterator<String> it = map.keySet().iterator();
		log.info("create basic monitor tasks .");
		while (it.hasNext()) {
			String houseIdStr = it.next();
			IdcHouses idcHouses = map.get(houseIdStr); 
			taskToJobQueueAndRedis(GlobalParams.DT_BASIC_MONITOR, idcHouses.getHouseId(),houseIdStr, Integer.parseInt(getYesterday()),null);
		}
	}

	private void taskToJobQueueAndRedis(int dataType, long houseid,String houseidstr, int date,Integer type) {
		JobQueue job = new JobQueue();
		RedisTaskStatus redisTaskStatus = new RedisTaskStatus();
		String content = "";
		String commandStr = "";
		switch (dataType) {
		case GlobalParams.DT_ACTIVE_STATE:
			job.setJobtype(GlobalParams.UPLOAD_ACTIVE_STATE_TYPE);
			commandStr = (String) LocalConfig.getInstance().getHashValueByHashKey(GlobalParams.UPLOAD_ACTIVE_STATE_KEY);
			initUploadTaskParams(redisTaskStatus, commandStr);
			break;
		case GlobalParams.DT_ACTIVE_RESOURCES:
			ActiveResourceMonitor dto = new ActiveResourceMonitor();
			dto.setHouseid(houseid);
			if(type!=null){
				dto.setType(type);
			}
			dto.setDate(date);
			dto.setHouseidstr(houseidstr);
			content = dto.objectToJson();
			job.setJobtype(GlobalParams.UPLOAD_ACTIVE_RESOURCE_MONITOR_TYPE);
			commandStr = (String) LocalConfig.getInstance().getHashValueByHashKey(GlobalParams.UPLOAD_ACTIVE_RESOURCE_MONITOR_KEY);
			initUploadTaskParams(redisTaskStatus, commandStr);
			break;
		case GlobalParams.DT_ILLEGAL_WEB:
			IllegalWeb idto = new IllegalWeb();
			idto.setHouseid(houseid);
			idto.setDate(date);
			idto.setHouseidstr(houseidstr);
			content = idto.objectToJson();
			job.setJobtype(GlobalParams.UPLOAD_ILLEGAL_WEB_MONITOR_TYPE);
			commandStr = (String) LocalConfig.getInstance().getHashValueByHashKey(GlobalParams.UPLOAD_ILLEGAL_MONITOR_KEY);
			initUploadTaskParams(redisTaskStatus, commandStr);
			break;
		case GlobalParams.DT_BASIC_MONITOR:
			BasicMonitor bdto = new BasicMonitor();
			bdto.setHouseid(houseid);
			bdto.setDate(date);
			bdto.setHouseidstr(houseidstr);
			content = bdto.objectToJson();
			job.setJobtype(GlobalParams.UPLOAD_BASIC_MONITOR_TYPE);
			commandStr = (String) LocalConfig.getInstance().getHashValueByHashKey(GlobalParams.UPLOAD_IDC_MONITOR_KEY);
			initUploadTaskParams(redisTaskStatus, commandStr);
		default:
			break;
		}
		long createTime = Tools.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		Long taskId = TaskIdUtil.getInstance().getTaskId();
		try {
			job.setToptaskid(0L);
			job.setTaskid(taskId);
			job.setParams(content);
			job.setCreatetime(System.currentTimeMillis()/1000);
			job.setCreatetime(createTime);
			job.setToptaskid(0L);
			job.setIsretry(0);
			JobQueueUtil.sendMsgToKafkaJobQueue(job);
			
			//写入redis任务hash
			redisTaskStatus.setTaskid(taskId);
			redisTaskStatus.setTasktype(1);
			redisTaskStatus.setToptaskid(0L);
			redisTaskStatus.setCreatetime(createTime);
			redisTaskStatus.setContent(job.objectToJson());
			redisTaskStatus.setStatus(1);
			redisTaskStatus.setTimes(1);
			TaskMessageUtil.getInstance().setTask(taskId, redisTaskStatus);
		} catch (Exception e) {
			log.error("schedule reply job exception：type="+dataType,e);
			MonitorStatisticsUtils.addEvent(ModuleConstant.MODULE_SMMSAPI_TASK_QUARTZ, e);
		}
	}
	
	private String getYesterday() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		Date time = cal.getTime();
		return new SimpleDateFormat("yyyyMMdd").format(time);
	}
	
	private void initUploadTaskParams(RedisTaskStatus redisTaskStatus, String commandStrs) {
		String[] params = commandStrs.split(",");
		redisTaskStatus.setMaxtimes(Integer.parseInt(params[0]));
		redisTaskStatus.setExpiretime(Long.parseLong(params[1]));
		redisTaskStatus.setInterval(Integer.parseInt(params[2]));
	}
}
