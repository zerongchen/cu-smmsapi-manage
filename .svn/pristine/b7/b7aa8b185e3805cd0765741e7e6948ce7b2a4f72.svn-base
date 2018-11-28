package com.aotain.smmsapi.task;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSON;
import com.aotain.common.config.model.IdcHouses;
import com.aotain.common.utils.model.msg.SmmsAckQueue;
import com.aotain.common.utils.tools.Tools;
import com.aotain.common.utils.tools.ZipUtils;
import com.aotain.smmsapi.task.bean.Return;
import com.aotain.smmsapi.task.client.AxisWSClient;
import com.aotain.smmsapi.task.client.BaseWSClient;
import com.aotain.smmsapi.task.client.CXFWSClient;
import com.aotain.smmsapi.task.client.TJCXFWSClient;
import com.aotain.smmsapi.task.constant.GlobalParams;
import com.aotain.smmsapi.task.utils.LocalConfig;
import com.aotain.smmsapi.task.utils.StringUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring-smmstask-kafka.xml" })
public class AckSend {
	private Logger logger = Logger.getLogger(AckSend.class);
	
	@Test
	public void test(){

		SmmsAckQueue ack = new SmmsAckQueue();
		ack.setTaskid(232322L);
		ack.setToptaskid(0L);
		ack.setAckxml("sadfjkaslfdsaflasdfa");
		ack.setCreatetime(System.currentTimeMillis()/1000);
		String xmlAck = ack.getAckxml();
		logger.info("ack-reply thread " + " xml data : " + xmlAck);
		String identify = "taskid=" + ack.getTaskid() + ",parentid=" + ack.getToptaskid();

		boolean success = false;
		// 1. 将xml字符串压缩成zip文件的byte[]数组
		try {
			byte[] data = ZipUtils.zip(xmlAck, Tools.getBatchId() + ".xml", "UTF-8");
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
					logger.error("ack-reply fail with unknown exception:" + identify);
				} else if (rs.getResultCode().equals(BigInteger.ZERO)) {
					success = true;
					logger.info("ack-reply success:" + identify);
				} else {
					logger.error("ack-reply fail : " + identify + ",code=" + rs.getResultCode() + ",version="
							+ rs.getVersion() + ",msg=" + rs.getMsg());
				}
			} else {
				logger.error("cannot found idcid information , do ack-reply fail:" + identify);
			}
		} catch (Exception e) {
			logger.error("zip ack-reply data exception:" + identify, e);
		}
	}
}
