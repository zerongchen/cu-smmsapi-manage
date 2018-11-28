package com.aotain.smmsapi.task;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import com.aotain.common.utils.model.msg.RedisTaskStatus;

public class FtpTest {
	public static void main(String[] args) throws SocketException, IOException {
		FTPClient ftp = new FTPClient();
		List<RedisTaskStatus> taskIdList = new ArrayList<RedisTaskStatus>();
		RedisTaskStatus redisTaskStatus = null;
		ftp.connect("192.168.21.162", 21);
		ftp.login("smmsuser", "change");// 登录   Smmsuser change
        int reply = ftp.getReplyCode();  
        if (!FTPReply.isPositiveCompletion(reply)) {  
            ftp.disconnect();  
            System.out.println("登入失败");
            return;
        } 
        System.out.println("登入成功");
        FTPFile[]  files = ftp.listFiles();
        if(files == null || files.length == 0){
        	System.out.println("目录为空");
        	return;
        }
        for(FTPFile file : files){
        	System.out.println(file.getName());
        }
	}
}
