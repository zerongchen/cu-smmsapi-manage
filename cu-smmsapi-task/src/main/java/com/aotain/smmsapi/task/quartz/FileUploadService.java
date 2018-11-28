package com.aotain.smmsapi.task.quartz;


public interface FileUploadService {
	
	
	/**
	 * 创建ISMS活动状态定时任务
	 * 
	 * @author : songl
	 * @since:2017年11月14日 下午5:26:19
	 */
	public void createActiveStateTask();
	
	/**
	 * 创建活跃资源监控上报定时任务
	 * 
	 * @author : songl
	 * @since:2017年11月14日 下午5:26:19
	 */
	public void createActiveResourcesMonitorTask();
	
	/**
	 * 创建违法违规网站监测数据上报定时任务
	 * 
	 * @author : liuz
	 * @since:2017年11月15日 上午10:58:10
	 */
	public abstract void createIllegalWebMonitorTask();
	/**
	 * 创建基础数据异常监测数据上报定时任务
	 * 
	 * @author : songl
	 * @since:2017年11月28日 上午8:52:29
	 */
	public void createBasicMonitorTask();
}
