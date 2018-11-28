package com.aotain.smmsapi.task.quartz.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.aotain.common.config.ContextUtil;
import com.aotain.common.config.LocalConfig;
import com.aotain.common.config.redis.BaseRedisService;
import com.aotain.common.utils.model.msg.FileUploadInfo;
import com.aotain.common.utils.model.msg.RedisTaskStatus;
import com.aotain.common.utils.monitorstatistics.ModuleConstant;
import com.aotain.common.utils.redis.TaskMessageUtil;
import com.aotain.common.utils.tools.MonitorStatisticsUtils;
import com.aotain.smmsapi.task.QuartzMain;
import com.aotain.smmsapi.task.constant.GlobalParams;
import com.aotain.smmsapi.task.logStatus.service.LogReportService;
import com.aotain.smmsapi.task.quartz.DealResultService;
import com.aotain.smmsapi.task.quartz.mapper.TaskFileDao;
import com.aotain.smmsapi.task.smmsreturn.service.SmmsReturnService;
import com.aotain.smmsapi.task.utils.StringUtil;

@Component
@Service(value = "baseIDCWebService")
public class DealResultServiceImpl implements DealResultService {
	private static Logger log = LoggerFactory.getLogger(DealResultServiceImpl.class);

	@Autowired
	private LogReportService logReportService;
	
	@Autowired
	private SmmsReturnService smmsReturnService;
	
	@Autowired
	private TaskFileDao taskFileDao;
	
	public void dealSmmsProcessResult() {
		if (QuartzMain.LSELECTOR == null || !QuartzMain.LSELECTOR.getLeader()) {
			log.debug("service node(" + QuartzMain.SERVICE_NODE_NAME + ") is sleeping ...");
			return;
		} else {
			log.info("service node(" + QuartzMain.SERVICE_NODE_NAME + ") is working ...");
		}
		log.info("create smms process result tasks .");
		try {
			String ufpath = (String) com.aotain.common.config.LocalConfig.getInstance()
					.getHashValueByHashKey(GlobalParams.UPLOAD_FILE_PATH);
			String ufbakpath = (String) com.aotain.common.config.LocalConfig.getInstance()
					.getHashValueByHashKey(GlobalParams.UPLOAD_FILE_BAK_PATH);
			String ftpwpath = (String) com.aotain.common.config.LocalConfig.getInstance()
					.getHashValueByHashKey(GlobalParams.FTP_PATH);
			String ftpip = (String) com.aotain.common.config.LocalConfig.getInstance()
					.getHashValueByHashKey(GlobalParams.FTP_IP);
			String ftpport = (String) com.aotain.common.config.LocalConfig.getInstance()
					.getHashValueByHashKey(GlobalParams.FTP_PORT);
			String ftpusr = (String) com.aotain.common.config.LocalConfig.getInstance()
					.getHashValueByHashKey(GlobalParams.FTP_USER);
			String ftppwd = (String) com.aotain.common.config.LocalConfig.getInstance()
					.getHashValueByHashKey(GlobalParams.FTP_PASSWORD);
			String ftpMode = (String) com.aotain.common.config.LocalConfig.getInstance()
					.getHashValueByHashKey(GlobalParams.FTP_MODE); // true-被动,其它-主动
			String uploadFilePath = ufpath;
			String uploadFileBakPath = ufbakpath;
			String ftpWorkPath = ftpwpath;
			if (!uploadFilePath.endsWith(File.separator)) {
				uploadFilePath = uploadFilePath + File.separator;
			}
			if (!uploadFileBakPath.endsWith(File.separator)) {
				uploadFileBakPath = uploadFileBakPath + File.separator;
			}
			if (!ftpWorkPath.endsWith(File.separator)) {
				ftpWorkPath = ftpWorkPath + "/";
			}
			int ftpPort = Integer.parseInt(ftpport);
			// 登陆FTP服务器
			FTPClient ftp = new FTPClient();
			
			try {
				ftp.connect(ftpip, ftpPort);
				ftp.login(ftpusr, ftppwd);// 登录
				// 在指定为被动模式的情况下进入被动模式
				if(org.apache.commons.lang.StringUtils.isNotBlank(ftpMode) && "true".equals(ftpMode)){
					//ftp.enterLocalActiveMode();    //主动模式
					ftp.enterLocalPassiveMode(); //被动模式
				}
				log.info("User " + ftpusr + " login " + ftpip + " server success!!");
			} catch (Exception e1) {
				log.error("User " + ftpusr + " login " + ftpip + " server failed!!");
				MonitorStatisticsUtils.addEvent(ModuleConstant.MODULE_SMMSAPI_TASK_QUARTZ, e1);
				return;
			}
			String remotePath = ftpWorkPath + "999/";
			//处理999目录文件
//			dealDirectoryFiles(ftp, remotePath);
			dealDirectoryFilesByBatch(ftp, remotePath, 1000);
			// 校验管局999文件
			ftp.logout();
		} catch (Exception e) {
			log.error("smms 999 directory deal exception:", e);
			MonitorStatisticsUtils.addEvent(ModuleConstant.MODULE_SMMSAPI_TASK_QUARTZ, e);
		}
	}
	
	private void dealDirectoryFilesByBatch(FTPClient ftp, String remotePath,int batchSize) throws IOException{
		Boolean flag = ftp.changeWorkingDirectory(remotePath);// 切换到999目录
		if (flag) {
			log.info("changeWorkingDirectory " + remotePath + "success...");
		} else {
			log.error("changeWorkingDirectory " + remotePath + "failed...");
			return;
		}
		// 在999目录查找符合规则的文件
		FTPFile[] files = listFiles(ftp,batchSize);
		if(files == null || files.length == 0) {
			return;
		}
		final BaseRedisService<String, String, String> rediscluster = ContextUtil.getContext().getBean("baseRedisServiceImpl",BaseRedisService.class);
		// 遍历文件，进行处理
		for(FTPFile file : files) {
			String fileName = file.getName();
			
			// 检查此文件是否正在被别的线程处理，如果是则跳过不处理
			String result = rediscluster.getHash("deal_fileName", fileName);
			if(!StringUtil.isEmptyString(result)){
				continue; 
			}
			
			log.info("deal file at 999 directory:" + fileName);
			//先将fileName 写入redis hash，以便不让其他线程处理
			rediscluster.putHash("deal_fileName", fileName, "");
			
			// 读取文件内容
			String content = getContent(ftp,fileName);
			// 解析上报文件名、文件处理状态、taskId
			String[] tmpStrs = fileName.split("-");
			int fileType = -1;
			String fName = tmpStrs[1];
			int status = -1;
			long taskId = -1L;
			try{
				// 1-基础数据记录类型；2-基础数据监测异常记录数据类型；3-访问日志查询记录数据类型；
				// 4-违法信息监测记录数据类型；5-违法信息过滤记录数据类型；6-基础数据查询指令数据类型；
				// 7-ISMS活动状态数据类型；8-活跃资源监测记录数据类型；9-违法违规网站监测记录类型
				fileType = Integer.parseInt(tmpStrs[0]); 
				status = Integer.parseInt(tmpStrs[2].split("\\.")[0]);
				taskId = Long.parseLong(fName.substring(fName.length() - 10));
			}catch(Exception e){
				log.error("parse taskId or status fail - unknow file ：" + fileName,e);
				// 删除无效文件
				ftp.deleteFile(fileName);
				//文件名也从redis hash中删除
				rediscluster.removeHash("deal_fileName", fileName);
				MonitorStatisticsUtils.addEvent(ModuleConstant.MODULE_SMMSAPI_TASK_QUARTZ, e);
				continue;
			}
			
			// 输出1：更新idc_isms_stat_log_report表的状态信息
			try{
				int returnStatu = (status == 0 ? 0 : 1); // status == 0 表示管局返回成功
				logReportService.updateLogStatusByTaskId(taskId,returnStatu);
			}catch(Exception e){
				log.warn("update idc_isms_stat_log_report status fail：" + fileName,e);
			} // 输出1 失败时，还需继续执行输出2
			
			// 输出2：更新文件上报任务状态
			try{
				updateUploadXmlTaskStatus(taskId,fName,status,content,ftp,fileName);
			}catch(Exception e){
				log.warn("update upload xml task status fail：" + fileName,e);
			} // 输出2 失败时，还需继续执行输出3
			
			// 输出3 ：调用上报完成处理流程
			try{
				// 写redis key缓存即可，另外起独立的线程专门进行文件处理结果更新
				smmsReturnService.writeRedisCache(taskId, fName, status,fileType);
			}catch(Exception e){
				log.warn("update upload xml task status fail：" + fName,e);
			}
		}
	}

	/**
	 * 更新文件状态
	 * @param taskId
	 * @param fName
	 * @param status
	 * @param content
	 * @param ftp
	 * @param fileName
	 */
	private void updateUploadXmlTaskStatus(long taskId,String fName,int status,String content,FTPClient ftp,String fileName) {
		RedisTaskStatus redisTaskStatus = TaskMessageUtil.getInstance().getTask(taskId);
		FileUploadInfo fileUploadInfo;
		boolean isTimeout = false;
		if (redisTaskStatus == null) {// 超时直接更新数据库
			redisTaskStatus = new RedisTaskStatus();
			fileUploadInfo = new FileUploadInfo();
			redisTaskStatus.setTaskid(taskId);
			redisTaskStatus.setToptaskid(taskId);
			redisTaskStatus.setTasktype(3);
			redisTaskStatus.setCreatetime(System.currentTimeMillis() / 1000);
			redisTaskStatus.setTimes(1);
			String commandStr = (String) LocalConfig.getInstance().getHashValueByHashKey(GlobalParams.UPLOAD_SMMS_FILE_KEY);
			initUploadTaskParams(redisTaskStatus, commandStr);
			fName+=".xml";
			fileUploadInfo.setFilename(fName);// 文件名
			isTimeout = true;
		} else { // 未超时，修改redis状态
			// 防止toptaskid为0，导致message入库报错
			if(redisTaskStatus.getToptaskid() == null || redisTaskStatus.getToptaskid()==0L){
				redisTaskStatus.setToptaskid(redisTaskStatus.getTaskid());
			}
			String obString = redisTaskStatus.getContent();
			fileUploadInfo = JSON.parseObject(obString,FileUploadInfo.class);
		}
		fileUploadInfo.setStatus(status);// 文件状态
		fileUploadInfo.setDealresult(content == null ? "" : content);// 文件内容
		redisTaskStatus.setStatus(4);
		redisTaskStatus.setContent(JSON.toJSONString(fileUploadInfo));
		
		// 删除相应文件
		try {
			ftp.deleteFile(fileName);
			//删除文件成功则写入redis
			if(!isTimeout) {
				TaskMessageUtil.getInstance().setTask(redisTaskStatus.getTaskid(), redisTaskStatus);
			}else{
				// 超时直接更新数据库
				try {
					int cnt = taskFileDao.updateFileStatus(fileUploadInfo);
					if(cnt <= 0){
						log.error("updateFileStatus fail ", fileUploadInfo.toString());	
					}
				} catch (Exception e) {
					log.error("updateFileStatus exception ",e);
				}
			}
			//文件名也从redis hash中删除
			BaseRedisService<String, String, String> rediscluster = ContextUtil.getContext().getBean("baseRedisServiceImpl",BaseRedisService.class);
			rediscluster.removeHash("deal_fileName", fileName);
			log.info("delete filename:" + fileName + "success..");
		} catch (Exception e) {
			log.error("delete filename:" + fileName + "exception..", e);
			MonitorStatisticsUtils.addEvent(ModuleConstant.MODULE_SMMSAPI_TASK_QUARTZ, e);
		}
	}

	/**
	 * 下载文件内容
	 * @param ftp
	 * @param fileName
	 * @return
	 */
	private String getContent(FTPClient ftp, String fileName) {
		InputStream ins = null;
		BufferedReader br = null; 
		StringBuilder dealContent = new StringBuilder();
		try {
			ins = ftp.retrieveFileStream(fileName);
			br = new BufferedReader(new InputStreamReader(ins, "UTF-8"));
			String line = "";
			int i = 0;
			while ((line = br.readLine()) != null) {
				if(i > 0){
					dealContent.append("\r\n");
				}
				dealContent.append(line);
				i++;
			}
		} catch (IOException e) {
			log.error("read 999 file content fail : " + fileName, e);
			return null;
		} finally {
			try {
				if (br != null) {
					br.close();
				}
				if (ins != null) {
					ins.close();
				}
				ftp.completePendingCommand();
			} catch (IOException e) {
				log.warn("data stream close exception", e);
			}
		}
		return dealContent.toString();
	}

	/**
	 * 过滤并获取所有满足条件的管局999文件
	 * @param ftp
	 * @param batchSize
	 * @return
	 */
	private FTPFile[] listFiles(FTPClient ftp, int batchSize) {
		FTPFile[] files = null;
		final BaseRedisService<String, String, String> rediscluster = ContextUtil.getContext().getBean("baseRedisServiceImpl",BaseRedisService.class);
		try {
			files = ftp.listFiles(".", new FTPFileFilter() {
				private int index = 0;
				@Override
				public boolean accept(FTPFile file) {

					if(index == batchSize){
						return false;
					}
					if (file.isDirectory()) {
						return false;
					}
					String result = rediscluster.getHash("deal_fileName", file.getName());
					if(!StringUtils.isBlank(result)){
						return false;
					}
					// 111-11111111111111111111-0
					if (file.getName().matches(".*\\w+-\\d{20,}-\\w+.*")) {
						index++;
						return true;
					}
					return false;
				}
			});
		} catch (IOException e) {
			log.error("list 999 directory files failed" , e);
			MonitorStatisticsUtils.addEvent(ModuleConstant.MODULE_SMMSAPI_TASK_QUARTZ, e);
			return null;
		}
		
		if(files == null) { // 空目录打印一行日志
			log.info("directory 999 is empty");
		}
		
		return files;
	}

	/**
	 * 
	 * 处理999目录文件，读取文件返回redis任务TaskId和文件处理结果集合
	 * 
	 * @author : songl
	 * @since:2017年11月13日 下午6:54:31
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private void dealDirectoryFiles(FTPClient ftp, String remotePath) throws IOException {
		Boolean flag = ftp.changeWorkingDirectory(remotePath);// 切换到999目录
		if (flag) {
			log.info("changeWorkingDirectory " + remotePath + "success...");
		} else {
			log.info("changeWorkingDirectory " + remotePath + "failed...");
		}
		//List<RedisTaskStatus> taskIdList = new ArrayList<RedisTaskStatus>();
		RedisTaskStatus redisTaskStatus = null;
		final BaseRedisService<String, String, String> rediscluster = ContextUtil.getContext().getBean("baseRedisServiceImpl",BaseRedisService.class);
		String[] fileNames = null;
		try {
			FTPFile[] files = ftp.listFiles(".", new FTPFileFilter() {
				@Override
				public boolean accept(FTPFile file) {
					if (file.isDirectory()) {
						return false;
					}
					String result = rediscluster.getHash("deal_fileName", file.getName());
					if(!StringUtils.isBlank(result)){
						return false;
					}
					if (file.getName().matches(".*\\w+-\\w+-\\w+.*")) {
						return true;
					}
					return false;
				}
			});
			if (files != null) {
				int i = 0;
				fileNames = new String[files.length];
				for (FTPFile fileName : files) {
					fileNames[i++] = fileName.getName();
				}
			}

			BufferedReader br = null;
			StringBuilder dealContent = null;
			InputStream ins = null;
			FileUploadInfo fileUploadInfo = null;
			if (fileNames != null && fileNames.length > 0) {
				for (int i = 0; i < fileNames.length && i < 1000; i++) {
					String fileName = fileNames[i];
					
					String result = rediscluster.getHash("deal_fileName", fileName);
					/**
					 * 如果该文件正在被其他线程处理则跳过,
					 * 否则继续处理处理完后删除文件名在redis记录
					 */
					if(StringUtil.isEmptyString(result)){
						log.info("deal file at 999 directory:" + fileName);
						
						//先将fileName 写入redis hash，以便不让其他线程处理
						rediscluster.putHash("deal_fileName", fileName, "");
						
						fileName = fileName.substring(fileName.lastIndexOf(File.separator) + 1);
						// 获取文件目录
						ins = ftp.retrieveFileStream(fileName);
						fileName = fileName.substring(fileName.lastIndexOf(File.separator) + 1);
						br = new BufferedReader(new InputStreamReader(ins, "UTF-8"));
						String line = "";
						dealContent = new StringBuilder(150);
						while ((line = br.readLine()) != null) {
							dealContent.append(line);
						}
						if (ins != null) {
							ins.close();
							ftp.completePendingCommand();
						}
						br.close();
						String[] param = fileName.split("-");

						if (param.length >= 0 && param != null) {
							String fName = param[1];// 返回的文件名+taskId
							
							if(fName.length()<10){
								//存在文件名小于10位数的错误文件，暂时不作处理，redis的记录暂不删除，以便其他线程不会扫描到该文件
								log.warn("Exist warning file :"+fName +" (fileName's length is less than 10)");
								continue;
							}
							long taskId;
							String[] dealResultArr = param[2].split("\\.");
							Integer status = Integer.parseInt(dealResultArr[0]);// 文件处理状态
							try {
								taskId = Long.parseLong(fName.substring(fName.length() - 10, fName.length()));
								
								//日志上报回写状态
								int returnStatu =0;
								if(status!=0){
									returnStatu =1;
								}
								logReportService.updateLogStatusByTaskId(taskId,returnStatu);
							} catch (NumberFormatException e1) {
								//将错误文件挪移
								ftp.deleteFile(fileName);
								//文件名也从redis hash中删除
								rediscluster.removeHash("deal_fileName", fileName);
								log.error("unknow file:"+fName);
								MonitorStatisticsUtils.addEvent(ModuleConstant.MODULE_SMMSAPI_TASK_QUARTZ, e1);
								continue;
							}
							
							redisTaskStatus = TaskMessageUtil.getInstance().getTask(taskId);
							if (redisTaskStatus == null) {// 超时查询,重新建立对象
								redisTaskStatus = new RedisTaskStatus();
								fileUploadInfo = new FileUploadInfo();
								redisTaskStatus.setTaskid(taskId);
								redisTaskStatus.setToptaskid(taskId);
								redisTaskStatus.setTasktype(3);
								redisTaskStatus.setCreatetime(System.currentTimeMillis() / 1000);
								redisTaskStatus.setTimes(1);
								String commandStr = (String) LocalConfig.getInstance().getHashValueByHashKey(GlobalParams.UPLOAD_SMMS_FILE_KEY);
								initUploadTaskParams(redisTaskStatus, commandStr);
								
								fName+=".xml";
								fileUploadInfo.setFilename(fName);// 文件名
							} else {
								// 防止toptaskid为0，导致message入库报错
								if(redisTaskStatus.getToptaskid() == null || redisTaskStatus.getToptaskid()==0L){
									redisTaskStatus.setToptaskid(redisTaskStatus.getTaskid());
								}
								String obString = redisTaskStatus.getContent();
								fileUploadInfo = JSON.parseObject(obString,FileUploadInfo.class);
							}

							
							/**
							 * 处理文件信息
							 */
							fileUploadInfo.setStatus(status);// 文件状态
							
							fileUploadInfo.setDealresult(dealContent.toString());// 文件内容
							
							redisTaskStatus.setStatus(4);
							redisTaskStatus.setContent(JSON.toJSONString(fileUploadInfo));

							
							// 删除相应文件
							try {
								ftp.deleteFile(fileName);
								//删除文件成功则写入redis
								TaskMessageUtil.getInstance().setTask(redisTaskStatus.getTaskid(), redisTaskStatus);
								
								//文件名也从redis hash中删除
								rediscluster.removeHash("deal_fileName", fileName);
								
								log.info("delete filename:" + fileName + "success..");
							} catch (Exception e) {
								log.error("delete filename:" + fileName + "exception..", e);
								MonitorStatisticsUtils.addEvent(ModuleConstant.MODULE_SMMSAPI_TASK_QUARTZ, e);
							}
							
					}
					
					}
				}
			}
		} catch (IOException e) {
			log.error("read 999 directory failed" , e);
			MonitorStatisticsUtils.addEvent(ModuleConstant.MODULE_SMMSAPI_TASK_QUARTZ, e);
		}
	}
	
	private void initUploadTaskParams(RedisTaskStatus redisTaskStatus, String commandStrs) {
		String[] params = commandStrs.split(",");
		redisTaskStatus.setMaxtimes(Integer.parseInt(params[0]));
		redisTaskStatus.setExpiretime(Long.parseLong(params[1]));
		redisTaskStatus.setInterval(Integer.parseInt(params[2]));
	}
	
	public static void main(String[] args) {
	
		/*String obString = "{\"content\": {\"createtime\": 1516008001,\"filepath\": \"/home/at_dev/isms/isms_data/upload/7/2018-01-15/15160080010002387531.xml\",\"status\": -1,\"filename\":\"15160080010002387531.xml\",\"recordcount\": [1],\"ip\": \"192.168.50.201 \"},\"createip\": \"server-1/192.168.50.201\",\"createtime\": 1516008001,\"expiretime\": 0,\"interval\": 600,\"maxtimes\": 1,\"status\": 1,\"taskid\":2387531,\"tasktype\": 3,\"times\": 1,\"toptaskid\": 2387530}";
		@SuppressWarnings("unused")
		FileUploadInfo fileUploadInfo = JSON.parseObject(obString,FileUploadInfo.class);
		*/
	}

}
