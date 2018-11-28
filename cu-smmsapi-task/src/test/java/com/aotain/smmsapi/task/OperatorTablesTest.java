package com.aotain.smmsapi.task;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.aotain.smmsapi.task.smmsreturn.mapper.OperatorTablesDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring-smmstask-quartz.xml" })
public class OperatorTablesTest {

	@Autowired
	private OperatorTablesDao dao;

	@Test
	@Transactional
	@Rollback
	public void deleteIdcTest(){
		int cnt = dao.deleteJyz(68L);
		System.out.println("删除经营者数据条数："+cnt);
		cnt = dao.deleteHouses(Arrays.asList(4345,4342));
		System.out.println("删除用户数据条数："+cnt);
	}
}
