package com.aotain.ismi.webservice;

import javax.jws.WebParam;
import javax.jws.WebService;

@WebService(endpointInterface="com.aotain.ismi.webservice.IsmsCommand", serviceName="IsmsCommand")
public interface IsmsCommand {
	
	public String idc_command(
			@WebParam(name="idcId", targetNamespace="http://webservice.ismi.aotain.com/") String idcId, 
			@WebParam(name="randVal", targetNamespace="http://webservice.ismi.aotain.com/") String randVal, 
			@WebParam(name="pwdHash", targetNamespace="http://webservice.ismi.aotain.com/") String pwdHash,
			@WebParam(name="command", targetNamespace="http://webservice.ismi.aotain.com/") String command, 
			@WebParam(name="commandHash", targetNamespace="http://webservice.ismi.aotain.com/") String commandHash, 
			@WebParam(name="commandType", targetNamespace="http://webservice.ismi.aotain.com/") int commandType,
			@WebParam(name="commandSequence", targetNamespace="http://webservice.ismi.aotain.com/") long commandSequence, 
			@WebParam(name="encryptAlgorithm", targetNamespace="http://webservice.ismi.aotain.com/") int encryptAlgorithm, 
			@WebParam(name="hashAlgorithm", targetNamespace="http://webservice.ismi.aotain.com/") int hashAlgorithm,
			@WebParam(name="compressionFormat", targetNamespace="http://webservice.ismi.aotain.com/") int compressionFormat, 
			@WebParam(name="commandVersion", targetNamespace="http://webservice.ismi.aotain.com/") String commandVersion);
	
}
