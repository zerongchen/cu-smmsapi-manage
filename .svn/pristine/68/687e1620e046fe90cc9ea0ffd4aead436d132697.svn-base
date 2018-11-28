package com.aotain.smmsapi.task.utils;

import java.util.Properties;

import com.aotain.common.utils.tools.FileUtils;

/**
 * 从环境中加载配置文件
 * 
 * @author liuz@aotian.com
 * @date 2018年1月9日 下午5:23:33
 */
public class TaskConfigUtilsFactory {

	/**
	 * 从${work.path}或者jar资源路径中加载配置文件
	 * 
	 * @param cfgName
	 * @return
	 */
	public static TaskConfigUtils createConfig(String cfgName) {
		Properties cfg = FileUtils.loadPropertiesFromConfig("config", cfgName);
		TaskConfigUtils tcu = new TaskConfigUtils();
		tcu.setSelectConnectTimeout(Long.parseLong(cfg.getProperty("select.connectTimeout")));
		tcu.setSelectSessionTimeout(Long.parseLong(cfg.getProperty("select.sessionTimeout")));
		tcu.setSelectName(cfg.getProperty("select.name"));
		tcu.setSelectPath(cfg.getProperty("select.path"));
		return tcu;
	}
	
}
