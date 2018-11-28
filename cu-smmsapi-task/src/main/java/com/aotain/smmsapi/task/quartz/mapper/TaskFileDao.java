package com.aotain.smmsapi.task.quartz.mapper;

import com.aotain.common.config.annotation.MyBatisDao;
import com.aotain.common.utils.model.msg.FileUploadInfo;

@MyBatisDao
public interface TaskFileDao {

	int updateFileStatus(FileUploadInfo upload);

}
