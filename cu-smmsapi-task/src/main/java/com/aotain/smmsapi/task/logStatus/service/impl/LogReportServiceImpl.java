package com.aotain.smmsapi.task.logStatus.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aotain.smmsapi.task.logStatus.mapper.LogReportDao;
import com.aotain.smmsapi.task.logStatus.service.LogReportService;

/**
 * 日志上报状态回写服务类
 * @author silence
 * @time 2018年3月9日
 */
@Service
public class LogReportServiceImpl implements LogReportService{

	@Autowired
	private LogReportDao logReportDao;
	
	@Override
	public boolean updateLogStatusByTaskId(long taskId,Integer status) {
		int result = logReportDao.updateLogStatusByTaskId(taskId,status);
		return result>0;
	}
	
}
