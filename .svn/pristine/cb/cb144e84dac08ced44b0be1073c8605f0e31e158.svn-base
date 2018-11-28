package com.aotain.smmsapi.task.kafka;

import java.util.Map;

import org.apache.log4j.Logger;

import com.aotain.common.config.LocalConfig;
import com.aotain.common.utils.kafka.KafkaCustomer;

/**
 * Ack上报Kafka环境管理
 * 
 * @author liuz@aotian.com
 * @date 2017年11月15日 下午4:15:45
 */
public class AckKafkaService {
	private KafkaCustomer consumer; // 消费者

	private String ackKafkaTipic;
	private int kafakThreadNum;
	private final String ACK_GROUP_ID = "SMMSTASK-ACK-REPLY";

	private Logger logger = Logger.getLogger(AckKafkaService.class);
	private boolean isRunning = false; // 运行标志

	public AckKafkaService() {
		ackKafkaTipic = com.aotain.smmsapi.task.utils.LocalConfig.getInstance().getKafkaTopicName();
		kafakThreadNum = Integer.parseInt(LocalConfig.getInstance().getHashValueByHashKey("ack.customer.threadnum"));
		
		// 退出程序执行
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// 停止消费
				if(consumer != null){
					consumer.shutdown();
					logger.info("ack-reply service shutdown customer!");
				}
			}
		});
	}

	/**
	 * 启动或者重启
	 */
	public void startup() {
		if (!isRunning) {
			Map<String, Object> conf = LocalConfig.getInstance().getKafkaCustomerConf();
			conf.put("group.id", ACK_GROUP_ID);
			consumer = new KafkaCustomer(conf); // kafka配置
			logger.warn("ack-reply service startup ...");
			consumer.customer(ackKafkaTipic, kafakThreadNum, new AckReplyJob());
			isRunning = true;
		}
	}

	/**
	 * 暂停消费
	 */
	public void pause() {
		if (isRunning) {
			logger.warn("ack-reply service pause ...");
			consumer.shutdown();
			isRunning = false;
			consumer = null;
		}
	}

	/**
	 * 获取异常线程数量
	 * 
	 * @return
	 */
	public int getErrorThreadCount() {
		if (consumer == null) {
			return -1;
		}
		int count = kafakThreadNum - consumer.getActiveThreadNum();
		return count > 0 ? count : 0;
	}

}
