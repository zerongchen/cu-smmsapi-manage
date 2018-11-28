package com.aotain.smmsapi.webservice.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

public class DBUtil {
	private static Logger logger = Logger.getLogger(DBUtil.class);
	/**
	 * 获取一个数据库连接
	 * @return
	 */
	public static Connection getConnection(){
		String driver = LocalConfig.getInstance().getDbDriver();
		String url = LocalConfig.getInstance().getDbURL(); 
		String user = LocalConfig.getInstance().getDbUser(); 
		String password = LocalConfig.getInstance().getDbPassword(); 
		
		Connection conn = null;
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, user, password);
		} catch (ClassNotFoundException e) {
			logger.error("Not find Driver", e);
		} catch (SQLException e) {
			logger.error("Connect error", e);
		}
		return conn;
	}
	
	/**
	 * 关闭一个数据库连接
	 * @param conn
	 */
	public static void closeConnection(Connection conn){
		if(conn != null){
			try {
				conn.close();
			} catch (SQLException e) {
				logger.error("Close Connect fail", e);
			}
		}
	}
	
	// 关闭数据库连接，释放回数据库连接池
	public static void closeConnection(Connection conn, java.sql.Statement st, ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
			if (st != null) {
				st.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (Exception e) {
			logger.error("Close Connect fail!", e); 
		}
	}
	
	// 关闭数据库连接，释放回数据库连接池
	public static void closeConnection(Connection conn, java.sql.Statement st) {
		try {
			if (st != null) {
				st.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (Exception e) {
			logger.error("Close Connect fail!", e); 
		}
	}
	
	public static void closeConnection(Statement st, ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
			if (st != null) {
				st.close();
			}
		} catch (Exception e) {
			logger.error("Close Connect fail!", e); 
		}
	}
}
