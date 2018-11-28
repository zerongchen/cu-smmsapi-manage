package com.aotain;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.aotain.common.utils.model.smmscmd.LogQuery;
import com.aotain.common.utils.tools.FileUtils;
import com.aotain.common.utils.tools.XmlUtils;
import com.aotain.smmsapi.webservice.constant.GlobalParams;
import com.aotain.smmsapi.webservice.validate.CommandFileXsdValidator;

public class Main {
	public static void main(String[] args) {
		try {
			FileInputStream is = new FileInputStream(new File("t.xml"));
			byte[] bytes = FileUtils.read2Bytes("t.xml");
			ByteArrayInputStream bai = new ByteArrayInputStream(bytes);
			 System.out.println(CommandFileXsdValidator.getInstance().doValidate(GlobalParams.XFT_LOG_QUERY, bai));
			 LogQuery logquery = XmlUtils.createBean(bai, LogQuery.class);
			 System.out.println(logquery);
			 is.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
