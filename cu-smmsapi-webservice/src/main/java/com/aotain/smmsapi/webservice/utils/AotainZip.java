package com.aotain.smmsapi.webservice.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.tools.zip.ZipOutputStream;
import org.apache.log4j.Logger;

public class AotainZip {
	private static Logger logger = Logger.getLogger(AotainZip.class);
	
	public static void doZip(ByteArrayOutputStream buffer, String dirName,
			String fileName) {
		CreateDir(dirName);
		try {
			ZipOutputStream zipOut = new ZipOutputStream( new BufferedOutputStream(new FileOutputStream(dirName + fileName + ".zip")));
			zipOut.putNextEntry(new org.apache.tools.zip.ZipEntry(fileName + ".xml"));
			zipOut.setEncoding("UTF-8");
			zipOut.write(buffer.toByteArray());
			zipOut.close();
		} catch (IOException ioe) {
			logger.error("dozip error! dirName=" + dirName + ", fileName=" + fileName, ioe);
		}
	}

	private static void CreateDir(String dirName) {
		File file = new File(dirName);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	public static int unZip(byte[] buf, String dirName) {
		int result = 0;
		FileOutputStream fileOut;
		File file;

		CreateDir(dirName);
		ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(buf));
		ZipEntry entry;
		try {
			while ((entry = zis.getNextEntry()) != null) {
				file = new File(dirName + entry.getName());

				if (entry.isDirectory()) {
					file.mkdirs();
				} else {
					File parent = file.getParentFile();
					if (parent != null && !parent.exists()) {
						parent.mkdirs();
					}
					int readedBytes;
					byte[] buffer = new byte[512];
					fileOut = new FileOutputStream(file);
					while ((readedBytes = zis.read(buffer)) > 0) {
						fileOut.write(buffer, 0, readedBytes);
					}
					fileOut.close();
					zis.closeEntry();
				}
			}
		} catch (IOException ioe) {
			logger.error(ioe.toString());
			result = -100;
		} finally {
			try {
				zis.close();
			} catch (IOException ioe) {
				logger.error(ioe.toString());
				result = -200;
			}
		}

		return result;
	}

	public static String unZipReturnFileName(byte[] buf, String dirName) {
		String result = null;
		FileOutputStream fileOut;
		File file;
		CreateDir(dirName);
		ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(buf));
		try {
			java.util.zip.ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				result = entry.getName();
				file = new File(dirName + entry.getName());
				if (entry.isDirectory()) {
					file.mkdirs();
				} else {
					File parent = file.getParentFile();
					if (parent != null && !parent.exists()) {
						parent.mkdirs();
					}
					int readedBytes;
					byte[] buffer = new byte[512];
					fileOut = new FileOutputStream(file);
					while ((readedBytes = zis.read(buffer)) > 0) {
						fileOut.write(buffer, 0, readedBytes);
					}
					fileOut.close();

	                    zis.closeEntry();
		            } 
	        	}
	        }
	        catch(Exception e){ 
	        	logger.error(e.toString());
	            result = null;
	        } 
	        finally{
	        	try{
	        		zis.close();
	        	}
	        	 catch(Exception e){ 
	        		 logger.error(e.toString());
	 	        } 
	        }
	        
	      return result;
	}
}
