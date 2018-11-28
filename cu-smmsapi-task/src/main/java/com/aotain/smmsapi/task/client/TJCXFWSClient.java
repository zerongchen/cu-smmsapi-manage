package com.aotain.smmsapi.task.client;

import java.io.StringReader;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aotain.common.utils.monitorstatistics.ModuleConstant;
import com.aotain.common.utils.tools.MonitorStatisticsUtils;
import com.aotain.smmsapi.task.QuartzMain;
import com.aotain.smmsapi.task.bean.ObjectFactory;
import com.aotain.smmsapi.task.bean.Return;
import com.aotain.smmsapi.task.client.cxf.tj.Commandack;
import com.aotain.smmsapi.task.client.cxf.tj.CommandackPortType;
import com.aotain.smmsapi.task.client.cxf1.IDCWebService_Service;
import com.aotain.smmsapi.task.client.cxf1.IdcWebService;
import com.aotain.smmsapi.task.constant.GlobalParams;
import com.aotain.smmsapi.task.utils.LocalConfig;

public class TJCXFWSClient extends BaseWSClient<TJCXFWSClient, Commandack> {
	private static Logger logger = LoggerFactory.getLogger(TJCXFWSClient.class);
	public TJCXFWSClient(String idcId, byte[] data) {
		super(idcId, data);
	}

	@Override
	public Commandack toIdcCommandack(TJCXFWSClient t) {
		return null;
	}

	@Override
	public Return callService(TJCXFWSClient t) {
		ObjectFactory factory = new ObjectFactory();
		Return result = factory.createReturn();
		String smmsVersion 		= (String)com.aotain.common.config.LocalConfig.getInstance().getHashValueByHashKey(GlobalParams.VERSION);
		result.setVersion(smmsVersion);
		try {
			//调用ISMI接口下发指令
			URL url = new URL(t.getServiceUrl());
			Commandack commandack = new Commandack(url);
			CommandackPortType client = commandack.getCommandackHttpPort();
			String response = client.idcCommandack(
					t.getIdcId(), 
					t.getRandVal(),
					t.getPwdHash(), 
					t.getResult(), 
					t.getResultHash(), 
					t.getEncryptAlgorithm(), 
					t.getHashAlgorithm(), 
					t.getCommpresssionFormat(), 
					t.getCommandVersion());

			JAXBContext context = JAXBContext.newInstance(Return.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			logger.info("\n" + response);
			result = (Return) unmarshaller.unmarshal(new StringReader(response));
		} catch (Exception e) {
			result = callServiceNew(t);
		}
		return result;
	}
	
	public Return callServiceNew(TJCXFWSClient t) {
		ObjectFactory factory = new ObjectFactory();
		Return result = factory.createReturn();
		String smmsVersion 		= (String)com.aotain.common.config.LocalConfig.getInstance().getHashValueByHashKey(GlobalParams.VERSION);
		result.setVersion(smmsVersion);
		try {
			//调用ISMI接口下发指令
			URL url = new URL(t.getServiceUrl());
			IDCWebService_Service service = new IDCWebService_Service(url);
	        IdcWebService client = service.getIdcWebServicePort();
			String response = client.idcCommandack(
					t.getIdcId(), 
					t.getRandVal(),
					t.getPwdHash(), 
					t.getResult(), 
					t.getResultHash(), 
					t.getEncryptAlgorithm(), 
					t.getHashAlgorithm(), 
					t.getCommpresssionFormat(), 
					t.getCommandVersion());

			JAXBContext context = JAXBContext.newInstance(Return.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			logger.info("\n" + response);
			result = (Return) unmarshaller.unmarshal(new StringReader(response));
		} catch (Exception e) {
			logger.error("["+ t.getIdcId() +"]callService:", e);
			result.setResultCode(new BigInteger("10000"));
			result.setMsg(e.toString());
			MonitorStatisticsUtils.addEvent(ModuleConstant.MODULE_SMMSAPI_TASK_ACK, e);
		} 
		return result;
	}
	
	public static void main(String[] args) {
		ObjectFactory factory = new ObjectFactory();
		Return result = factory.createReturn();
		String smmsVersion 		= (String)com.aotain.common.config.LocalConfig.getInstance().getHashValueByHashKey(GlobalParams.VERSION);
		result.setVersion(smmsVersion);
		URL url = null;
		try {
			//调用ISMI接口下发指令
			url =  new URL("http://60.29.111.230:8002/IDCWebService_smms/commandack?wsdl");
			Commandack commandack = new Commandack(url);
			CommandackPortType client = commandack.getCommandackHttpPort();
			String response = client.idcCommandack(
					"", 
					"",
					"", 
					"", 
					"", 
					0, 
					0, 
					0, 
					"");

			JAXBContext context = JAXBContext.newInstance(Return.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			logger.info("\n" + response);
			result = (Return) unmarshaller.unmarshal(new StringReader(response));
		} catch (Exception e) {
			try {
				//调用ISMI接口下发指令
				IDCWebService_Service service = new IDCWebService_Service(url);
		        IdcWebService client = service.getIdcWebServicePort();
				String response = client.idcCommandack(
						"", 
						"",
						"", 
						"", 
						"", 
						0, 
						0, 
						0, 
						"");

				JAXBContext context = JAXBContext.newInstance(Return.class);
				Unmarshaller unmarshaller = context.createUnmarshaller();
				logger.info("\n" + response);
				result = (Return) unmarshaller.unmarshal(new StringReader(response));
			} catch (JAXBException e1) {
				e.printStackTrace();
			}
		}
	}
	
}
