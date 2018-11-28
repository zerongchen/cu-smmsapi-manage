package com.aotain.smmsapi.task.quartz.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.aotain.smmsapi.task.QuartzMain;
import com.aotain.smmsapi.task.prevalidate.service.PreValidateIdc;
import com.aotain.smmsapi.task.quartz.PreValidateService;

@Component
@Service("preValidate")
public class PreValidateServiceImpl implements PreValidateService {

	private static Logger logger = LoggerFactory.getLogger(PreValidateServiceImpl.class);

	@Autowired
	private PreValidateIdc preValidateIdc;

	@Override
	public void handlePreValidate() {
		if (QuartzMain.LSELECTOR == null || !QuartzMain.LSELECTOR.getLeader()) {
			logger.debug("service node(" + QuartzMain.SERVICE_NODE_NAME + ") is sleeping ...");
			return;
		} else {
			logger.info("service node(" + QuartzMain.SERVICE_NODE_NAME + ") is working ...");
		}
		logger.info("[DataValidate]validate preInput begin... ...");
		preValidateIdc.handleValidateIdc();
		logger.info("[DataValidate]validate preInput end... ...");
	}

}
