package com.aotain.smmsapi.task.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * 配置文件读取工具
 * 
 * @author liuz@aotian.com
 * @date 2017年11月28日 下午2:54:30
 */
//@Configuration
public class TaskConfigUtils {
	@Value("${select.path}")
	private String selectPath;
	
	@Value("${select.name}")
	private String selectName;
	
	@Value("${select.sessionTimeout}")
	private Long selectSessionTimeout;
	
	@Value("${select.connectTimeout}")
	private Long selectConnectTimeout;
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
	    return new PropertySourcesPlaceholderConfigurer();
	}

	public String getSelectPath() {
		return selectPath;
	}

	public void setSelectPath(String selectPath) {
		this.selectPath = selectPath;
	}

	public String getSelectName() {
		return selectName;
	}

	public void setSelectName(String selectName) {
		this.selectName = selectName;
	}

	public Long getSelectSessionTimeout() {
		return selectSessionTimeout;
	}

	public void setSelectSessionTimeout(Long selectSessionTimeout) {
		this.selectSessionTimeout = selectSessionTimeout;
	}

	public Long getSelectConnectTimeout() {
		return selectConnectTimeout;
	}

	public void setSelectConnectTimeout(Long selectConnectTimeout) {
		this.selectConnectTimeout = selectConnectTimeout;
	}

	@Override
	public String toString() {
		return "TaskConfigUtils [selectPath=" + selectPath + ", selectName=" + selectName + ", selectSessionTimeout="
				+ selectSessionTimeout + ", selectConnectTimeout=" + selectConnectTimeout + "]";
	}
	
}
