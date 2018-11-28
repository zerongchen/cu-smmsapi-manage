package com.aotain.smmsapi.task.kafka;

import java.io.File;
import java.io.InputStream;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.aotain.common.config.dao.CmdInfoManageMapper;
import com.aotain.common.config.dao.IdcJcdmJkcsMapper;
import com.aotain.common.config.model.CmdAck;
import com.aotain.common.config.model.IdcHouses;
import com.aotain.common.utils.kafka.ICustomerCallback;
import com.aotain.common.utils.model.msg.SmmsAckQueue;
import com.aotain.common.utils.monitorstatistics.ModuleConstant;
import com.aotain.common.utils.redis.TaskMessageUtil;
import com.aotain.common.utils.tools.FileUtils;
import com.aotain.common.utils.tools.MonitorStatisticsUtils;
import com.aotain.common.utils.tools.Tools;
import com.aotain.common.utils.tools.XmlUtils;
import com.aotain.common.utils.tools.ZipUtils;
import com.aotain.smmsapi.task.bean.IdcCommandAck;
import com.aotain.smmsapi.task.bean.IdcCommandAck.CommandAck;
import com.aotain.smmsapi.task.bean.Return;
import com.aotain.smmsapi.task.client.AxisWSClient;
import com.aotain.smmsapi.task.client.BaseWSClient;
import com.aotain.smmsapi.task.client.CXFWSClient;
import com.aotain.smmsapi.task.client.TJCXFWSClient;
import com.aotain.smmsapi.task.constant.DateFormatConstant;
import com.aotain.smmsapi.task.constant.GlobalParams;
import com.aotain.smmsapi.task.utils.DateUtils;
import com.aotain.smmsapi.task.utils.LocalConfig;
import com.aotain.smmsapi.task.utils.SpringContextTool;
import com.aotain.smmsapi.task.utils.StringUtil;

import antlr.build.Tool;

/**
 * Ack上报任务（kafka队列消费者）
 * 
 * @author liuz@aotian.com
 * @date 2017年11月15日 下午4:13:14
 */
public class AckReplyJob implements ICustomerCallback {
	private static Logger logger = LoggerFactory.getLogger(AckReplyJob.class);

	private CmdInfoManageMapper cmdInfoManageMapper;
	
	@Autowired
	private IdcJcdmJkcsMapper idc;
	@SuppressWarnings("unchecked")
	@Override
	public void callback(int threadnum,int partition, long offset,String message) {
		if (StringUtils.isBlank(message)) {
			return;
		}
		logger.info("ack-reply thread " + threadnum + " receive message :" + message);

		SmmsAckQueue ack = JSON.parseObject(message, SmmsAckQueue.class);
		String xmlAck = ack.getAckxml();
		logger.info("ack-reply thread " + threadnum + " xml data : " + xmlAck);
		String identify = "taskid=" + ack.getTaskid() + ",parentid=" + ack.getToptaskid();
		String fileName=Tools.getBatchId();
		//ack发送给管局时，备份一份明文xml到服务器
		try {
			String path = com.aotain.common.config.LocalConfig.getInstance().getHashValueByHashKey("upload_ws_zip_file_path");
			path += DateUtils.formatDateTime(DateFormatConstant.DATE_CHS_HYPHEN, new Date());
			backupFile(path, fileName+".xml", xmlAck);
		} catch (Exception e) {
			logger.error("backup ack xml exception:" , e);
			MonitorStatisticsUtils.addEvent(ModuleConstant.MODULE_SMMSAPI_TASK_ACK, e);
		}
		
		boolean success = false;
		int del_flag = 0;//0-未上报|1-上报中|2-上报失败|3-上报成功
		// 1. 将xml字符串压缩成zip文件的byte[]数组
		try {
			byte[] data = ZipUtils.zip(xmlAck, fileName + ".xml", "UTF-8");
			// 2. 将byte[]进行加密，压缩等处理后，发送给管局ack上报接口
			String resourceMonitorFlag = LocalConfig.getInstance().getResourceMonitor();
			int clientType = Integer.parseInt((String) com.aotain.common.config.LocalConfig.getInstance()
					.getHashValueByHashKey(GlobalParams.CLIENT_VERSION));
			BaseWSClient client = null;
			Map<String, IdcHouses> idcHouses = com.aotain.common.config.LocalConfig.getInstance().getAllIdcHouses();
			Iterator<String> it = idcHouses.keySet().iterator();
			if (it.hasNext()) {
				String idcId = idcHouses.get(it.next()).getIdcId(); // idcId获取
				if (clientType == 1) { // axis2调用
					client = new AxisWSClient(idcId, data);
				} else if (clientType == 2) { // cxf调用
					if (!StringUtil.isEmptyString(resourceMonitorFlag)
							&& GlobalParams.INTERFACE_DEFINE_TJ.equals(resourceMonitorFlag)) {
						client = new TJCXFWSClient(idcId, data);
					} else {
						client = new CXFWSClient(idcId, data);
					}
				}
				Return rs = client.callService(client);
				logger.debug("ack-reply return data: " + identify + ",data = " + rs);

				if (rs == null || rs.getResultCode() == null) {
					del_flag =2;
					logger.error("ack-reply fail with unknown exception:" + identify);
				} else if (rs.getResultCode().equals(BigInteger.ZERO)) {
					success = true;
					del_flag =3;
					logger.info("ack-reply success:" + identify);
				} else {
					del_flag =2;
					logger.error("ack-reply fail : " + identify + ",code=" + rs.getResultCode() + ",version="
							+ rs.getVersion() + ",msg=" + rs.getMsg());
				}
				
				//ack信息写入数据库
				InputStream inputStream = ZipUtils.unZip2Stream(data);
				IdcCommandAck cmdAck = XmlUtils.createBean(inputStream, IdcCommandAck.class);
				List<CommandAck> cAckList = cmdAck.getCommandAck();
				CmdAck ackInfo = null;
				
				for(CommandAck ackDto:cAckList){
					ackInfo = new CmdAck();
					Date time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(com.aotain.smmsapi.task.utils.Tools.getTime((ack.getCreatetime())));
					ackInfo.setCreateTime(time);
					ackInfo.setDealFlag(del_flag);
					ackInfo.setCommandFileid(0);
					ackInfo.setIdcId(idcId);
					ackInfo.setHouseId(ackDto.getHouseId() == null? 0L : ackDto.getHouseId());
					ackInfo.setCommandId(ackDto.getCommandId());
					ackInfo.setView(ackDto.getView()==null?0:ackDto.getView());
					ackInfo.setType(ackDto.getType());
					ackInfo.setFilterCount(ackDto.getFilterCount()==null?0:ackDto.getFilterCount());
					ackInfo.setMsgInfo(ackDto.getMsgInfo()==null?"":ackDto.getMsgInfo());
					ackInfo.setMonitorCount(ackDto.getMonitorCount()==null?0:ackDto.getMonitorCount());
					ackInfo.setResultCode(ackDto.getResultCode());
					if(cmdInfoManageMapper ==  null){
						cmdInfoManageMapper = (CmdInfoManageMapper) SpringContextTool.getBean("cmdInfoManageMapper");
					}
					try {
						cmdInfoManageMapper.InsertCmdAckInfo(ackInfo);
						logger.info("commndId:"+ackDto.getCommandId() + " write ACK into DB success...");
					} catch (Exception e) {
						logger.error("commndId:"+ackDto.getCommandId() + " write ACK into DB failed...",e);
					}
				}
			} else {
				logger.error("cannot found idcid information , do ack-reply fail:" + identify);
			}
		} catch (Exception e) {
			logger.error("zip ack-reply data exception:" + identify, e);
			MonitorStatisticsUtils.addEvent(ModuleConstant.MODULE_SMMSAPI_TASK_ACK, e);
		}
		// 更新任务Hash状态
		TaskMessageUtil.getInstance().setTaskStatus(ack.getTaskid(),ack.getToptaskid(),2, success ? 4 : 3,ack.objectToJson());
	}
	
	/**
	 * ack 备份目录
	 * @param path
	 * @param fileName
	 * @param filecontent
	 */
	private static void backupFile(String path, String fileName, String filecontent) {
		if (StringUtils.isBlank(path)) {
			path = "./ack_backup/";
		} else {
			path = path.replace("\\", "/");
			if (!path.endsWith("/")) {
				path +=  "/";
			}
		}
		File dir = new File(path);
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				logger.warn("fail to backup ack xml - create dirs fail : " + path);
				return;
			}
		}
		String filenameTemp = path + fileName;
		try {
			// 创建文件成功后，写入内容到文件里
			FileUtils.writeToFile(filecontent.getBytes("UTF-8"), filenameTemp);
			logger.warn("success to backup ack xml : " + filenameTemp);
		} catch (Exception e) {
			logger.warn("fail to backup ack xml : " + filenameTemp,e);
		}
	}
}
