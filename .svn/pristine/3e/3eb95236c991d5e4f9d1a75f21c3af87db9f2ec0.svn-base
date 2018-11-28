package com.aotain.smmsapi.task.quartz;

public interface DealResultService {

	/**
	 * 此方法用于处理999目录下的内容
	 * ISMI将数据上报给SMMS后，SMMI解析数据后会将处理结果放在999目录下。
	 * 此方法读取999目录下的文件名,写入redis任务Hash，然后进行文件删除处理
	 */
	public void dealSmmsProcessResult();
}
