package com.aotain.smmsapi.task;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.junit.Test;

import com.aotain.smmsapi.task.logStatus.mapper.LogReportDao;
import com.aotain.smmsapi.task.logStatus.service.LogReportService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring-smmstask-kafka.xml" })
public class logReportStatus {

	@Autowired
	private LogReportService logReportService;
	
	@Autowired
	private LogReportDao logReportDao;
	@Test
	public void updateLogStatus(){
		
		long taskId = 2408354;
		int statu =1;
		Boolean result = logReportService.updateLogStatusByTaskId(taskId,statu);
		System.out.println("result="+result);
	}
}
