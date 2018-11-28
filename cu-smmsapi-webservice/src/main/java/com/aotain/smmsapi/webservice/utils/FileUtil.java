package com.aotain.smmsapi.webservice.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class FileUtil {
	private static Logger logger = Logger.getLogger(FileUtil.class);
	
	public static void listFile(String path) {
		File file = new File(path);
		File files[] = file.listFiles();
		for (int i = 0; i < files.length; i++) {

			if (!files[i].getName().contains(".tgz")) {
				continue;
			}
			logger.info(path + files[i].getName());
			deleteFile(path + files[i].getName());
		}

	}

	public static int count(String filename) {
		try {
			InputStream is = new BufferedInputStream(new FileInputStream(filename));
			byte[] c = new byte[1024];
			int count = 0;
			int readChars = 0;
			while ((readChars = is.read(c)) != -1) {
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n') {
						++count;
					}
				}
			}
			is.close();
			return count;
		} catch (Exception e) {
			return 0;
		}
	}

	public static void copyFile(String oldPath, String newPath) {
		try {
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists()) {
				InputStream inStream = new FileInputStream(oldPath); 
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				while ((byteread = inStream.read(buffer)) != -1) {
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
				fs.close();
			}
		} catch (Exception e) {

		}

	}

	public static void renameFile(String path1, String path2) {
		try {
			File oldFile = new File(path1);
			File newFile = null;
			if (oldFile != null) {
				newFile = new File(path2);
				oldFile.renameTo(newFile);
			}
		} catch (Exception ex) {
		}
	}

	public static boolean isExist(String filepath) throws Exception {
		File file = new File(filepath);
		return file.exists();
	}

	public static void moveFile(String resFilePath, String distFolder) {
		try {
			File resFile = new File(resFilePath);
			File distFile = new File(distFolder);
			if (resFile.isDirectory()) {
				FileUtils.moveDirectoryToDirectory(resFile, distFile, true);
			} else if (resFile.isFile()) {
				if (!distFile.exists()) {
					distFile.mkdir();
				}
				FileUtils.moveFileToDirectory(resFile, distFile, true);
			}
		} catch (Exception ex) {
			logger.error("move file from  " + resFilePath + " to " + distFolder + ex.getStackTrace());
			logger.error("**delete file=" + resFilePath);
			deleteFile(resFilePath); 
		}
	}
	
	public static boolean deleteFile(String fileName) {
		File file = new File(fileName);
		if (file.exists() && file.isFile()) {
			if (file.delete()) {
				return true;
			} else {
				logger.error("delete " + fileName + " failed!");
				return false;
			}
		} else {
			logger.error("delete " + fileName + " not exist!");
			return false;
		}
	}

	public static void writeFile(String fileName, String content) {
		try {
			RandomAccessFile rf = new RandomAccessFile(fileName, "rw");
			rf.seek(rf.length()); 
			rf.writeBytes(content);
			rf.close();
		} catch (IOException e) {
			logger.error("writeFile:"+e.getStackTrace());
		}
	}

	public static void reNameFile(String fileName, String newFileName) {
		try {
			File oldFile = new File(fileName);
			File newFile = null;
			if (oldFile != null) {
				newFile = new File(newFileName);
				oldFile.renameTo(newFile);
			}
		} catch (Exception e) {
			logger.error("reNameFile:"+e.getStackTrace());
		}
	}

	public static void readFileLine(String fileName) {
		File file = new File(fileName);
		LineNumberReader reader = null;
		FileReader in = null;
		try {
			in = new FileReader(file);
			reader = new LineNumberReader(in);
			String s = null;
			int i = 0;
			while (true) {
				s = reader.readLine();
				if (s == null)
					break;
				i++;

				if (i == 1)
					return;  

				System.out.println(s.replaceAll("\"", ""));
				String[] arr = s.replaceAll("\"", "").split("\\|\\|\\|");
				System.out.println(arr[2]);
			}
			System.out.println("count=" + i);
		} catch (FileNotFoundException e) {
			logger.error("readFileLine:"+e.getMessage());
		} catch (IOException e) {
			logger.error("readFileLine:"+e.getMessage());
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				logger.error("readFileLine:"+e.getStackTrace());
			}
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				logger.error("readFileLine:"+e.getStackTrace());
			}
		}

	}

	public static void list(File f, FileFilter filter, Set<File> set, int limit) {
		if (limit > -1 && set.size() >= limit) {
			return;
		}
		if (f == null) {
			return;
		}
		if (f.isFile()) {
			set.add(f);
		} else if (f.isDirectory()) {
			File[] files = null;
			if (filter == null) {
				files = f.listFiles();
			} else {
				files = f.listFiles(filter);
			}
			if (files != null) {
				for (File file : files) {
					list(file, filter, set, limit);
				}
			}
		}
	}

	public static void list(File f, FileFilter filter, Set<File> set) {
		if (f == null) {
			return;
		}
		if (f.isFile()) {
			set.add(f);
		} else if (f.isDirectory()) {
			File[] files = null;
			if (filter == null) {
				files = f.listFiles();
			} else {
				files = f.listFiles(filter);
			}
			if (files != null) {
				for (File file : files) {
					list(file, filter, set);
				}
			}
		}
	}

	public static List<String> listDir(String path) {
		List<String> list = new ArrayList<String>();
		File f = new File(path);
		if (f.isDirectory()) {
			File[] fList = f.listFiles();
			for (int j = 0; j < fList.length; j++) {
				if (fList[j].isDirectory()) {
					list.add(fList[j].getPath());
				}
			}
		}
		return list;
	}
	
	public static List<String> listDirsForLastModifyTime(String path) {
		List<String> result = new ArrayList<String>();
		List<File> temp = new ArrayList<File>();
		File f = new File(path);
		if (f.isDirectory()) {
			File[] fList = f.listFiles();
			for (int j = 0; j < fList.length; j++) {
				if (fList[j].isDirectory()) {
					temp.add(fList[j]);
				}
			}
		}
		Collections.sort(temp, new FileUtil.CompratorByLastModified());
		for (File file : temp) {
			result.add(file.getAbsolutePath());
		}
		return result;
	}

	public static boolean isDir(String path) {
		File file = new File(path);
		return file.isDirectory();
	}

	public static List<String> getDateDir(String basePath, int begin, int end) {
		if (begin == 0)
			begin = 20100910;
		List<String> list = new ArrayList<String>();
		for (int i = begin; i <= end; i++) {
			if (isDir(basePath + i)) {
				List<String> listSub = new ArrayList<String>();
				listSub = listDir(basePath + i);
				if (listSub.size() == 0)
					list.add(basePath + i);
				else {
					for (int m = 0; m < listSub.size(); m++) {
						list.add(listSub.get(m));
					}
				}
			}
		}
		return list;
	}

	public static void list(File f, FileFilter filter, String parent, Map<String, File> map) {
		if (f == null) {
			return;
		}
		String name = f.getName();
		if (parent != null) {
			name = parent + "/" + name;
		}
		if (f.isFile()) {
			map.put(name, f);
		} else if (f.isDirectory()) {
			File[] files = null;
			if (filter == null) {
				files = f.listFiles();
			} else {
				files = f.listFiles(filter);
			}
			if (files != null) {
				for (File file : files) {
					list(file, filter, name, map);
				}
			}
		}
	}

	public static void newFolder(String newfolder) {
		try {
			String filepath = newfolder;
			File myPath = new File(filepath);
			if (!myPath.exists()) {
				myPath.mkdir();
			}
		} catch (Exception e) {
			logger.error("newFolder:"+e.getStackTrace());
		}
	}

	public static String unGZIP(String inFilename, String key) {
		try {
			GZIPInputStream in = new GZIPInputStream(new FileInputStream(inFilename));
			String outFilename = inFilename.replace(key, "");
			OutputStream out = new FileOutputStream(outFilename);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			return outFilename;
		} catch (IOException e) {
			logger.error("unGZIP:"+e.getMessage()+",filename="+inFilename);
			return "";
		}
	}
	
	static class CompratorByLastModified implements Comparator<File> {
		public int compare(File f1, File f2) {
			long diff = f1.lastModified() - f2.lastModified();
			if (diff < 0) {
				return 1;
			} else if (diff == 0) {
				return 0;
			} else {
				return -1;
			}
		}
	}
}
