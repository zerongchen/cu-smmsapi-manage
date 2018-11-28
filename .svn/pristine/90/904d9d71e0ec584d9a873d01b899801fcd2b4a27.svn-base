package com.aotain.smmsapi.task.client;

import java.io.StringReader;
import java.math.BigInteger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.axis2.AxisFault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aotain.common.utils.monitorstatistics.ModuleConstant;
import com.aotain.common.utils.tools.MonitorStatisticsUtils;
import com.aotain.smmsapi.task.QuartzMain;
import com.aotain.smmsapi.task.bean.ObjectFactory;
import com.aotain.smmsapi.task.bean.Return;
import com.aotain.smmsapi.task.client.axis2.IDCWebServiceStub;
import com.aotain.smmsapi.task.client.axis2.IDCWebServiceStub.Idc_commandack;

public class AxisWSClient extends BaseWSClient<AxisWSClient, IDCWebServiceStub.Idc_commandack> {
	private static Logger logger = LoggerFactory.getLogger(AxisWSClient.class);

	public AxisWSClient(String idcId, byte[] data) {
		super(idcId, data);
	}

	@Override
	public Return callService(AxisWSClient t) {
		ObjectFactory factory = new ObjectFactory();
		Return result = factory.createReturn();
		try {
			IDCWebServiceStub service = new IDCWebServiceStub(t.getServiceUrl());
			IDCWebServiceStub.Idc_commandackResponse resp = service.idc_commandack(toIdcCommandack(t));
			String response = resp.getIdc_commandackReturn();

			JAXBContext context = JAXBContext.newInstance(Return.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			logger.info("resp.getIdc_commandackReturn()=\n" + response);
			result = (Return) unmarshaller.unmarshal(new StringReader(response));
		} catch (Exception e){
			logger.error("["+ t.getIdcId() +"]callService:", e);
			MonitorStatisticsUtils.addEvent(ModuleConstant.MODULE_SMMSAPI_TASK_ACK, e);
			result.setResultCode(new BigInteger("10000"));
			result.setMsg(e.toString());
        }
		logger.debug("["+ t.getIdcId() +"]callService done.");
		return result;
	}

	@Override
	public Idc_commandack toIdcCommandack(AxisWSClient t) {
		Idc_commandack commandack = new Idc_commandack();
		commandack.setIdcId(t.getIdcId());
		commandack.setRandVal(t.getRandVal());
		commandack.setPwdHash(t.getPwdHash());
		commandack.setResult(t.getResult());
		commandack.setResultHash(t.getResultHash());
		commandack.setEncryptAlgorithm(t.getEncryptAlgorithm());
		commandack.setHashAlgorithm(t.getHashAlgorithm());
		commandack.setCompressionFormat(t.getCommpresssionFormat());
		commandack.setCommandVersion(t.getCommandVersion());
		return commandack;
	}
	
	public static void main(String[] args) throws AxisFault {
		Idc_commandack commandack = new Idc_commandack();
		commandack.setIdcId("");
		commandack.setRandVal("");
		commandack.setPwdHash("");
		commandack.setResult("");
		commandack.setResultHash("");
		commandack.setEncryptAlgorithm(1);
		commandack.setHashAlgorithm(1);
		commandack.setCompressionFormat(1);
		commandack.setCommandVersion("v2.0");
		
		ObjectFactory factory = new ObjectFactory();
		Return result = factory.createReturn();
		try {
			IDCWebServiceStub service = new IDCWebServiceStub("http://14.23.93.146:9090/smmi/ws/commandack?wsdl");
			IDCWebServiceStub.Idc_commandackResponse resp = service.idc_commandack(commandack);
			String response = resp.getIdc_commandackReturn();

			JAXBContext context = JAXBContext.newInstance(Return.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			logger.info("resp.getIdc_commandackReturn()=\n" + response);
			result = (Return) unmarshaller.unmarshal(new StringReader(response));
		} catch (Exception e){
			result.setResultCode(new BigInteger("10000"));
			result.setMsg(e.toString());
			MonitorStatisticsUtils.addEvent(ModuleConstant.MODULE_SMMSAPI_TASK_ACK, e);
        }
	}
}
