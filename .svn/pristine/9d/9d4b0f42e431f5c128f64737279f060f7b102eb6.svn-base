package com.aotain.cu.utils;

import java.util.HashMap;
import java.util.Map;

public class ThreadLocalUtil {
	
	private static final ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal<Map<String,Object>>();
	
	/**
	 * 保存键值对到线程中
	 * @param key
	 * @param value
	 */
	public static void set(String key, Object value) {
		if (StringUtil.isBlank(key)) {
			return ;
		}
		if (threadLocal.get() == null) {
			threadLocal.set(new HashMap<String, Object>());
		} 
		Map<String, Object> map = (Map<String, Object>) threadLocal.get();
		map.put(key, value);
	}
	/**
	 * 从线程中获取键为key的值
	 * @param key
	 * @return
	 */
	public static Object get(String key) {
		if (threadLocal.get() == null || StringUtil.isBlank(key)) {
			return null;
		}
		Map<String, Object> map = (Map<String, Object>) threadLocal.get();
		return map.containsKey(key) ? map.get(key) : null;
	} 
	/**
	 * 
	 * @param key
	 * @return
	 */
	public static boolean contain(String key) {
		if (threadLocal.get() == null || StringUtil.isBlank(key)) {
			return false;
		}
		return ((Map)threadLocal.get()).containsKey(key);
	} 
	/**
	 * 
	 */
	public static void destroy() {
		if (threadLocal != null) {
			threadLocal.remove();
		}
	}
	
}
