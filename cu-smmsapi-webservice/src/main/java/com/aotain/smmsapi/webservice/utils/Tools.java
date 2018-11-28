package com.aotain.smmsapi.webservice.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.sql.Clob;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.common.util.Base64Exception;
import org.apache.cxf.common.util.Base64Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aotain.smmsapi.webservice.constant.DateFormatConstant;

public class Tools {
	private static Logger logger = LoggerFactory.getLogger(Tools.class);
	
	/**
	 * 判断是否是IP地址
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isIpAddress(String s) {
		if (s == null)
			return false;
		String regex = "(((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))[.](((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))[.](((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))[.](((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(s);
		return m.matches();
	}

	/**
	 * 获取ICP注册域名
	 * 
	 * @param domain
	 * @return
	 */
	public static String getICPDomain(String domain) {
		String root = "";
		String[] temp = domain.replace(".com.cn", ".com_cn").replace(".net.cn",
				".net_cn").replace(".org.cn", ".org_cn").replace(".gov.cn",
				".gov_cn").replace(".vnet.cn", ".vnet_cn").replace(".edu.cn",
				".edu_cn").replace(".", "/").split("/");
		if (temp.length > 2) {
			// root = domain.substring(domain.indexOf(".")+1);
			root = temp[temp.length - 2] + "." + temp[temp.length - 1];
			root = root.replace("_", ".");
		} else
			root = domain;

		return root;
	}

	/**
	 * ip地址转成整数.
	 * 
	 * @param ip
	 * @return
	 */
	public static long ip2long(String ip) {
		if (!isIpAddress(ip))
			return -1;

		String[] ips = ip.split("[.]");
		long num = 16777216L * Long.parseLong(ips[0]) + 65536L
				* Long.parseLong(ips[1]) + 256 * Long.parseLong(ips[2])
				+ Long.parseLong(ips[3]);

		return num;
	}

	/**
	 * 整数转成ip地址.
	 * 
	 * @param ipLong
	 * @return
	 */
	public static String long2ip(long ipLong) {
		long mask[] = { 0x000000FF, 0x0000FF00, 0x00FF0000, 0xFF000000 };
		long num = 0;
		StringBuffer ipInfo = new StringBuffer();
		for (int i = 0; i < 4; i++) {
			num = (ipLong & mask[i]) >> (i * 8);
			if (i > 0)
				ipInfo.insert(0, ".");
			ipInfo.insert(0, Long.toString(num, 10));
		}
		return ipInfo.toString();
	}

	// oracle.sql.Clob类型转换成String类型
	public static String ClobToString(Clob clob) throws SQLException,
			IOException {
		String reString = "";
		Reader is = clob.getCharacterStream();// 得到流
		BufferedReader br = new BufferedReader(is);
		String s = br.readLine();
		StringBuffer sb = new StringBuffer();
		// 执行循环将字符串全部取出付值给StringBuffer由StringBuffer转成String
		while (s != null) {
			sb.append(s);
			s = br.readLine();
		}
		reString = sb.toString();
		return reString;
	}

	public static boolean isNumber(String num) {
		return (num != null && num.matches("[0-9]+"));
	}

	public static boolean isInt(String num) {
		try {
			Integer.parseInt(num);
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	public static String getBatchId() {
		String mBatchId = "";
		Calendar cal = Calendar.getInstance();
		mBatchId = "" + cal.get(Calendar.YEAR);
		mBatchId += format2Two(cal.get(Calendar.MONTH) + 1 + "");
		mBatchId += format2Two(cal.get(Calendar.DAY_OF_MONTH) + "");
		mBatchId += format2Two(cal.get(Calendar.HOUR_OF_DAY) + "");
		mBatchId += format2Two(cal.get(Calendar.MINUTE) + "");
		mBatchId += format2Two(cal.get(Calendar.SECOND) + "");
		mBatchId += format2Two(cal.get(Calendar.MILLISECOND)+"");
		mBatchId += getUUIDPrefix();
		return mBatchId;
	}
	
	public static String getUUIDPrefix() {
		String result = "";
		//550E8400-E29B-11D4-A716-446655440000
		UUID uuid = UUID.randomUUID();
		String temp = uuid.toString();
		if (temp.contains("-")) {
			result = temp.substring(0, 8);
		}
		return result;
	}

	public static String format2Two(String num) {
		if (num.length() < 2) {
			return "0" + num;
		}
		return num;
	}

	// 当前时间，格式yyyy-MM-dd HH:mm:ss
	public static String getTimeStamp() {
		String ret = "";
		Calendar cal = Calendar.getInstance();
		ret = "" + cal.get(Calendar.YEAR);
		ret += "-" + format2Two(cal.get(Calendar.MONTH) + 1 + "");
		ret += "-" + format2Two(cal.get(Calendar.DAY_OF_MONTH) + "");
		ret += " " + format2Two(cal.get(Calendar.HOUR_OF_DAY) + "");
		ret += ":" + format2Two(cal.get(Calendar.MINUTE) + "");
		ret += ":" + format2Two(cal.get(Calendar.SECOND) + "");
		return ret;
	}
	
	public static String getTime(long time) {
		try {
			long startT = 0;//fromDateStringToLong("1970-01-01 08:00:00");

			long in = Long.parseLong(time + "");
			// 输入的是相对时间的 秒差
			Date date1 = new Date(1000 * in + startT);
			SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return inputFormat.format(date1);

		} catch (Exception ex) {
			ex.printStackTrace();
			return "";
		}
	}
	
	
	public static Long setTime(String time) {
		long startT = 0;
		long longTime = 0;
		try {
			longTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time).getTime();
			startT = 0;//fromDateStringToLong("1970-01-01 08:00:00");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return (longTime - startT)/1000;
	}
	public static String idlist2string(List<?> idlist) {
		String ret = "";
		for (int i = 0; i < idlist.size(); i++) {
			String mId = idlist.get(i).toString();
			ret += mId + ",";
		}
		if (!ret.equals("")) {
			ret = ret.substring(0, ret.length() - 1);
		}
		return ret;
	}

	//base64解码
	public static String decodeBase64(String param){
		if(StringUtils.isNotBlank(param)){
			try {
				param = new String(Base64Utility.decode(param), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				logger.error(e.toString());
			} catch (Base64Exception e) {
				logger.error(e.toString());
			}
		}
		return param;
	}
	
	//base64编码
	public static String encodeBase64(String param){
		if(StringUtils.isNotBlank(param)){
			param = Base64Utility.encode(param.getBytes());
		}
		return param;
	}

	// 用，隔开多个字符串转换成List
	public static void str2List(List<BigInteger> list, String mStr) {
		if (mStr != null) {
			String[] strs = mStr.split(",");
			for (int i = 0; i < strs.length; i++) {
				list.add(new BigInteger(strs[i]));
			}
		}

	}
	
	public static List<BigInteger> str2List(String mStr, String separator) {
		List<BigInteger> list = new ArrayList<BigInteger>();
		if (mStr != null) {
			String[] strs = mStr.split(separator);
			for (int i = 0; i < strs.length; i++) {
				list.add(new BigInteger(strs[i]));
			}
		}
		return list;
	}
	
	public static List<Long> str2ListWithLong(String mStr, String separator) {
		List<Long> list = new ArrayList<Long>();
		if (mStr != null) {
			String[] strs = mStr.split(separator);
			for (int i = 0; i < strs.length; i++) {
				list.add(new Long(strs[i]));
			}
		}
		return list;
	}
	
	public static List<Integer> strToIntegerList(String mStr, String separator) {
		List<Integer> list = new ArrayList<Integer>();
		if (mStr != null) {
			String[] strs = mStr.split(separator);
			for (int i = 0; i < strs.length; i++) {
				list.add(new Integer(strs[i]));
			}
		}
		return list;
	}

	public static void str2List2(List<String> list, String mStr) {
		if (mStr != null) {
			String[] strs = mStr.split(",");
			for (int i = 0; i < strs.length; i++) {
				list.add(strs[i]);
			}
		}

	}

	// 得到时间的毫秒数，s为时间，格式为yyyy-MM-dd HH:mm:ss
	public static long getTimeMS(String s) throws ParseException {
		long startT = 0;
		long longTime = 0;
		try {
			longTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(s).getTime();
			startT = 0;//fromDateStringToLong("1970-01-01 08:00:00");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return (longTime - startT)/1000;
	}
	
	// 得到日期时间+天数后的毫秒数，s为时间，格式为yyyy-MM-dd HH:mm:ss
	public static long getAddTime(String s,int addhour) throws ParseException {
		long startT = 0;
		long longTime = 0;
		try {
			Date date=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(s);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(Calendar.DATE, 1);
			longTime = cal.getTime().getTime();
			startT = 0;//fromDateStringToLong("1970-01-01 08:00:00");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return (longTime - startT)/1000;
	}
	
	public static Long fromDateStringToLong(String inVal) {
		Date date = null;
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			date = inputFormat.parse(inVal); // 将字符型转换成日期型
		} catch (Exception e) {
			e.printStackTrace();
		}
		return date.getTime(); // 返回毫秒数
	}
	
	public static Integer formatDateStrToInt(String val) {
		String [] arr =  val.split("-");
		return Integer.parseInt(arr[0]+arr[1]+arr[2]);
	}
	
	//得到minute
	public static int getMinute(String inVal) {
		Date date = null;
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		int minute = 0;
		try {
			date = inputFormat.parse(inVal); // 将字符型转换成日期型
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			minute = cal.get(Calendar.MINUTE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return minute;
	}
	
    //得到hh
	public static int getHour(String inVal) {
		Date date = null;
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String hour="";
		try {
			date = inputFormat.parse(inVal); // 将字符型转换成日期型
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			hour = cal.get(Calendar.HOUR_OF_DAY) < 10 ? "0" + cal.get(Calendar.HOUR_OF_DAY) : cal.get(Calendar.HOUR_OF_DAY) + "";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Integer.parseInt(hour); // 返回毫秒数
	}
	
	//得到yyyymmdd
	public static int getDateStr(String inVal) {
		Date date = null;
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String year = "";
		String month = "";
		String day = "";
		try {
			date = inputFormat.parse(inVal); // 将字符型转换成日期型
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			year = cal.get(Calendar.YEAR) + "";
			int month1 = cal.get(Calendar.MONTH) + 1;
			month = month1 < 10 ? "0" + month1 : month1 + "";
			day = cal.get(Calendar.DAY_OF_MONTH) < 10 ? "0" + cal.get(Calendar.DAY_OF_MONTH) : cal.get(Calendar.DAY_OF_MONTH) + "";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Integer.parseInt(year + month + day); // 返回毫秒数
	}

	// 得到时间字符串，s为时间毫秒数，格式为yyyy-MM-dd HH:mm:ss
	public static String getTimeStr(long s) throws ParseException {
		try {
			long startT = 0;//fromDateStringToLong("1970-01-01 08:00:00");

			long in = Long.parseLong(s + "");
			// 输入的是相对时间的 秒差
			Date date1 = new Date(1000 * in + startT);
			SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return inputFormat.format(date1);

		} catch (Exception ex) {
			ex.printStackTrace();
			return "";
		}
	}

	public static String getTimeFormat(String s) throws ParseException {
		String[] m = s.split(" ");
		String ret = "";
		if (m.length > 0)
			ret = m[0];
		return ret;
	}
	
	public static String getCurrentTime(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(new Date());
	}
	
	public static String getHexString(byte[] b) {
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}
	
	public static byte[] getByteArray(String hexString) {
		  return new BigInteger(hexString,16).toByteArray();
	}
	
	/**
	 * 根据分割时间间隔，将时间分割。
	 * @param startDateStr	开始时间，格式为：yyyy-MM-dd HH:mm:ss
	 * @param endDateStr	结束时间，格式为：yyyy-MM-dd HH:mm:ss
	 * @param interval		分割时间间隔，单位秒
	 * @return	按照时间间隔分割后的时间值
	 */
	public static List<Long> obtainInterval(String startDateStr, String endDateStr, int interval) {
		return obtainInterval(startDateStr, endDateStr, DateFormatConstant.DATETIME_CHS, interval); 
	}
	
	public static Set<Long> obtainIntervalNew(String startDateStr, String endDateStr) {
		return obtainIntervalNew(startDateStr, endDateStr, DateFormatConstant.DATETIME_CHS);
	}
	
	/**
	 * 根据分割时间间隔(一个小时)，将时间分割。
	 * @param startDateStr	开始时间，格式为：yyyy-MM-dd HH:mm:ss
	 * @param endDateStr	结束时间，格式为：yyyy-MM-dd HH:mm:ss
	 * @return	按照时间间隔分割后的时间值
	 */
	public static Set<Long> obtainIntervalNew(String startDateStr, String endDateStr, String pattern) {
		Set<Long> result = new TreeSet<Long>();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(pattern);
			Date startDate = sdf.parse(startDateStr);
			Date endDate = sdf.parse(endDateStr);
			Calendar cal_start = Calendar.getInstance();
			Calendar cal_end = Calendar.getInstance();
			cal_start.setTime(startDate);
			cal_end.setTime(endDate);
			Calendar temp = Calendar.getInstance();
			result.add(cal_start.getTimeInMillis() / 1000);
			while (cal_start.getTimeInMillis() < cal_end.getTimeInMillis()) {
				temp.setTime(cal_start.getTime());
				result.add(temp.getTimeInMillis() / 1000);
				cal_start.add(Calendar.HOUR_OF_DAY, 1);
				temp.add(Calendar.HOUR_OF_DAY, 1);
				if (temp.before(cal_end)) {
					if (temp.get(Calendar.MINUTE) != 0 || temp.get(Calendar.MILLISECOND) != 0) {
						temp.set(Calendar.MINUTE, 0);
						temp.set(Calendar.SECOND, 0);
						result.add(temp.getTimeInMillis() / 1000);
					}
				}
			}
			if (cal_start.getTimeInMillis() == cal_end.getTimeInMillis()) {
				result.add(cal_start.getTimeInMillis() / 1000);
			} else {
				result.add(cal_end.getTimeInMillis() / 1000);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static Map<Integer, List<Integer>> obtainIntervalNewForDayHour(String startTime, String endTime) {
		Map<Integer, List<Integer>> dayHourMap = new TreeMap<Integer, List<Integer>>();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(DateFormatConstant.DATETIME_CHS);
			Date startDate = sdf.parse(startTime);
			Date endDate = sdf.parse(endTime);
			Calendar cal_start = Calendar.getInstance();
			Calendar cal_end = Calendar.getInstance();
			cal_start.setTime(startDate);
			cal_end.setTime(endDate);
			while (cal_start.getTimeInMillis() < cal_end.getTimeInMillis()) {
				List<Integer> hours = new ArrayList<Integer>();
				int startDt   = Tools.getDateStr(Tools.getTimeStr(cal_start.getTimeInMillis() / 1000));
				int endDt   = Tools.getDateStr(Tools.getTimeStr(cal_end.getTimeInMillis() / 1000));
				int startHour = Tools.getHour(Tools.getTimeStr(cal_start.getTimeInMillis() / 1000));
				int endHour = Tools.getHour(Tools.getTimeStr(cal_end.getTimeInMillis() / 1000));
				if (startDt != endDt) {
					for (int i = startHour; i < 24; i++) {
						hours.add(i);
					}
				} else {
					for (int i = startHour; i <= endHour; i++) {
						hours.add(i);
					}
				}
				cal_start.add(Calendar.DAY_OF_MONTH, 1);
				cal_start.set(Calendar.HOUR_OF_DAY, 0);
				cal_start.set(Calendar.MINUTE, 0);
				cal_start.set(Calendar.SECOND, 0);
				dayHourMap.put(startDt, hours);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dayHourMap;
	}
	
	public static List<Long> obtainInterval(String startDateStr, String endDateStr, String pattern, int interval) {
		List<Long> result = new ArrayList<Long>();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(pattern);
			Date startDate = sdf.parse(startDateStr);
			Date endDate = sdf.parse(endDateStr);
			Calendar cal = Calendar.getInstance();
			cal.setTime(startDate);
			long start = cal.getTimeInMillis() / 1000;
			cal.setTime(endDate);
			long end = cal.getTimeInMillis() / 1000;
			Calendar preCal = Calendar.getInstance();
			while (start < end) {
				preCal.setTimeInMillis(start * 1000);
				result.add(start);
				start = start + interval;
				
				cal.setTimeInMillis(start * 1000);
				if (preCal.get(Calendar.HOUR_OF_DAY) != cal.get(Calendar.HOUR_OF_DAY)) {
					cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);
					result.add(cal.getTimeInMillis() / 1000);
				}
			}
			if (start == end) {
				result.add(start);
			} else {
				result.add(end);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static void main(String[] args) throws IOException, ParseException {
		System.out.println(fromDateStringToLong("2017-10-10 10:10:10"));
		
//        System.out.println(getUUIDPrefix());
//        System.out.println(getUUIDPrefix());
//        System.out.println(getUUIDPrefix());
//        System.out.println(getBatchId());
//        System.out.println(getBatchId());
//        System.out.println(getBatchId());
		
	}
	
}