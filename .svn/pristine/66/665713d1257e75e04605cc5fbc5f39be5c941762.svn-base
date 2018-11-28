package com.aotain.smmsapi.task.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aotain.common.utils.model.msg.RedisTaskStatus;

import scala.annotation.meta.param;

public class FtpUtil {
	private FTPClient ftpClient;
	private String strIp;
	private int intPort;
	private String user;
	private String password;
	
	private Logger logger = LoggerFactory.getLogger(FtpUtil.class.getName());

	/* *
	 * Ftp构造函数
	 */
	public FtpUtil(String strIp, int intPort, String user, String Password) {
		this.strIp = strIp;
		this.intPort = intPort;
		this.user = user;
		this.password = Password;
		this.ftpClient = new FTPClient();
	}

	/**
	 * @return 判断是否登入成功
	 * */
	public boolean ftpLogin() {
		boolean isLogin = false;
		FTPClientConfig ftpClientConfig = new FTPClientConfig();
		ftpClientConfig.setServerTimeZoneId(TimeZone.getDefault().getID());
		this.ftpClient.setControlEncoding("UTF-8");
		this.ftpClient.configure(ftpClientConfig);
		try {
			if (this.intPort > 0) {
				this.ftpClient.connect(this.strIp, this.intPort);
			} else {
				this.ftpClient.connect(this.strIp);
			}
			// FTP服务器连接回答
			int reply = this.ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				this.ftpClient.disconnect();
				logger.error("login ftp server failed..");
				return isLogin;
			}
			this.ftpClient.login(this.user, this.password);
			// 设置传输协议
			this.ftpClient.enterLocalPassiveMode();
			this.ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			logger.info( this.user + " login ftp server success");
			isLogin = true;
		} catch (Exception e) {
			logger.error(this.user + " login ftp server failed！" + e);
		}
		this.ftpClient.setBufferSize(1024 * 2);
		this.ftpClient.setDataTimeout(30 * 1000);
		this.ftpClient.setConnectTimeout(30 * 1000);
		return isLogin;
	}

	/**
	 * @退出关闭服务器链接
	 * */
	public void ftpLogOut() {
		if (null != this.ftpClient && this.ftpClient.isConnected()) {
			try {
				boolean reuslt = this.ftpClient.logout();// 退出FTP服务器
				if (reuslt) {
					logger.info("login out server success!");
				}
			} catch (IOException e) {
				logger.error("login out ftp exception：" + e);
			} finally {
				try {
					this.ftpClient.disconnect();// 关闭FTP服务器的连接
				} catch (IOException e) {
					logger.error("close FTP exception！:",e);
				}
			}
		}
	}

	public boolean changeWorkingDir(String workPath) {
		boolean result = false;
		try {
			result = this.ftpClient.changeWorkingDirectory(workPath);
			if (result) {
				logger.info("login into ftp directory[" + workPath + "]success！");
			} else {
				logger.info("login into ftp directory[" + workPath + "]success！");
			}
			return result;
		} catch (IOException e) { 
			logger.error("发生异常：", e);
		}
		return result;
	}

	/***
	 * 上传Ftp文件
	 * @param localFile 当地文件
	 * @param romotPath 上传服务器路径 应该以/结束
	 * *//*
	public boolean uploadFile(File localFile, String romotPath) {
		BufferedInputStream inStream = null;
		boolean success = false;
		try {
			this.ftpClient.changeWorkingDirectory(romotPath);// 改变工作路径
			inStream = new BufferedInputStream(new FileInputStream(localFile));
			logger.info(localFile.getName() + " 开始上传.....");
			success = this.ftpClient.storeFile(localFile.getName(), inStream);
			if (success == true) {
				logger.info(localFile.getName() + " 上传成功");
				return success;
			} else {
				logger.info(localFile.getName() + " 上传失败");
				return success;
			}
		} catch (FileNotFoundException e) {
			logger.error(localFile + " 未找到");
			DamsLog.devFtpError(localFile + " 未找到：", e); 
		} catch (IOException e) {
			logger.error("发生异常：" + e);
			DamsLog.devFtpError("发生异常：", e); 
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					logger.error("发生异常：" + e);
					DamsLog.devFtpError(e); 
				}
			}
		}
		return success;
	}
	
	*//***
	 * 上传Ftp文件
	 * @param localFile 当地文件
	 * @param romotPath 上传服务器路径 应该以/结束
	 * *//*
	public int uploadFileComplete(File localFile, String romotPath) {
		BufferedInputStream inStream = null;
		int success = 1;
		String uploadFileName = localFile.getName();
		String uploadFileTempName = localFile.getName() + ".tmp";
		try {
			this.ftpClient.changeWorkingDirectory(romotPath);// 改变工作路径
			inStream = new BufferedInputStream(new FileInputStream(localFile));
			logger.info(uploadFileName + " 开始上传.....");
			boolean flag = this.ftpClient.storeFile(uploadFileTempName, inStream);
			if (flag) {
				boolean f = this.ftpClient.rename(uploadFileTempName, uploadFileName);
				if (f) {
					logger.info(uploadFileTempName + " rename to " + uploadFileName + " successful!");
					logger.info(uploadFileName + " 上传成功");
					success = 0;
					return success;
				}
			} else {
				logger.info(uploadFileName + " 上传失败");
				success = 1;
				return success;
			}
		} catch (FileNotFoundException e) {
			success = 2; 
			EventLogUtil.addEventLog(GlobalParams.RESULT_FILE_NOTFOUND_EXCEPTION_CN); 
			return success;
		} catch (IOException e) {
			success = 3;
			EventLogUtil.addEventLog(GlobalParams.RESULT_FILE_EXCEPTION_CN); 
			return success;
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					e.printStackTrace(); 
					EventLogUtil.addEventLog(GlobalParams.RESULT_FILE_EXCEPTION_CN); 
				}
			}
		}
		return success;
	}
	
	*//**
	 * 
	 * 下载文件
	 * @param remoteFileName 待下载文件名称
	 * @param localPath 下载到当地那个路径下
	 * @param remotePath remoteFileName所在的路径
	 * @return
	 *//*
	public boolean downloadFile(String remoteFileName, String localPath, String remotePath) {
		String strFilePath = localPath + remoteFileName;
		BufferedOutputStream outStream = null;
		boolean success = false;
		try {
			this.ftpClient.changeWorkingDirectory(remotePath);
			outStream = new BufferedOutputStream(new FileOutputStream(strFilePath));
			logger.info(remoteFileName + "  开始下载....");
			success = this.ftpClient.retrieveFile(remoteFileName, outStream);
			if (success == true) {
				logger.info(remoteFileName + "  成功下载到  " + strFilePath);
				return success;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(remoteFileName + "下载失败");
		} finally {
			if (null != outStream) {
				try {
					outStream.flush();
					outStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (success == false) {
			logger.error(remoteFileName + "下载失败!!!");
		}
		return success;
	}*/

	/**
	 * 删除远程服务器上的文件
	 * @param remoteFileName	远程服务器上待删除的文件
	 * @param remotePath	远程服务器路径
	 * @return
	 */
	public boolean removeRemoteFile(String remoteFileName, String remotePath) {
		boolean success = false;
		try {
			this.ftpClient.changeWorkingDirectory(remotePath);
			String[] allNames = this.ftpClient.listNames();
			if (allNames.length > 0) {
				int code = ftpClient.dele(remoteFileName);
				if (code == 250) {
					success = true;
				}
			}
			if (success == true) {
				logger.info(remoteFileName + " 成功从服务器上删除");
				return success;
			}
			ftpLogOut();
			if (ftpClient.isConnected()) {
				ftpClient.disconnect();
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(remoteFileName + " 删除失败");
		}
		if (success == false) {
			logger.error(remoteFileName + " 删除失败");
		}
		return success;
	}
	
//	/**
//	 * 
//	 * @param localPath		本地文件夹
//	 * @param remotePath	远程文件夹
//	 * @return
//	 */
//	public boolean uploadDirectory(String localPath, String remotePath) {
//		File src = new File(localPath);
//		try {
//			this.ftpClient.makeDirectory(remotePath);
//			remotePath = remotePath + src.getName() + "/";
//			this.ftpClient.makeDirectory(remotePath);
//		} catch (IOException e) {
//			e.printStackTrace();
//			logger.error(remotePath + " 目录创建失败");
//		}
//		File[] allFile = src.listFiles();
//		for (int currentFile = 0; currentFile < allFile.length; currentFile++) {
//			if (!allFile[currentFile].isDirectory()) {
//				String srcName = allFile[currentFile].getPath().toString();
//				uploadFile(new File(srcName), remotePath);
//			}
//		}
//		return true;
//	}
	
	/*public boolean mkdirRemoteDirectory(String remotePath) {
		try {
			this.ftpClient.makeDirectory(remotePath);
			logger.info(remotePath + " 目录创建成功");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			logger.info(remotePath + " 目录创建失败");
			return false;
		}
	}*/
	
	public boolean mkdirRemoteDirectory(String parentPath, String childPath) {
		File src = new File(childPath);
		try {
			this.ftpClient.makeDirectory(parentPath);
			logger.info(parentPath + " 目录创建成功");
			parentPath = parentPath + src.getName() + "/";
			this.ftpClient.makeDirectory(parentPath);
			logger.info(parentPath + " 目录创建成功");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			logger.info(" 目录创建失败"); 
			return false;
		}
	}

//	/**
//	 * 下载远程文件夹中的内容至本地文件夹
//	 * @param localPath		本地文件夹
//	 * @param remotePath	远程文件夹
//	 * @return
//	 */
//	public boolean downloadDirectory(String localPath, String remotePath) {
//		try {
//			String fileName = new File(remotePath).getName();
//			localPath = localPath + fileName + "//";
//			new File(localPath).mkdirs();
//			FTPFile[] allFile = this.ftpClient.listFiles(remotePath);
//			for (int currentFile = 0; currentFile < allFile.length; currentFile++) {
//				if (!allFile[currentFile].isDirectory()) {
//					downloadFile(allFile[currentFile].getName(), localPath, remotePath);
//				}
//			}
//			for (int currentFile = 0; currentFile < allFile.length; currentFile++) {
//				if (allFile[currentFile].isDirectory()) {
//					String strremoteDirectoryPath = remotePath + "/" + allFile[currentFile].getName();
//					downloadDirectory(localPath, strremoteDirectoryPath);
//				}
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//			logger.info("下载文件夹失败");
//			return false;
//		}
//		return true;
//	}
//
//	/**
//	 * 下载远程文件夹中的内容至本地文件夹
//	 * @param localPath		本地文件夹
//	 * @param remotePath	远程文件夹
//	 */
//	public void downloadDirectoryNew(String localPath, String remotePath) {
//		try {
//			File local = new File(localPath);
//			if (!local.exists()) {
//				local.mkdirs();
//			} else {
//				if (local.isDirectory()) {
//					String[] fileNames = this.ftpClient.listNames(remotePath);
//					if (fileNames != null && fileNames.length > 0) {
//						for (String fileName : fileNames) {
//							fileName = fileName.substring(fileName.lastIndexOf(File.separator) + 1);
//							boolean f = downloadFile(fileName, localPath, remotePath);
//							if (f) {
//								removeRemoteFile(fileName, remotePath);
//							}
//						}
//					} else {
//						logger.info(remotePath + " 目录下没有文件要下载");
//					}
//					/*FTPFile[] allFile = this.ftpClient.listFiles(remotePath);
//					for (int currentFile = 0; currentFile < allFile.length; currentFile++) {
//						if (!allFile[currentFile].isDirectory()) {
//							boolean f = downloadFile(allFile[currentFile].getName(), localPath, remotePath);
//							if (f) {
//								removeRemoteFile(allFile[currentFile].getName(), remotePath);
//							}
//						}
//					}
//					for (int currentFile = 0; currentFile < allFile.length; currentFile++) {
//						if (allFile[currentFile].isDirectory()) {
//							String strRemoteDirectoryPath = remotePath + File.separator + allFile[currentFile].getName();
//							downloadDirectoryNew(localPath, strRemoteDirectoryPath);
//						}
//					}*/
//				}
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//			logger.info("下载文件夹失败");
//		}
//	}
	
	/**
	 * 
	 * @param file
	 * @throws Exception
	 */
	public void upload(File file) throws Exception {
		if (file.isDirectory()) {
			ftpClient.makeDirectory(file.getName());
			ftpClient.changeWorkingDirectory(file.getName());
			String[] files = file.list();
			for (String file3 : files) {
				File file1 = new File(file.getPath() + "/" + file3);
				if (file1.isDirectory()) {
					upload(file1);
					ftpClient.changeToParentDirectory();
				} else {
					File file2 = new File(file.getPath() + "/" + file3);
					FileInputStream input = new FileInputStream(file2);
					ftpClient.storeFile(file2.getName(), input);
					input.close();
				}
			}
		} else {
			File file2 = new File(file.getPath());
			FileInputStream input = new FileInputStream(file2);
			ftpClient.storeFile(file2.getName(), input);
			input.close();
		}
	}

	public FTPClient getFtpClient() {
		return ftpClient;
	}

	public void setFtpClient(FTPClient ftpClient) {
		this.ftpClient = ftpClient;
	}

	public static void main(String[] args) throws IOException {
		try {
			//192.168.21.106 ||192.168.201.237
			/*FtpUtil ftp = new FtpUtil("192.168.21.106", 21, "at_dev", "qsQq#3Mx");
			boolean flag = ftp.ftpLogin();
			//String[] fileNames = ftp.ftpClient.listNames("/home/at_dev/cu/999");
			ftp.ftpClient.changeWorkingDirectory("/home/at_dev/cu/999");
			//ftp.ftpClient.mlistFile("/home/at_dev/cu/999");
			String[] fileNames = ftp.ftpClient.listNames();
			if(flag){
				System.out.println("ftp 登陆成功");
			}else{
				System.out.println("ftp 登陆失败");
			}
			long startTime = System.currentTimeMillis();
			ftp.downloadDirectoryNew("F:\\", "999");
			long endTime = System.currentTimeMillis();
			System.out.println((endTime - startTime) / 1000);
			ftp.ftpLogOut();*/
			/*String a = "1234";
			System.out.println(a.substring(a.length()-2,a.length()));*/
			FTPClient ftp = new FTPClient();
			ftp.connect("192.168.21.106", 21);
			ftp.login("at_dev", "qsQq#3Mx");// 登录  
            int reply = ftp.getReplyCode();  
            if (!FTPReply.isPositiveCompletion(reply)) {  
                ftp.disconnect();  
            } 
            ftp.changeWorkingDirectory("/home/at_dev/cu/999/");
            String[] names = ftp.listNames();
            //String[] fileNames = ftp.listNames("/home/at_dev/cu/999");
            System.out.println();
		} catch (Exception e) {
			System.out.print(e);
		}
	}
	
	
			
}
