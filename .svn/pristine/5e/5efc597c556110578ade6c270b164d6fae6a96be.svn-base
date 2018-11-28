package com.aotain.smmsapi.webservice;

import java.util.HashMap;
import java.util.Map;

public class BaseWs {

	protected final static Map<Integer, String> msgCode = new HashMap<Integer, String>();

	public BaseWs() {
		msgCode.put(0, "无错误操作成功");
		msgCode.put(1, "用户认证失败出错");
		msgCode.put(2, "解密失败");
		msgCode.put(3, "文件校验失败");
		msgCode.put(4, "解压缩失败");
		msgCode.put(5, "文件格式异常");
		msgCode.put(6, "文件内容异常");
		msgCode.put(999, "其他错误");
	}

	protected String getMsgByCode(int code) {
		String msg = "未知错误";
		if (msgCode.containsKey(code)) {
			msg = msgCode.get(code);
		}
		return msg;
	}
}
