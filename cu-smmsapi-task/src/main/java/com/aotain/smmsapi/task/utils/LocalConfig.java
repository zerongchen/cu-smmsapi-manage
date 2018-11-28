package com.aotain.smmsapi.task.utils;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import com.aotain.common.utils.tools.FileUtils;

public class LocalConfig {
	private String resourceMonitor;
	private String kafkaTopicName;
	private Integer kafkaCoustomerThreadNum;

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

	private LocalConfig() {
//		ResourceBundle config = ResourceBundle.getBundle("config-smmstask-kafka");
		Properties pro = FileUtils.loadPropertiesFromConfig("config", "config-smmstask-kafka.properties");
		kafkaTopicName = (String) pro.get("ack.kafka.topic");
		String tp = (String) pro.get("ack.kafka.constomer.threadNum");
		if(!StringUtils.isBlank(tp)&& StringUtils.isNumeric(tp)){
			kafkaCoustomerThreadNum = Integer.parseInt(tp);
		}else{
			kafkaCoustomerThreadNum = 1;
		}
		resourceMonitor = (String) pro.get("resourceMonitorFlag");
	}

	public String getResourceMonitor() {
		return resourceMonitor;
	}

	public void setResourceMonitor(String resourceMonitor) {
		this.resourceMonitor = resourceMonitor;
	}

	public String getKafkaTopicName() {
		return kafkaTopicName;
	}

	public void setKafkaTopicName(String kafkaTopicName) {
		this.kafkaTopicName = kafkaTopicName;
	}

	public Integer getKafkaCoustomerThreadNum() {
		return kafkaCoustomerThreadNum;
	}

	public void setKafkaCoustomerThreadNum(Integer kafkaCoustomerThreadNum) {
		this.kafkaCoustomerThreadNum = kafkaCoustomerThreadNum;
	}

	
	
}
