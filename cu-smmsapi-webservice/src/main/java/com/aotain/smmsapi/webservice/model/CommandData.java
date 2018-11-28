package com.aotain.smmsapi.webservice.model;

import com.aotain.common.utils.model.smmscmd.CommandBaseVo;

/**
 * 指令解析结果临时存储对象
 * 
 * @author liuz@aotian.com
 * @date 2017年11月10日 下午2:58:52
 */
public class CommandData {
	private String error;
	private CommandBaseVo cmdObj;

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public CommandBaseVo getCmdObj() {
		return cmdObj;
	}

	public void setCmdObj(CommandBaseVo cmdObj) {
		if (cmdObj == null) {
			error = "指令解析结果为空";
		}
		this.cmdObj = cmdObj;
	}

	@Override
	public String toString() {
		return "CommandData [error=" + error + ", cmdObj=" + cmdObj + "]";
	}

}
