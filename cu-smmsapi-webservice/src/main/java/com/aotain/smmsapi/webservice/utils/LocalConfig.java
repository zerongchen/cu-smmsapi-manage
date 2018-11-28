package com.aotain.smmsapi.webservice.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aotain.smmsapi.webservice.constant.GlobalParams;

public class LocalConfig {
	private Logger logger = LoggerFactory.getLogger(LocalConfig.class);
	private String dbDriver;
	private String dbURL;
	private String dbUser;
	private String dbPassword;
	private String wsPassword;
	private String wsVersion;
	private String aesKey;
	private String aesIv;
	private String rzKey;
	
	private String smmsUrl;
	private String smmsPassword;
	private String smmsVersion;
	private String smmsAesKey;
	private String smmsAesIv;
	private String smmsRzKey;
	private Integer clientType;
	
	private String smmsFtpIp;
	private String smmsFtpPort;
	private String smmsFtpUser;
	private String smmsFtpPwd;
	private String smmsFtpWorkpath;
	
	private Integer base64Transcode;
	
	private String hadoopDriver;
	private String hadoopUrl;
	private String hadoopUser;
	private String hadoopPwd;
	private String hadoopMultipleUrl;
	private String multipleHadoop;
	private String hiveDriver;
	private String hiveUrl;
	private String hiveUser;
	private String hivePwd;
	private String uploadLimitNum;
	private String uploadFilePath;
	private String uploadFileBakPath;
	private String uploadZipFilePath;
	private String uploadWsFilePath;
	private String uploadWsFileBakPath;
	private String uploadWsZipFilePath;
	private String downloadFilePath;
	private String downloadFileBakPath;
	private String downloadZipFilePath;
	private String attachmentDataPath;
	
	private String requestUrl;
	private String resourceMonitor;
	private String redisFlag;
	
	private Long baseUnitId;
	private String monitorFilterResultPath;
	private String monitorFilterBakPath;
	private Integer timeSplitInterval;
	private String impalaDatabase;
	private String logNum;//日志记录行数
	private String monitorItemFilePath; 
	private String eventItemFilePath;
	private String exportFilePath;
	
	private Map<String, String> dbMap = new HashMap<String, String>();
	
	private static LocalConfig instance;
	
	public synchronized static LocalConfig getInstance() {
		if (instance == null) {
			instance = new LocalConfig(); 
		}
		return instance;
	}
	
	public synchronized static void removeInstance() {
		if (instance != null) {
			instance = null;
			getInstance();
		}
	}
	
	private LocalConfig(){
		ResourceBundle config = ResourceBundle.getBundle("config");
		dbDriver = config.getString("isms.driver");
		dbURL = config.getString("isms.url");
		dbUser = config.getString("isms.user");
		dbPassword = config.getString("isms.pwd");
		
		encapsulateData(config); 
	}
	
	private void encapsulateData(ResourceBundle config) {
		obtainData();
		obtainValue(config);
	}
	
	private void obtainValue(ResourceBundle config) {
		wsPassword = dbMap.get(GlobalParams.PASSWORD);
		wsVersion = dbMap.get(GlobalParams.VERSION);
		aesKey = dbMap.get(GlobalParams.AES_KEY);
		aesIv = dbMap.get(GlobalParams.AES_IV);
		rzKey = dbMap.get(GlobalParams.RZ_KEY);
		
		smmsUrl = dbMap.get(GlobalParams.SERVER_URL);
		smmsPassword = dbMap.get(GlobalParams.PASSWORD);
		smmsVersion = dbMap.get(GlobalParams.VERSION);
		smmsAesKey = dbMap.get(GlobalParams.AES_KEY);
		smmsAesIv = dbMap.get(GlobalParams.AES_IV);
		smmsRzKey = dbMap.get(GlobalParams.RZ_KEY);
		clientType = Integer.valueOf(dbMap.get(GlobalParams.CLIENT_VERSION));
		
		smmsFtpIp = dbMap.get(GlobalParams.FTP_IP);
		smmsFtpPort = dbMap.get(GlobalParams.FTP_PORT);
		smmsFtpUser = dbMap.get(GlobalParams.FTP_USER);
		smmsFtpPwd = dbMap.get(GlobalParams.FTP_PASSWORD);
		smmsFtpWorkpath = dbMap.get(GlobalParams.FTP_PATH);
		
		base64Transcode = Integer.parseInt(dbMap.get(GlobalParams.BASE64_TRANSCODE));
		
		hadoopDriver = dbMap.get(GlobalParams.HADOOP_DRIVER);
		hadoopUrl = dbMap.get(GlobalParams.HADOOP_URL);
		hadoopUser = dbMap.get(GlobalParams.HADOOP_USER);
		hadoopPwd = dbMap.get(GlobalParams.HADOOP_PASSWORD);
		hadoopMultipleUrl = dbMap.get(GlobalParams.HADOOP_MULTIPLE_URL);
		multipleHadoop = dbMap.get(GlobalParams.MULTIPLE_ENABLED);
		uploadLimitNum= dbMap.get(GlobalParams.LIMIT_NUM);
		hiveDriver = dbMap.get(GlobalParams.HIVE_DRIVER);
		hiveUrl = dbMap.get(GlobalParams.HIVE_URL);
		hiveUser = dbMap.get(GlobalParams.HIVE_USER);
		hivePwd = dbMap.get(GlobalParams.HIVE_PASSWORD);
		multipleHadoop = dbMap.get(GlobalParams.MULTIPLE_ENABLED);
		uploadLimitNum= dbMap.get(GlobalParams.LIMIT_NUM);
		
		uploadFilePath = dbMap.get(GlobalParams.UPLOAD_FILE_PATH);
		uploadFileBakPath = dbMap.get(GlobalParams.UPLOAD_FILE_BAK_PATH);
		uploadZipFilePath = dbMap.get(GlobalParams.UPLOAD_ZIP_FILE_PATH);		
		uploadWsFilePath = dbMap.get(GlobalParams.UPLOAD_WS_FILE_PATH);
		uploadWsFileBakPath = dbMap.get(GlobalParams.UPLOAD_WS_FILE_BAK_PATH);
		uploadWsZipFilePath = dbMap.get(GlobalParams.UPLOAD_WS_ZIP_FILE_PATH);
		downloadFilePath = dbMap.get(GlobalParams.DOWNLOAD_FILE_PATH);
		downloadFileBakPath = dbMap.get(GlobalParams.DOWNLOAD_FILE_BAK_PATH);
		downloadZipFilePath = dbMap.get(GlobalParams.DOWNLOAD_ZIP_FILE_PATH);
		attachmentDataPath = dbMap.get(GlobalParams.ATTACHMENT_DATA_PATH);
		
		logNum = dbMap.get(GlobalParams.HADOOP_LOGNUM);
		
		monitorItemFilePath = dbMap.get(GlobalParams.MONITORITEM_FILE_PATH);
		eventItemFilePath = dbMap.get(GlobalParams.EVENTITEM_FILE_PATH); 
		
		exportFilePath = dbMap.get(GlobalParams.EXPORT_FILE_PATH);
		
		String tempResourceMonitor = null;
		String tempSmmsRequestUrl = null;
		Long tempBaseUnitId = null;
		String tempRedisFlag = null;
		try {
			tempResourceMonitor = config.getString("resourceMonitorFlag");
			tempSmmsRequestUrl = dbMap.get(GlobalParams.SMMS_REQUEST_URL);
			tempBaseUnitId = Long.valueOf(dbMap.get(GlobalParams.TIANJIN_BASE_UNIT_ID));
			tempRedisFlag = config.getString("redisFlag");
		} catch (Exception e) {
		}
		resourceMonitor = tempResourceMonitor;
		requestUrl = tempSmmsRequestUrl;
		baseUnitId = tempBaseUnitId == null ? 0 : tempBaseUnitId;
		redisFlag = tempRedisFlag;
		
		monitorFilterResultPath = dbMap.get(GlobalParams.MONITOR_FILTER_RESULT_PATH);
		monitorFilterBakPath = dbMap.get(GlobalParams.MONITOR_FILTER_BAK_PATH);
		timeSplitInterval = Integer.valueOf(dbMap.get(GlobalParams.TIME_SPLIT_INTERVAL));
		impalaDatabase = dbMap.get(GlobalParams.IMPALA_DATABASE);
	}

	public static void main(String[] args) {
		System.out.println(LocalConfig.getInstance().getClientType());
	}
	
	private void obtainData() {
		Connection conn = null;
		Statement stat = null;
		ResultSet rset = null;
		try {
			conn = getConnection();
			String sql = "select * from idc_jcdm_jkcs t where t.sfyx = 1 and t.model_type = 1 or t.cs_key like '%hive%' or t.cs_key = 'export_filePath'";
			stat = conn.createStatement();
			rset = stat.executeQuery(sql);
			while (rset.next()) {
				String key = StringUtil.trimAll(rset.getString("CS_KEY"));
				String value = StringUtil.trimAll(rset.getString("CS_VALUE"));
				String type = rset.getString("TYPE");
				if (!StringUtil.isEmptyString(type)) {
					dbMap.put(type, key);
				} else {
					dbMap.put(key, value);
				}
			}
		} catch (Exception e) {
			logger.error(e.toString());
		} finally {
			DBUtil.closeConnection(conn, stat, rset);
		}
	}

	/*private LocalConfig(){
		ResourceBundle config = ResourceBundle.getBundle("config");
		dbDriver = config.getString("isms.driver");
		dbURL = config.getString("isms.url");
		dbUser = config.getString("isms.user");
		dbPassword = config.getString("isms.pwd");
		wsPassword = config.getString("isms.ws.pwd");
		wsVersion = config.getString("isms.ws.version");
		aesKey = config.getString("isms.aes.key");
		aesIv = config.getString("isms.aes.iv");
		rzKey = config.getString("isms.ws.rzkey");
		
		smmsUrl = config.getString("smms.ws.url");
		smmsPassword = config.getString("smms.ws.pwd");
		smmsVersion = config.getString("smms.ws.version");
		smmsAesKey = config.getString("smms.aes.key");
		smmsAesIv = config.getString("smms.aes.iv");
		smmsRzKey = config.getString("smms.ws.rzkey");
		clientType = Integer.valueOf(config.getString("smms.ws.client.version"));
		
		smmsFtpIp = config.getString("smms.ftp.ip");
		smmsFtpPort = config.getString("smms.ftp.port");
		smmsFtpUser = config.getString("smms.ftp.user");
		smmsFtpPwd = config.getString("smms.ftp.pwd");
		smmsFtpWorkpath = config.getString("smms.ftp.workpath");
		
		base64Transcode = Integer.parseInt(config.getString("accessLog.url.base64Transcode"));
		
		hadoopDriver = config.getString("hadoop.driver");
		hadoopUrl = config.getString("hadoop.url");
		hadoopUser = config.getString("hadoop.username");
		hadoopPwd = config.getString("hadoop.password");
		hadoopMultipleUrl = config.getString("hadoop.multiple.url");
		multipleHadoop = config.getString("multiple.enabled");
		uploadLimitNum= config.getString("upload.limit.num");
		
		uploadFilePath = config.getString("dams.uploadFilePath");
		uploadFileBakPath = config.getString("dams.uploadFileBakPath");
		uploadZipFilePath = config.getString("dams.uploadZipFilePath");
		uploadWsFilePath = config.getString("dams.uploadWsFilePath");
		uploadWsFileBakPath = config.getString("dams.uploadWsFileBakPath");
		uploadWsZipFilePath = config.getString("dams.uploadWsZipFilePath");
		downloadFilePath = config.getString("dams.downloadFilePath");
		downloadFileBakPath = config.getString("dams.downloadFileBakPath");
		downloadZipFilePath = config.getString("dams.downloadZipFilePath");
		attachmentDataPath = config.getString("dams.attachmentDataPath");
		
		String tempResourceMonitor = null;
		String tempSmmsRequestUrl = null;
		Long tempBaseUnitId = null;
		try {
			tempResourceMonitor = config.getString("resourceMonitorFlag");
			tempSmmsRequestUrl = config.getString("smms.requestUrl");
			tempBaseUnitId = Long.valueOf(config.getString("tianjin.baseUnitId"));
		} catch (Exception e) {
		}
		resourceMonitor = tempResourceMonitor;
		requestUrl = tempSmmsRequestUrl;
		baseUnitId = tempBaseUnitId == null ? 0 : tempBaseUnitId;
		
		monitorFilterResultPath = config.getString("dams.monitorFilterResultPath");
		monitorFilterBakPath = config.getString("dams.monitorFilterBakPath");
		timeSplitInterval = Integer.valueOf(config.getString("dams.timeSplitInterval"));
	}*/
	
	private Connection getConnection() { 
		Connection conn = null;
		try {
			Class.forName(dbDriver);
			conn = DriverManager.getConnection(dbURL, dbUser, dbPassword);
		} catch (ClassNotFoundException e) {
			logger.error("Not find Driver");
		} catch (SQLException e) {
			logger.error("Connect error");
		}
		return conn;
	}
	
	
	
	public String getLogNum() {
		return logNum;
	}

	public String getRzKey() {
		return rzKey;
	}

	public String getSmmsFtpIp() {
		return smmsFtpIp;
	}

	public String getSmmsFtpPort() {
		return smmsFtpPort;
	}

	public String getSmmsFtpUser() {
		return smmsFtpUser;
	}

	public String getSmmsFtpPwd() {
		return smmsFtpPwd;
	}

	public String getSmmsFtpWorkpath() {
		return smmsFtpWorkpath;
	}
	
	public String getSmmsUrl() {
		return smmsUrl;
	}
	
	public String getSmmsPassword() {
		return smmsPassword;
	}

	public String getSmmsVersion() {
		return smmsVersion;
	}

	public String getSmmsAesKey() {
		return smmsAesKey;
	}

	public String getSmmsAesIv() {
		return smmsAesIv;
	}

	public String getSmmsRzKey() {
		return smmsRzKey;
	}

	public Integer getClientType() {
		return clientType;
	}

	public String getUploadWsFilePath() {
		return uploadWsFilePath;
	}

	public String getUploadWsFileBakPath() {
		return uploadWsFileBakPath;
	}

	public String getUploadWsZipFilePath() {
		return uploadWsZipFilePath;
	}

	public String getUploadLimitNum() {
		return uploadLimitNum;
	}
	
	public String getHadoopDriver() {
		return hadoopDriver;
	}

	public String getHadoopUrl() {
		return hadoopUrl;
	}

	public String getHadoopUser() {
		return hadoopUser;
	}

	public String getHadoopPwd() {
		return hadoopPwd;
	}

	public String getHadoopMultipleUrl() {
		return hadoopMultipleUrl;
	}

	public String getMultipleHadoop() {
		return multipleHadoop;
	}
	
	public String getHiveDriver() {
		return hiveDriver;
	}

	public String getHiveUrl() {
		return hiveUrl;
	}

	public String getHiveUser() {
		return hiveUser;
	}

	public String getHivePwd() {
		return hivePwd;
	}

	public String getDbDriver() {
		return dbDriver;
	}

	public String getDbURL() {
		return dbURL;
	}

	public String getDbUser() {
		return dbUser;
	}

	public String getDbPassword() {
		return dbPassword;
	}
	
	public String getWsPassword() {
		return wsPassword;
	}
	
	public String getAesIv() {
		return aesIv;
	}
	
	public String getAesKey() {
		return aesKey;
	}
	
	public String getRzkey() {
		return rzKey;
	}
	
	public String getWsVersion() {
		return wsVersion;
	}

	public String getUploadFilePath() {
		return uploadFilePath;
	}

	public String getUploadFileBakPath() {
		return uploadFileBakPath;
	}
	
	public String getUploadZipFilePath() {
		return uploadZipFilePath;
	}

	public String getDownloadFilePath() {
		return downloadFilePath;
	}
	
	public String getDownloadFileBakPath() {
		return downloadFileBakPath;
	}

	public String getDownloadZipFilePath() {
		return downloadZipFilePath;
	}
	
	public String getAttachmentDataPath() {
		return attachmentDataPath;
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public String getResourceMonitor() {
		return resourceMonitor;
	}
	
	public String getRedisFlag() {
		return redisFlag;
	}

	public Long getBaseUnitId() {
		return baseUnitId;
	}

	public String getMonitorFilterResultPath() {
		return monitorFilterResultPath;
	}

	public String getMonitorFilterBakPath() {
		return monitorFilterBakPath;
	}

	public Integer getTimeSplitInterval() {
		return timeSplitInterval;
	}

	public Integer getBase64Transcode() {
		return base64Transcode;
	}

	public String getImpalaDatabase() {
		return impalaDatabase;
	}

	public Map<String, String> getDbMap() {
		return dbMap;
	}

	public String getMonitorItemFilePath() {
		return monitorItemFilePath;
	}

	public String getEventItemFilePath() {
		return eventItemFilePath;
	}

	public String getExportFilePath() {
		return exportFilePath;
	}
	
}
