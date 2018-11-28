package com.aotain.smmsapi.task.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.aotain.common.utils.monitorstatistics.ModuleConstant;
import com.aotain.common.utils.tools.AES;
import com.aotain.common.utils.tools.MD5;
import com.aotain.common.utils.tools.MonitorStatisticsUtils;
import com.aotain.common.utils.tools.Random;
import com.aotain.common.utils.tools.SHA1;
import com.aotain.smmsapi.task.QuartzMain;
import com.aotain.smmsapi.task.bean.Return;
import com.aotain.smmsapi.task.constant.GlobalParams;
import com.aotain.smmsapi.task.utils.StringUtil;
import com.jcraft.jsch.Logger;

import sun.misc.BASE64Encoder;

public abstract class BaseWSClient<T, IdcCommandack> {

	private String serviceUrl 		= (String)com.aotain.common.config.LocalConfig.getInstance().getHashValueByHashKey(GlobalParams.SERVER_URL);
//	private String serviceUrl 		= "http://218.26.33.179:7070/ism_product/ws/commandack?wsdl";
	private String password 		= (String)com.aotain.common.config.LocalConfig.getInstance().getHashValueByHashKey(GlobalParams.PASSWORD);
	private String aesKey 			= (String)com.aotain.common.config.LocalConfig.getInstance().getHashValueByHashKey(GlobalParams.AES_KEY);
	private String aesIv 			= (String)com.aotain.common.config.LocalConfig.getInstance().getHashValueByHashKey(GlobalParams.AES_IV);
	private String rzKey 			= (String)com.aotain.common.config.LocalConfig.getInstance().getHashValueByHashKey(GlobalParams.RZ_KEY);
	
	private String idcId;
	private String randVal			= Random.getRandomString();
	private String pwdHash;
	private String result;
	private String resultHash;
	private int encryptAlgorithm 	= GlobalParams.ENCRYPT_ALGORITHM_AES;
	private int hashAlgorithm 		= GlobalParams.HASH_ALGORITHM_MD5;
	private int commpresssionFormat	= GlobalParams.COMPRESSION_FORMAT_ZIP;
	private String commandVersion 	= (String)com.aotain.common.config.LocalConfig.getInstance().getHashValueByHashKey(GlobalParams.VERSION);
	private String uploadWSFilePath = (String)com.aotain.common.config.LocalConfig.getInstance().getHashValueByHashKey(GlobalParams.UPLOAD_WS_FILE_PATH);
	private byte[] data;
	
	public BaseWSClient(String idcId, byte[] data) {
		super();
		this.idcId = idcId;
		this.data = data;
		// 调用封装数据的方法
		this.encapsulateData();
	}

	private void encapsulateData() {
		try {
			this.pwdHash = this.password + this.randVal;
			if (this.hashAlgorithm == GlobalParams.HASH_ALGORITHM_MD5) {
				this.pwdHash = MD5.Encrypt2(this.pwdHash.getBytes());
			} else if (this.hashAlgorithm == GlobalParams.HASH_ALGORITHM_SHA) {
				SHA1 sha1 = new SHA1();
				String byteData = this.password + this.randVal;
				this.pwdHash = sha1.getDigestOfbase64(byteData.getBytes());
			}
			if (this.encryptAlgorithm == GlobalParams.ENCRYPT_ALGORITHM_AES) {
				this.result = AES.Encrypt(data, this.aesKey, this.aesIv);
			} else {
				this.result = new BASE64Encoder().encode(data);
			}
			byte[] resultBytes = StringUtil.joinBytes(data, this.rzKey.getBytes("UTF-8"));
			String resultHash = resultBytes.toString();
			if (this.hashAlgorithm == GlobalParams.HASH_ALGORITHM_MD5) {
				resultHash = MD5.Encrypt2(resultBytes);
			} else if (this.hashAlgorithm == GlobalParams.HASH_ALGORITHM_SHA) {
				SHA1 sha1 = new SHA1();
				resultHash = sha1.getDigestOfbase64(resultBytes);
			}
			this.resultHash = resultHash;
		} catch (Exception e){
			e.printStackTrace();
			MonitorStatisticsUtils.addEvent(ModuleConstant.MODULE_SMMSAPI_TASK_ACK, e);
        }
	}
	
	public abstract IdcCommandack toIdcCommandack(T t);
	
	public abstract Return callService(T t);

	private byte[] readFileReturnByte(String fileFullName) {
		byte[] result = null;
		BufferedInputStream in = null;
		ByteArrayOutputStream out = null;
		try {
			in = new BufferedInputStream(new FileInputStream(fileFullName));
			out = new ByteArrayOutputStream();
			byte[] temp = new byte[1024];
			int size = 0;
			while ((size = in.read(temp)) != -1) {
				out.write(temp, 0, size);
			}
			result = out.toByteArray();
			out.close();
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public String getServiceUrl() {
		return serviceUrl;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAesKey() {
		return aesKey;
	}

	public void setAesKey(String aesKey) {
		this.aesKey = aesKey;
	}

	public String getAesIv() {
		return aesIv;
	}

	public void setAesIv(String aesIv) {
		this.aesIv = aesIv;
	}

	public String getRzKey() {
		return rzKey;
	}

	public void setRzKey(String rzKey) {
		this.rzKey = rzKey;
	}

	public String getIdcId() {
		return idcId;
	}

	public void setIdcId(String idcId) {
		this.idcId = idcId;
	}

	public String getRandVal() {
		return randVal;
	}

	public void setRandVal(String randVal) {
		this.randVal = randVal;
	}

	public String getPwdHash() {
		return pwdHash;
	}

	public void setPwdHash(String pwdHash) {
		this.pwdHash = pwdHash;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getResultHash() {
		return resultHash;
	}

	public void setResultHash(String resultHash) {
		this.resultHash = resultHash;
	}

	public int getEncryptAlgorithm() {
		return encryptAlgorithm;
	}

	public void setEncryptAlgorithm(int encryptAlgorithm) {
		this.encryptAlgorithm = encryptAlgorithm;
	}

	public int getHashAlgorithm() {
		return hashAlgorithm;
	}

	public void setHashAlgorithm(int hashAlgorithm) {
		this.hashAlgorithm = hashAlgorithm;
	}

	public int getCommpresssionFormat() {
		return commpresssionFormat;
	}

	public void setCommpresssionFormat(int commpresssionFormat) {
		this.commpresssionFormat = commpresssionFormat;
	}

	public String getCommandVersion() {
		return commandVersion;
	}

	public void setCommandVersion(String commandVersion) {
		this.commandVersion = commandVersion;
	}

	public String getUploadWSFilePath() {
		return uploadWSFilePath;
	}

	public void setUploadWSFilePath(String uploadWSFilePath) {
		this.uploadWSFilePath = uploadWSFilePath;
	}

}
