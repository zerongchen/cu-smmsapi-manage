package com.aotain.smmsapi.task.client;

import java.io.StringReader;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aotain.common.utils.monitorstatistics.ModuleConstant;
import com.aotain.common.utils.tools.MonitorStatisticsUtils;
import com.aotain.smmsapi.task.QuartzMain;
import com.aotain.smmsapi.task.bean.ObjectFactory;
import com.aotain.smmsapi.task.bean.Return;
import com.aotain.smmsapi.task.client.cxf.Commandack;
import com.aotain.smmsapi.task.client.cxf.CommandackPortType;
import com.aotain.smmsapi.task.client.cxf1.IDCWebService_Service;
import com.aotain.smmsapi.task.client.cxf1.IdcWebService;
import com.aotain.smmsapi.task.constant.GlobalParams;
import com.aotain.smmsapi.task.utils.LocalConfig;

public class CXFWSClient extends BaseWSClient<CXFWSClient, Commandack> {
	private static Logger logger = LoggerFactory.getLogger(CXFWSClient.class);
	
	public CXFWSClient(String idcId, byte[] data) {
		super(idcId, data);
	}

	@Override
	public Commandack toIdcCommandack(CXFWSClient t) {
		return null;
	}

	@Override
	public Return callService(CXFWSClient t) {
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
	
	public Return callServiceNew(CXFWSClient t) {
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
	
	private static void test() {
		ObjectFactory factory = new ObjectFactory();
		Return result = factory.createReturn();
		try {
			//调用ISMI接口下发指令
			URL url = new URL("http://218.26.33.179:7070/ism_product/ws/commandack?wsdl");
			QName qName = new QName("http://webservice.ismi.surfilter.com/", "commandack");
			Commandack commandack = new Commandack(url);//, qName);
			CommandackPortType client = commandack.getCommandackHttpPort();
			
			/*URL url = new URL("http://183.136.190.57:7080/ism/ws/commandack?wsdl");
			IDCWebService_Service service = new IDCWebService_Service(url);
	        IdcWebService client = service.getIdcWebServicePort();*/
			
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
			System.out.println("haha");
			//e.printStackTrace();
			ObjectFactory factory1 = new ObjectFactory();
			Return result1 = factory.createReturn();
			try {
				//调用ISMI接口下发指令
				/*URL url = new URL("http://183.136.190.57:7080/ism/ws/commandack?wsdl");
				QName qName = new QName("http://webservice.ismi.surfilter.com/", "commandack");
				Commandack commandack = new Commandack(url);//, qName);
				CommandackPortType client = commandack.getCommandackHttpPort();*/
				
				URL url = new URL("http://218.26.33.179:7070/ism_product/ws/commandack?wsdl");
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
			} catch (Exception e1) {
			}
		}
	}
	
	private static void testAxis2Webservice() {
		try {
			// axis2 服务端
			String url = "http://218.26.33.179:7070/ism_product/ws/commandack?wsdl";
			// 使用RPC方式调用WebService
			RPCServiceClient serviceClient = new RPCServiceClient();
			// 指定调用WebService的URL
			EndpointReference targetEPR = new EndpointReference(url);
			Options options = serviceClient.getOptions();
			// 确定目标服务地址
			options.setTo(targetEPR);
			// 确定调用方法
			//options.setAction("urn:getPrice");
			
			QName qname = new QName("commandack");
			// 指定getPrice方法的参数值
			Object[] parameters = new Object[] { "", "", "", "", "", 0, 0, 0, "" };

			// 指定getPrice方法返回值的数据类型的Class对象
			Class[] returnTypes = new Class[] { String.class };

			/*// 调用方法一 传递参数，调用服务，获取服务返回结果集
			OMElement element = serviceClient.invokeBlocking(qname, parameters);
			// 值得注意的是，返回结果就是一段由OMElement对象封装的xml字符串。
			// 我们可以对之灵活应用,下面我取第一个元素值，并打印之。因为调用的方法返回一个结果
			String result = element.getFirstElement().getText();
			System.out.println(result);*/

			// 调用方法二 getPrice方法并输出该方法的返回值
			Object[] response = serviceClient.invokeBlocking(qname, parameters, returnTypes);
			// String r = (String) response[0];
			String r = (String) response[0];
			System.out.println(r);
		} catch (AxisFault e) {
			e.printStackTrace();
		}
	}
	
	private static void testAxisWebservice() {
		
	}

	public static void main(String[] args) {
//		test();
		testAxis2Webservice();
	}
	
}
