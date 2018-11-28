package com.aotain.smmsapi.task;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.aotain.common.config.LocalConfig;
import com.aotain.common.utils.kafka.AckQueueUtil;
import com.aotain.common.utils.kafka.KafkaProducer;
import com.aotain.common.utils.model.msg.SmmsAckQueue;
import com.aotain.common.utils.redis.TaskIdUtil;
import com.aotain.common.utils.tools.XmlUtils;
import com.aotain.smmsapi.task.bean.IdcCommandAck;
import com.aotain.smmsapi.task.bean.IdcCommandAck.CommandAck;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/spring-base.xml" })
public class WriteAckTestData {

	@Test
	public void writeAck() {
		KafkaProducer p = new KafkaProducer(LocalConfig.getInstance().getKafkaProducerConf());
		SmmsAckQueue ack = new SmmsAckQueue();
		ack.setTaskid(TaskIdUtil.getInstance().getTaskId());
		
		IdcCommandAck d = new IdcCommandAck();
		d.setIdcId("123123123");
		d.setTimeStamp("2013-88-11 12:33:21");
		CommandAck e = new  CommandAck();
		e.setCommandId(1231231L);
		e.setHouseId(77777L);
		e.setType(BigInteger.ONE);
		d.getCommandAck().add(e);
		try {
			ack.setAckxml(XmlUtils.toXmlString(d));
			AckQueueUtil.sendMsgToKafkaAckQueue(ack);
			System.out.println("写入成功");
		} catch (UnsupportedEncodingException | JAXBException t) {
			// TODO Auto-generated catch block
			t.printStackTrace();
		}
	}

}
