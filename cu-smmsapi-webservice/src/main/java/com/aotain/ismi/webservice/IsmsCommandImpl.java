package com.aotain.ismi.webservice;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.aotain.common.config.LocalConfig;
import com.aotain.common.utils.kafka.JobQueueUtil;
import com.aotain.common.utils.model.msg.JobQueue;
import com.aotain.common.utils.model.msg.RedisTaskStatus;
import com.aotain.common.utils.model.smmscmd.Blacklist;
import com.aotain.common.utils.model.smmscmd.CodeList;
import com.aotain.common.utils.model.smmscmd.Command;
import com.aotain.common.utils.model.smmscmd.CommandBaseVo;
import com.aotain.common.utils.model.smmscmd.CommandRecord;
import com.aotain.common.utils.model.smmscmd.IdcInfoManage;
import com.aotain.common.utils.model.smmscmd.LogQuery;
import com.aotain.common.utils.model.smmscmd.NoFilter;
import com.aotain.common.utils.model.smmscmd.QueryView;
import com.aotain.common.utils.model.smmscmd.ReturnInfo;
import com.aotain.common.utils.monitorstatistics.ModuleConstant;
import com.aotain.common.utils.redis.TaskIdUtil;
import com.aotain.common.utils.redis.TaskMessageUtil;
import com.aotain.common.utils.tools.AES;
import com.aotain.common.utils.tools.FileUtils;
import com.aotain.common.utils.tools.MD5;
import com.aotain.common.utils.tools.MonitorStatisticsUtils;
import com.aotain.common.utils.tools.SHA1;
import com.aotain.common.utils.tools.XmlUtils;
import com.aotain.common.utils.tools.ZipUtils;
import com.aotain.smmsapi.webservice.BaseWs;
import com.aotain.smmsapi.webservice.constant.GlobalParams;
import com.aotain.smmsapi.webservice.model.CommandData;
import com.aotain.smmsapi.webservice.model.Return;
import com.aotain.smmsapi.webservice.utils.StringUtil;
import com.aotain.smmsapi.webservice.utils.Tools;
import com.aotain.smmsapi.webservice.validate.CommandFileXsdValidator;

import sun.misc.BASE64Decoder;

@SuppressWarnings("restriction")
@Transactional
@Repository
@Service(value = "idcCommand")
public class IsmsCommandImpl extends BaseWs implements IsmsCommand {
	private Logger logger = Logger.getLogger(IsmsCommandImpl.class);

	public static void main(String[] args) {
		IsmsCommandImpl obj = new IsmsCommandImpl();
		String idcId = "A2.B1.B2-20090001";
		String randVal = "2KNAFYTUf2fjAotvyWNV";
		String pwdHash = "NWI1NGUzNmZlOTY3MTVkZWI1YmQyMzg2YTkyYzI0MjU=";
		String command = "UEsDBBQACAAIAC9QbUsAAAAAAAAAAAAAAAARAAAAMTUxMDUzODQ5MTA2OS54bWxlj00KgzAQhfee"
				+ "ImSvmYnQP2Kk7lz0EGKiFUwiVUu9faOh2NLZzPDmzfcYkb9MT576MXbOZhQToETb2qnOthmdpyY+"
				+ "UTJOlVVV76zO6KJHmstIdKoubeNula1aLSPiS9TOGO8slUQAOAi2C8EwLYOWINjWf288KyibutKV"
				+ "vPKkwKTgMQc4eyIKFha78e7mUa+Bgn3GwGV/YDF1RvtXzCA54DFGjDElCBfAS+rv93UUcr7eewNQ"
				+ "SwcI04IXzrQAAAAnAQAAUEsBAhQAFAAIAAgAL1BtS9OCF860AAAAJwEAABEAAAAAAAAAAAAAAAAA"
				+ "AAAAADE1MTA1Mzg0OTEwNjkueG1sUEsFBgAAAAABAAEAPwAAAPMAAAAAAA==";
		String commandHash = "MzBkODUzZDMyZWM3MzE0NjQ5YjlkOWQ0ZWE3NGI3NTU=";
		int commandType = 5;
		long commandSequence = 1977744929618015664L;// 下发唯一编号
		int encryptAlgorithm = 0;// AES加密算法
		int hashAlgorithm = 1;// 哈希算法
		int compressionFormat = 1;// 压缩格式
		String commandVersion = "v2.0";// 版本信息
		obj.idc_command(idcId, randVal, pwdHash, command, commandHash, commandType, commandSequence, encryptAlgorithm,
				hashAlgorithm, compressionFormat, commandVersion);

	}
	
	@PostConstruct
	private void init(){
		MonitorStatisticsUtils.initModuleNoThread(ModuleConstant.MODULE_INTERFACE);
	}

	@Override
	public String idc_command(String idcId, String randVal, String pwdHash, String command, String commandHash,
			int commandType, long commandSequence, int encryptAlgorithm, int hashAlgorithm, int compressionFormat,
			String commandVersion) {
		Return result = new Return();
		String identify = "idcId=" + idcId + ",randVal=" + randVal + ",commandSequence=" + commandSequence
				+ ",commandVersion=" + commandVersion;
		MonitorStatisticsUtils.addEvent(ModuleConstant.MODULE_INTERFACE, null);
		try {
			String wsPwd = (String) LocalConfig.getInstance().getHashValueByHashKey(GlobalParams.PASSWORD);
			String m_pwdHash = "";

			logger.info("receive smms command：" + identify);
			logger.debug("smms command details：idcId=" + idcId + ",randVal=" + randVal + ",pwdHash=" + pwdHash
					+ ",command=" + command + ",commandHash=" + commandHash + ",commandType=" + commandType
					+ ",commandSequence=" + commandSequence + ",encryptAlgorithm=" + encryptAlgorithm
					+ ",hashAlgorithm=" + hashAlgorithm + ",compressionFormat=" + compressionFormat + ",commandVersion="
					+ commandVersion);

			// 1. 密码hash校验
			if (hashAlgorithm == 0) {
				m_pwdHash = Tools.encodeBase64(wsPwd + randVal);
			} else if (hashAlgorithm == 1) {
				m_pwdHash = MD5.Encrypt2((wsPwd + randVal).getBytes());
			} else if (hashAlgorithm == 2) {
				SHA1 sha1 = new SHA1();
				String byteData = wsPwd + randVal;
				m_pwdHash = sha1.getDigestOfbase64(byteData.getBytes());
			} else {
				logger.error("nonsupport hash type, hashAlgorithm=" + hashAlgorithm + "：" + identify);
				result.setResultCode(new BigInteger("1"));
				result.setMsg(this.getMsgByCode(1));
				return getUploadReturnString(result);
			}
			if (!m_pwdHash.equals(pwdHash)) {
				logger.error("password hash code mismatch,clientPwdHash=" + pwdHash + ",serverPwdHash=" + m_pwdHash
						+ "：" + identify);
				result.setResultCode(new BigInteger("1"));
				result.setMsg(this.getMsgByCode(1));
				return getUploadReturnString(result);
			}

			if (encryptAlgorithm != 0 && encryptAlgorithm != 1) {
				logger.error("nonsupport encrypt type,encryptAlgorithm=" + encryptAlgorithm + "：" + identify);
				result.setResultCode(new BigInteger("2"));
				result.setMsg(this.getMsgByCode(2));
				return getUploadReturnString(result);
			}

			// 2. 数据解密
			String aesKey = (String) LocalConfig.getInstance().getHashValueByHashKey(GlobalParams.AES_KEY);
			String aesIv = (String) LocalConfig.getInstance().getHashValueByHashKey(GlobalParams.AES_IV);
			byte[] beianInfoFile = null;
			if (encryptAlgorithm != 0) {
				try {
					beianInfoFile = AES.DecryptReturnByte(command, aesKey, aesIv);
				} catch (Exception e) {
					logger.error("AES decrypt exception：" + identify, e);
					MonitorStatisticsUtils.addEvent(ModuleConstant.MODULE_INTERFACE, e);
				}
			} else {
				try {
					beianInfoFile = new BASE64Decoder().decodeBuffer(command);
				} catch (Exception e) {
					logger.error("base64 decode exception：" + identify, e);
					MonitorStatisticsUtils.addEvent(ModuleConstant.MODULE_INTERFACE, e);
				}
			}

			if (beianInfoFile == null) {
				logger.error("command xml file is decode fail ：command=" + command + ",encryptAlgorithm="
						+ encryptAlgorithm + "：" + identify);
				result.setResultCode(new BigInteger("2"));
				result.setMsg(this.getMsgByCode(2));
				return getUploadReturnString(result);
			}

			// 3. 指令数据hash校验
			String rzKey = (String) LocalConfig.getInstance().getHashValueByHashKey(GlobalParams.RZ_KEY);
			String fileHash = "";
			try {
				byte[] resultBytes = StringUtil.joinBytes(beianInfoFile, rzKey.getBytes("UTF-8"));
				if (hashAlgorithm == 0) {
					fileHash = Tools.encodeBase64(beianInfoFile.toString());
				} else if (hashAlgorithm == 1) {
					fileHash = MD5.Encrypt2(resultBytes);
				} else if (hashAlgorithm == 2) {
					SHA1 sha1 = new SHA1();
					fileHash = sha1.getDigestOfbase64(resultBytes);
				}

				if (hashAlgorithm != 0 && !commandHash.equals(fileHash)) {
					logger.error("command xml file hase code mismatch,clientFileHash=" + commandHash
							+ ",serverFileHash=" + fileHash + "：" + identify);
					result.setResultCode(new BigInteger("3"));
					result.setMsg(this.getMsgByCode(3));
					return getUploadReturnString(result);
				}
			} catch (Exception e) {
				logger.error("command xml file hase code found expcetion in decode：" + identify, e);
				MonitorStatisticsUtils.addEvent(ModuleConstant.MODULE_INTERFACE, e);
			}

			// 4. 指令数据解压
			InputStream inputStream = null;
			if (compressionFormat == 1) {
				try {
					inputStream = ZipUtils.unZip2Stream(beianInfoFile);
				} catch (IOException e) {
					logger.error("command xml uncompress fail：" + identify, e);
					MonitorStatisticsUtils.addEvent(ModuleConstant.MODULE_INTERFACE, e);
				}
			} else if (compressionFormat == 0) {
				inputStream = new ByteArrayInputStream(beianInfoFile);
			} else {
				logger.error(
						"nonsupport compression format type, compressionFormat=" + compressionFormat + "：" + identify);
				result.setResultCode(new BigInteger("4"));
				result.setMsg(this.getMsgByCode(4));
				return getUploadReturnString(result);
			}

			if (inputStream == null) {
				logger.error("command xml file stream is empty after uncompress：" + identify);
				result.setResultCode(new BigInteger("4"));
				result.setMsg(this.getMsgByCode(4));
				return getUploadReturnString(result);
			}

			// 解密之后立即备份指令内容
			try {
				writeBackupFile(inputStream, identify);
				inputStream.reset();
			} catch (IOException e) {
				logger.error("reset inputstream fail", e);
				MonitorStatisticsUtils.addEvent(ModuleConstant.MODULE_INTERFACE, e);
			} 
			
			CommandData tmpd = null;
			String type = null;
			// 5. 指令数据校验、解析、提交任务
			try {
				type = XmlUtils.getRootName(inputStream);
				// 5.1 指令格式校验与解析
				tmpd = parseCommandXml(type, inputStream);
				IOUtils.closeQuietly(inputStream); // 关闭流
				
				// 展示收到的数据
				logger.info("receive command  data : " + (tmpd.getCmdObj()==null ? null : tmpd.getCmdObj().toJsonString()));
				if (!StringUtils.isBlank(tmpd.getError())) {
					logger.error("command xml file data format xsd validate fail," + tmpd.getError() + "：" + identify);
					result.setResultCode(new BigInteger("5"));
					result.setMsg(this.getMsgByCode(5));
					return getUploadReturnString(result);
				}
			} catch (Exception e) {
				logger.error("command xml file data read expcetion：" + identify, e);
				result.setResultCode(new BigInteger("5"));
				result.setMsg(this.getMsgByCode(5));
				MonitorStatisticsUtils.addEvent(ModuleConstant.MODULE_INTERFACE, e);
				return getUploadReturnString(result);
			}

			// 5.2 指令写入job队列
			JobQueue jobQueue = new JobQueue();
			CommandBaseVo cmdObj = tmpd.getCmdObj();
			jobQueue.setParams(cmdObj.toJsonString());
			RedisTaskStatus redisTaskStatus = new RedisTaskStatus();
			String commandStrs = "";
			switch (type) {
			case GlobalParams.COMMAND_BASIC_DATA_QUWERY_CN:
				jobQueue.setJobtype(GlobalParams.COMMAND_BASIC_DATA_QUWERY_TYPE);
				commandStrs = (String) com.aotain.common.config.LocalConfig.getInstance()
						.getHashValueByHashKey(GlobalParams.COMMAND_BASIC_DATA_QUWERY_KEY);
				initComandTaskParams(redisTaskStatus, commandStrs);
				break;
			case GlobalParams.COMMAND_LOG_QUERY_CN:
				jobQueue.setJobtype(GlobalParams.COMMAND_LOG_QUERY_TYPE);
				commandStrs = (String) com.aotain.common.config.LocalConfig.getInstance()
						.getHashValueByHashKey(GlobalParams.COMMAND_LOG_QUERY_KEY);
				initComandTaskParams(redisTaskStatus, commandStrs);
				break;
			case GlobalParams.COMMAND_INFO_MANAGE_CN:
				jobQueue.setJobtype(GlobalParams.COMMAND_INFO_MANAGE_TYPE);
				commandStrs = (String) com.aotain.common.config.LocalConfig.getInstance()
						.getHashValueByHashKey(GlobalParams.COMMAND_INFO_MANAGE_KEY);
				initComandTaskParams(redisTaskStatus, commandStrs);
				break;
			case GlobalParams.COMMAND_CODE_RELEASE_CN:
				jobQueue.setJobtype(GlobalParams.COMMAND_CODE_RELEASE_TYPE);
				commandStrs = (String) com.aotain.common.config.LocalConfig.getInstance()
						.getHashValueByHashKey(GlobalParams.COMMAND_CODE_RELEASE_KEY);
				initComandTaskParams(redisTaskStatus, commandStrs);
				break;
			case GlobalParams.COMMAND_BASIC_DATA_VALIDATE_CN:
				jobQueue.setJobtype(GlobalParams.COMMAND_BASIC_DATA_VALIDATE_TYPE);
				commandStrs = (String) com.aotain.common.config.LocalConfig.getInstance()
						.getHashValueByHashKey(GlobalParams.COMMAND_BASIC_DATA_VALIDATE_KEY);
				initComandTaskParams(redisTaskStatus, commandStrs);
				break;
			case GlobalParams.COMMAND_INFO_NO_FILTER_CN:
				jobQueue.setJobtype(GlobalParams.COMMAND_INFO_NO_FILTER_TYPE);
				commandStrs = (String) com.aotain.common.config.LocalConfig.getInstance()
						.getHashValueByHashKey(GlobalParams.COMMAND_INFO_NO_FILTER_KEY);
				initComandTaskParams(redisTaskStatus, commandStrs);
				break;
			case GlobalParams.COMMAND_INFO_BLACK_CN:
				jobQueue.setJobtype(GlobalParams.COMMAND_INFO_BLACK_TYPE);
				commandStrs = (String) com.aotain.common.config.LocalConfig.getInstance()
						.getHashValueByHashKey(GlobalParams.COMMAND_INFO_BLACK_KEY);
				initComandTaskParams(redisTaskStatus, commandStrs);
				break;
			case GlobalParams.COMMAND_INFO_QUERY_VIEW_CN:
				jobQueue.setJobtype(GlobalParams.COMMAND_INFO_QUERY_VIEW_TYPE);
				commandStrs = (String) com.aotain.common.config.LocalConfig.getInstance()
						.getHashValueByHashKey(GlobalParams.COMMAND_INFO_QUERY_VIEW_KEY);
				initComandTaskParams(redisTaskStatus, commandStrs);
				break;
			case GlobalParams.COMMAND_INFO_BLACK_RECORD_CN:
				jobQueue.setJobtype(GlobalParams.COMMAND_INFO_BLACK_RECORD_TYPE);
				commandStrs = (String) com.aotain.common.config.LocalConfig.getInstance()
						.getHashValueByHashKey(GlobalParams.COMMAND_INFO_BLACK_RECORD_KEY);
				initComandTaskParams(redisTaskStatus, commandStrs);
				break;
			}
			try {
				Long taskId = TaskIdUtil.getInstance().getTaskId();
				jobQueue.setTaskid(taskId);
				jobQueue.setToptaskid(taskId);
				jobQueue.setIsretry(0);
				jobQueue.setCreatetime(System.currentTimeMillis()/1000);
				JobQueueUtil.sendMsgToKafkaJobQueue(jobQueue);

				// 5.3 写入redis任务信息hash
				redisTaskStatus.setToptaskid(taskId);
				redisTaskStatus.setTaskid(taskId);
				redisTaskStatus.setTasktype(1); // JOB任务
				redisTaskStatus.setContent(JSON.toJSONString(jobQueue));
				redisTaskStatus.setCreatetime(System.currentTimeMillis()/1000);
				redisTaskStatus.setStatus(1); // 开始
				redisTaskStatus.setTimes(1);  // 从1开始
				TaskMessageUtil.getInstance().setTask(taskId, redisTaskStatus);
			} catch (Exception e) {
				logger.error("write job queue or redis hash fail", e);
				result.setResultCode(new BigInteger("999"));
				result.setMsg(this.getMsgByCode(999));
				MonitorStatisticsUtils.addEvent(ModuleConstant.MODULE_INTERFACE, e);
				return getUploadReturnString(result);
			}

			try {
				logger.info("smms command receive success：" + identify);
				result.setResultCode(new BigInteger("0"));
				result.setMsg(this.getMsgByCode(0));
				return getUploadReturnString(result);
			} finally {
				// 将备份换到接收到文件之后立即就执行
//				try {
//					inputStream.reset();
//					writeBackupFile(inputStream, identify);
//				} catch (IOException e) {
//					logger.error("reset inputstream fail", e);
//					MonitorStatisticsUtils.addEvent(ModuleConstant.MODULE_INTERFACE, e);
//				} finally {
//					IOUtils.closeQuietly(inputStream);
//				}
			}
		} catch (Exception e) {
			logger.error("service exception：" + identify, e);
			result.setResultCode(new BigInteger("999"));
			result.setMsg(this.getMsgByCode(999));
			String msg = getUploadReturnString(result);
			MonitorStatisticsUtils.addEvent(ModuleConstant.MODULE_INTERFACE, e);
			return msg;
		}
	}

	/**
	 * 构造RedisTaskStatus
	 * 
	 * @param redisTaskStatus
	 * @param commandStrs
	 */
	private void initComandTaskParams(RedisTaskStatus redisTaskStatus, String commandStrs) {
		String[] params = commandStrs.split(",");
		redisTaskStatus.setMaxtimes(Integer.parseInt(params[0]));
		redisTaskStatus.setExpiretime(Long.parseLong(params[1]));
		redisTaskStatus.setInterval(Integer.parseInt(params[2]));
		redisTaskStatus.setNexttime(System.currentTimeMillis() / 1000 + redisTaskStatus.getInterval());
	}

	/**
	 * 解析指令内容，并校验指令格式是否合法
	 * 
	 * @param type
	 * @param inputStream
	 * @return
	 */
	private CommandData parseCommandXml(String type, InputStream inputStream) {
		CommandData cdata = new CommandData();
		try {
			String f = (String) LocalConfig.getInstance().getHashValueByHashKey("xsd_enabled");
			boolean xsdEnabled = false; // 默认不需要校验
			if(!StringUtils.isBlank(f)){
				try{
					xsdEnabled = Boolean.valueOf(f);
				}catch(Exception e){
					logger.warn("xsd_enabled params invalid : " + f,e);
				}
			}else{
				logger.warn("xsd_enabled params is empty ");
			}
			// 开启校验开关时，需要校验，否则不做校验
			if(xsdEnabled) {
				String error = CommandFileXsdValidator.getInstance().doValidate(type, inputStream);
				if (!StringUtils.isBlank(error)) {
					cdata.setError(error);
					// 校验发生错误时，还是需要对xsd进行解析并返回，因此不直接返回
					//  return cdata; 
				}
			}
			switch (type) {
			case GlobalParams.XFT_BLACK_LIST:
				Blacklist blacklist = XmlUtils.createBean(inputStream, Blacklist.class);
				cdata.setCmdObj(blacklist);
				break;
			case GlobalParams.XFT_CODE_LIST:
				CodeList codeList = XmlUtils.createBean(inputStream, CodeList.class);
				cdata.setCmdObj(codeList);
				break;
			case GlobalParams.XFT_IDC_COMMAND:
				Command command = XmlUtils.createBean(inputStream, Command.class);
				cdata.setCmdObj(command);
				break;
			case GlobalParams.XFT_IDC_INFO_MANAGE:
				IdcInfoManage idcInfoMng = XmlUtils.createBean(inputStream, IdcInfoManage.class);
				cdata.setCmdObj(idcInfoMng);
				break;
			case GlobalParams.XFT_LOG_QUERY:
				LogQuery logquery = XmlUtils.createBean(inputStream, LogQuery.class);
				cdata.setCmdObj(logquery);
				break;
			case GlobalParams.XFT_NO_FILTER:
				NoFilter nofilter = XmlUtils.createBean(inputStream, NoFilter.class);
				cdata.setCmdObj(nofilter);
				break;
			case GlobalParams.XFT_RETURN_INFO:
				ReturnInfo returnInfo = XmlUtils.createBean(inputStream, ReturnInfo.class);
				cdata.setCmdObj(returnInfo);
				break;
			case GlobalParams.XFT_QUERY_VIEW:
				QueryView queryView = XmlUtils.createBean(inputStream, QueryView.class);
				cdata.setCmdObj(queryView);
				break;
			case GlobalParams.XFT_COMMAND_RECORD:
				CommandRecord commandRecord = XmlUtils.createBean(inputStream, CommandRecord.class);
				cdata.setCmdObj(commandRecord);
				break;
			default:
				cdata.setError("nonsupport command type：" + type);
				break;
			}
		} catch (Exception e) {
			cdata.setError("xsd validate exception：" + e.getMessage());
			logger.error("xsd validate exception", e);
			MonitorStatisticsUtils.addEvent(ModuleConstant.MODULE_INTERFACE, e);
		}
		return cdata;
	}

	/**
	 * 写备份文件
	 * 
	 * @param istream
	 * @param identify
	 */
	private void writeBackupFile(InputStream istream, String identify) {
		String filePath = (String) LocalConfig.getInstance().getHashValueByHashKey(GlobalParams.DOWNLOAD_FILE_BAK_PATH);
		String fileName = Tools.getBatchId() + "";
		String resultFileName = filePath + fileName + ".tmp";
		File file  = new File(resultFileName);
		
		FileOutputStream outStream = null;
		try {
			if(!file.getParentFile().exists()){
				file.getParentFile().mkdirs(); // 目录不存在时，自动创建
			}
			outStream = new FileOutputStream(resultFileName);
			// 写入数据
			outStream.write(IOUtils.toByteArray(istream));
			outStream.flush();
			outStream.close();
			// 完成后修改文件名
			if (FileUtils.renameFile(resultFileName, resultFileName.replace(".tmp", ".xml"))) {
				logger.info("command xml file backup success：" + resultFileName + "," + identify);
			}else{
				logger.info("command xml file backup rename .tmp to .xml fail：" + resultFileName + "," + identify);
			}
		} catch (Exception ex) {
			logger.error("command xml file backup exception：" + resultFileName + "," + identify, ex);
			MonitorStatisticsUtils.addEvent(ModuleConstant.MODULE_INTERFACE, ex);
		} finally {
			IOUtils.closeQuietly(outStream);
			IOUtils.closeQuietly(istream);
		}
	}

	/**
	 * 构造返回结果
	 * 
	 * @param result
	 * @return
	 */
	private String getUploadReturnString(Return result) {
		String tmp = "";
		String wsVersion = "v2.0";
		try {
			wsVersion = (String) LocalConfig.getInstance().getHashValueByHashKey(GlobalParams.VERSION);
		} catch (Exception e) {
			logger.error("ws version get exception,use default version : v2.0", e);
			MonitorStatisticsUtils.addEvent(ModuleConstant.MODULE_INTERFACE, e);
		}
		try {
			result.setVersion(wsVersion);
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			JAXBContext context = JAXBContext.newInstance(Return.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.marshal(result, buffer);
			tmp = buffer.toString("UTF-8");
			logger.info("return message after receive smms command：" + tmp);
		} catch (Exception ex) {
			logger.debug("IdcCommand Return error", ex);
			MonitorStatisticsUtils.addEvent(ModuleConstant.MODULE_INTERFACE, ex);
		}
		return tmp;
	}
	
}
