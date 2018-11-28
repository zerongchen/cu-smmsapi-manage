package com.aotain.smmsapi.task.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class Tools {
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
	 * 判断是否url
	 * @param url
	 * @return
	 */
	public static boolean isURL(String url) {
		if (url == null) {
			return false; 
		} 
		String regex = "^" +
						// protocol identifier
						"(?i)(" + 
						"(?:(?:https?|ftp)://)" +
						// user:pass authentication
						"(?:\\S+(?::\\S*)?@)?" +
						"(?:" +
						// IP address exclusion
						// private & local networks
						"(?!10(?:\\.\\d{1,3}){3})" +
						"(?!127(?:\\.\\d{1,3}){3})" +
						"(?!169\\.254(?:\\.\\d{1,3}){2})" +
						"(?!192\\.168(?:\\.\\d{1,3}){2})" +
						"(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})" +
						// IP address dotted notation octets
						// excludes loopback network 0.0.0.0
						// excludes reserved space >= 224.0.0.0
						// excludes network & broacast addresses
						// (first & last IP address of each class)
						"(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])" +
						"(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}" +
						"(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))" +
						"|" +
						// host name
						"(?:(?:[a-z0-9]+-?)*[a-z0-9]+)" +
						// domain name
						"(?:\\.(?:[a-z0-9]+-?)*[a-z0-9]+)*" +
						// TLD identifier
						"(?:\\.(?:[a-z]{2,}))" +
						")" +
						// port number
						"(?::\\d{2,5})?" +
						// resource path
						"(?:/[^\\s]*)?" +
						")$";
		Pattern pattern = Pattern.compile(regex); 
		Matcher matcher = pattern.matcher(url);
		return matcher.matches(); 
	}

	/**
	 * 判断是否是合法域名
	 * 
	 * @param domain
	 * @return
	 */
	public static boolean isDomain(String domain) {
		if (domain == null) {
			return false; 
		} 
		String regex = "^" +
						// protocol identifier
						"(?i)(" + 
						"(?:(?:https?|ftp)://)?" +
						// user:pass authentication
						"(?:\\S+(?::\\S*)?@)?" +
						"(?:" +
						// IP address exclusion
						// private & local networks
						/* 支持IP
						"(?!10(?:\\.\\d{1,3}){3})" +
						"(?!127(?:\\.\\d{1,3}){3})" +
						"(?!169\\.254(?:\\.\\d{1,3}){2})" +
						"(?!192\\.168(?:\\.\\d{1,3}){2})" +
						"(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})" +
						*/
						// IP address dotted notation octets
						// excludes loopback network 0.0.0.0
						// excludes reserved space >= 224.0.0.0
						// excludes network & broacast addresses
						// (first & last IP address of each class)
						"(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])" +
						"(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}" +
						"(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))" +
						"|" +
						// host name
						"(?:(?:[a-z\\u00a1-\\uffff0-9]+-?)*[a-z\\u00a1-\\uffff0-9]+)" +
						// domain name
						"(?:\\.(?:[a-z\\u00a1-\\uffff0-9]+-?)*[a-z\\u00a1-\\uffff0-9]+)*" +
						// TLD identifier
						"(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))" +
						")" +
						// port number
						"(?::\\d{2,5})?" +
						// resource path
						"(?:/[^\\s]*)?" +
						")$";
		Pattern pattern = Pattern.compile(regex); 
		Matcher matcher = pattern.matcher(domain);
		return matcher.matches(); 
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
	//String类型转换成oracle.sql.Clob类型
	/*public static Clob stringToClob(String str) {  

		if (null == str)  
			return null;  
		else {  
			try {  
				java.sql.Clob c = new javax.sql.rowset.serial.SerialClob(str.toCharArray());  
				return c;  
				} catch (Exception e) {  
				return null;  
				}  
			}  
	}  

	public static Clob stringToClob(String str) throws Exception { 
		java.sql.Clob c = null;
        java.lang.reflect.Method methodToInvoke = c.getClass().getMethod( 
                "getCharacterOutputStream", (Class[]) null); 
        Writer writer = (Writer) methodToInvoke.invoke(c, (Object[]) null); 
        writer.write(str); 
        writer.close(); 
        return c; 
    } */

	
	public static boolean isNumber(String num) {
		return (num!=null && (num.matches("[0-9]+") || num.matches("([0-9])+?\\.+([0-9]+)")));
	}
	
	public static boolean isEmail(String email) {
		return (email!=null && email.matches("^([a-z0-9A-Z]+[-\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$"));
	}
	
	public static String getRandomCode(int bit){
		char c[] = new char[62];
		for (int i = 97, j = 0; i < 123; i++, j++) {
			c[j] = (char) i;
		}
		for (int o = 65, p = 26; o < 91; o++, p++) {
			c[p] = (char) o;
		}
		for (int m = 48, n = 52; m < 58; m++, n++) {
			c[n] = (char) m;
		}
		//生成随机类
		Random random = new Random();
		String checkCode = "";
		
		for (int i = 0; i < bit; i++) {
			int x = random.nextInt(62);
			String rand = String.valueOf(c[x]);
			checkCode += rand;
		}
		
		return checkCode;
	}
	
	public static long getBatchId(){
		String mBatchId="";
		Calendar cal=Calendar.getInstance();
		mBatchId=""+cal.get(Calendar.YEAR);
		mBatchId+=format2Two(cal.get(Calendar.MONTH)+1+"");
		mBatchId+=format2Two(cal.get(Calendar.DAY_OF_MONTH)+"");
		mBatchId+=format2Two(cal.get(Calendar.HOUR_OF_DAY)+"");
		mBatchId+=format2Two(cal.get(Calendar.MINUTE)+"");
		mBatchId+=format2Two(cal.get(Calendar.SECOND)+"");
		//mBatchId+=format2Two(cal.get(Calendar.MILLISECOND)+"");
		return Long.parseLong(mBatchId);
	}
	
	public static String format2Two(String num){
		if(num.length()<2){
			return "0"+num;
		}
		return num;
	}
	
	public static String getTimeFromNow() {
		try {
			// 输入的是相对时间的 秒差
			Date date = new Date();
			SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return inputFormat.format(date);

		} catch (Exception ex) {
			ex.printStackTrace();
			return "";
		}
	}
	
	public static String getStringFromNow(){
		try {
			// 输入的是相对时间的 秒差
			Date date = new Date();
			SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			return inputFormat.format(date);
		} catch (Exception ex) {
			ex.printStackTrace();
			return "";
		}
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
	
	
	public static void main(String[] args) {
//		System.out.println(isURL("BAIDU.COM"));
		System.out.println(Tools.getStringFromNow().substring(14));
		System.out.println(Tools.getStringFromNow());
		System.out.println(Tools.getStringFromNow());
		System.out.println(System.currentTimeMillis());
		System.out.println(new Date().getTime());
	}
}
