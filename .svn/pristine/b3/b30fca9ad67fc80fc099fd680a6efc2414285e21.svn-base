package com.aotain.smmsapi.task.smmsreturn.service;

import org.apache.log4j.Logger;

import com.aotain.common.config.ContextUtil;
import com.aotain.smmsapi.task.QuartzMain;
import com.aotain.smmsapi.task.smmsreturn.service.impl.SmmsReturnServiceImpl;

/**
 * 管局返回文件处理线程
 * 
 * @author liuz@aotian.com
 * @date 2018年8月24日 上午9:45:26
 */
public class DoSmmsReturnFileThread implements Runnable {
	private SmmsReturnService service;
	private Logger logger = Logger.getLogger(DoSmmsReturnFileThread.class);
	
	public DoSmmsReturnFileThread(){
		service = ContextUtil.getContext().getBean(SmmsReturnServiceImpl.class);
	}
	
	private int status = 0;

	@Override
	public void run() {
		status = 1;
		logger.info("DoSmmsReturnFileThread start ...");
		while(status != 2) {
			if (!QuartzMain.LSELECTOR.getLeader()) { // 非领导时，1分钟循环一次
				try {
					logger.info("DoSmmsReturnFileThread is not leader , will to sleep 1 minit ...");
					Thread.sleep(60 * 1000);
				} catch (InterruptedException e) {
					logger.error("InterruptedException found, Thread to be shutdown",e);
					break; // 时钟中断
				} 
				continue;
			}
			try{
				service.redisCacheDoProcess();
			}catch(Throwable e){
				logger.error("unexpected exceptoin found ",e);
			}
			try {
				Thread.sleep(20*1000); // 20秒钟处理1次
			} catch (InterruptedException e) {
				logger.error("InterruptedException found, Thread to be shutdown",e);
				break;// 时钟中断
			}
		}
		logger.warn("DoSmmsReturnFileThread shutdown");
		status = 3;
		
	}

	public void stop() {
		status = 2;
		logger.warn("receive stop command");
	}

}
