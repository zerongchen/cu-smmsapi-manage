package com.aotain.smmsapi.webservice.validate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.SAXValidator;
import org.dom4j.io.XMLWriter;
import org.dom4j.util.XMLErrorHandler;

import com.aotain.common.utils.monitorstatistics.ModuleConstant;
import com.aotain.common.utils.tools.MonitorStatisticsUtils;
import com.aotain.smmsapi.webservice.constant.GlobalParams;

/**
 * 指令文件校验工具
 * 
 * @author liuz@aotian.com
 * @date 2017年11月10日 下午3:12:11
 */
public class CommandFileXsdValidator {

	private static File xsdFile = null;
	private static CommandFileXsdValidator instance;
	private Logger logger = Logger.getLogger(CommandFileXsdValidator.class);

	private CommandFileXsdValidator() {
	}

	public synchronized static CommandFileXsdValidator getInstance() {
		if (instance == null) {
			instance = new CommandFileXsdValidator();
		}
		return instance;
	}

	/**
	 * 校验通过返回null，校验失败返回异常提示
	 * 
	 * @param type
	 * @param inputStream
	 * @return
	 * @throws Exception
	 */
	public String doValidate(String type, java.io.InputStream inputStream) throws Exception {
		String xsdName = "";
		switch (type) {
		case GlobalParams.XFT_BLACK_LIST:
			xsdName = "blackList.xsd";
			break;
		case GlobalParams.XFT_CODE_LIST:
			xsdName = "codeList.xsd";
			break;
		case GlobalParams.XFT_IDC_COMMAND:
			xsdName = "idcCommand.xsd";
			break;
		case GlobalParams.XFT_IDC_INFO_MANAGE:
			xsdName = "idcInfoManage.xsd";
			break;
		case GlobalParams.XFT_LOG_QUERY:
			xsdName = "logQuery.xsd";
//			return null; // 访问日志查询指令关闭校验功能，直接返回成功
			break;
		case GlobalParams.XFT_NO_FILTER:
			xsdName = "noFilter.xsd";
			break;
		case GlobalParams.XFT_RETURN_INFO:
			xsdName = "returnInfo.xsd";
			break;
		case GlobalParams.XFT_QUERY_VIEW:
			xsdName = "queryView.xsd";
			break;
		case GlobalParams.XFT_COMMAND_RECORD:
			xsdName = "commandRecord.xsd";
			break;
		default:
			xsdName = null;
			break;
		}
		if (xsdName == null) {
			return "不支持的指令类型：" + type;
		}
		InputStream is = this.getClass().getResourceAsStream(xsdName);
//		String path = this.getClass().getResource(xsdName).getPath();
//		if (System.getProperty("os.name").toLowerCase().indexOf("window") >= 0)
//			path = path.replaceFirst("/", "").replace("%20", " ");
//		xsdFile = new File(path);
		try {
			return doValidate(inputStream, is);
		} finally {
			inputStream.reset();
		}
	}

	private String doValidate(InputStream xmlStream, Object xsdFile) {
		if (xsdFile == null) {
			return null;
		}
		if (xmlStream == null) {
			return "xml数据为空";
		}
		try {
			XMLErrorHandler errorHandler = new XMLErrorHandler();
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setValidating(true);
			factory.setNamespaceAware(true);
			SAXParser parser = factory.newSAXParser();
			SAXReader xmlReader = new SAXReader();
			Document xmlDocument = (Document) xmlReader.read(xmlStream);
			parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
					"http://www.w3.org/2001/XMLSchema");
			parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource", xsdFile);
			SAXValidator validator = new SAXValidator(parser.getXMLReader());
			validator.setErrorHandler(errorHandler);
			validator.validate(xmlDocument);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			XMLWriter writer = new XMLWriter(bos, OutputFormat.createPrettyPrint());
			if (errorHandler.getErrors().hasContent()) {
				writer.write(errorHandler.getErrors());
				writer.flush();
				return new String(bos.toByteArray(), "UTF-8");
			}
			return null;
		} catch (Exception ex) {
			logger.error("xsd格式校验发生异常", ex);
			MonitorStatisticsUtils.addEvent(ModuleConstant.MODULE_INTERFACE, ex);
			return "格式校验发生异常";
		}
	}

	public static void main(String[] args) throws IOException {
		XMLWriter writer;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			writer = new XMLWriter(bos, OutputFormat.createPrettyPrint());
			XMLErrorHandler errorHandler = new XMLErrorHandler();
			writer.write(errorHandler.getErrors());
			System.out.println(bos.toString());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
