package com.aotain.smmsapi.task.smmsreturn.service;

/**
 * 管局999目录返回之后，需要将待上报表更新到上报表并清理操作表、等待表数据状态
 * 
 * @author liuz@aotian.com
 * @date 2018年8月15日 下午5:33:19
 */
public interface SmmsReturnService {
	/**
	 * 从redis中获取文件处理结果，并处理
	 */
	public void redisCacheDoProcess();
	
	/**
	 * 开始处理
	 * @param taskId
	 * @param fileName
	 * @param status
	 * @param fileType 
	 */
	public void doProcess(long taskId,String fileName,int status, int fileType);

	/**
	 * 将接收到的文件写入Redis缓存
	 * @param taskId 任务ID
	 * @param fileName 文件名
	 * @param status 999返回的文件处理状态
	 * @param fileType 文件类型
	 */
	public void writeRedisCache(long taskId, String fileName, int status, int fileType);
}
