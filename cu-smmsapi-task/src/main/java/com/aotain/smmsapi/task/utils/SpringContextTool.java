package com.aotain.smmsapi.task.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Spring上下文工具类，用于获取bean
 * 注意：要是该类起作用，必须将此类配置到spring 的 application-context.xml中，并且设置 延迟加载为false
 */
public class SpringContextTool implements ApplicationContextAware {
	
	private static ApplicationContext context;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		SpringContextTool.context = applicationContext;
	}
	
	
	public static ApplicationContext getApplicationContext() {
		return context;
	}
	
	/**
	 * 从上下文获取bean的通用方法
	 * @param name bean 名称
	 * @return
	 */
	public static Object getBean(String name){
		return context.getBean(name);
	}
	
 }