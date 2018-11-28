package com.aotain.smmsapi.task.bean;

import java.util.Properties;

/**
 * 权限同步配置信息
 * 
 * @author liuz@aotian.com
 * @date 2018年9月30日 下午6:43:41
 */
public class SynchConfig {
	// synch.isms.appid=24
	// synch.passport.preurl=http://192.168.50.152:8060
	// synch.passport.dp_queryUrl=/rest/data/permissionareas
	// synch.passport.dp_deleteUrl=/rest/data/permissionareas/settings/delete
	private Long appId;
	private String preurl;
	private String dp_queryUrl;
	private String dp_deleteUrl;

	public Long getAppId() {
		return appId;
	}

	public void setAppId(Long appId) {
		this.appId = appId;
	}

	public String getPreurl() {
		return preurl;
	}

	public void setPreurl(String preurl) {
		this.preurl = preurl;
	}

	public String getDp_queryUrl() {
		return dp_queryUrl;
	}

	public void setDp_queryUrl(String dp_queryUrl) {
		this.dp_queryUrl = dp_queryUrl;
	}

	public String getDp_deleteUrl() {
		return dp_deleteUrl;
	}

	public void setDp_deleteUrl(String dp_deleteUrl) {
		this.dp_deleteUrl = dp_deleteUrl;
	}

	@Override
	public String toString() {
		return "SynchConfig [appId=" + appId + ", preurl=" + preurl + ", dp_queryUrl=" + dp_queryUrl + ", dp_deleteUrl="
				+ dp_deleteUrl + "]";
	}

	public static SynchConfig loadFromeProperties(Properties prop) {
		if (prop == null || prop.keySet() == null || prop.keySet().size() == 0) {
			return null;
		}
		SynchConfig sc = new SynchConfig();
		// synch.isms.appid=24
		// synch.passport.preurl=http://192.168.50.152:8060
		// synch.passport.dp_queryUrl=/rest/data/permissionareas
		// synch.passport.dp_deleteUrl=/rest/data/permissionareas/settings/delete
		String key = "synch.isms.appid";
		sc.setAppId(Long.parseLong(checkLong(key,prop.getProperty(key))));
		key = "synch.passport.preurl";
		sc.setPreurl(checkNull(key,prop.getProperty(key)));
		key = "synch.passport.dp_queryUrl";
		sc.setDp_queryUrl(checkNull(key,prop.getProperty(key)));
		key = "synch.passport.dp_deleteUrl";
		sc.setDp_deleteUrl(checkNull(key,prop.getProperty(key)));
		return sc;
	}
	
	public static String checkNull(String key,String val) {
		if(null == val || "".equals(val.trim())){
			throw new RuntimeException("no filed ["+key+"] found at ribbon.properties");
		}
		return val;
	}
	
	public static String checkLong(String key,String val) {
		if(null == val || "".equals(val.trim())){
			throw new RuntimeException("no filed ["+key+"] found at ribbon.properties");
		}
		Long.parseLong(val);
		return val;
	}
}
