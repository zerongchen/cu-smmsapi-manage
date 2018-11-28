package com.aotain.smmsapi.task.smmsreturn.mapper;

import org.apache.ibatis.annotations.Param;

import com.aotain.common.config.annotation.MyBatisDao;
import com.aotain.common.utils.model.report.SmmsResultCache;
import com.aotain.cu.serviceapi.model.WaitApproveProcess;
import com.aotain.smmsapi.task.bean.IsmsWaitSubmitReportFile;

/**
 * 提交上报文件表数据库操作接口
 * 
 * @author liuz@aotian.com
 * @date 2018年8月16日 下午1:31:22
 */
@MyBatisDao
public interface SubmitReportFileDao {

	/**
	 * 查询否个文件的上报信息
	 * @param fileName
	 * @return
	 */
	public IsmsWaitSubmitReportFile getReportFileInfo(@Param("fileName")String fileName);

	/**
	 * 更新提交上报文件表处理状态
	 * @param dealFlag 上报文件名
	 * @param dealFlag 0-生成文件，1-上报成功，2-上报失败
	 * @return
	 */
	public int updateDealFlag(@Param("fileName")String reportFileName, @Param("dealFlag")int dealFlag);

	/**
	 * 查询某个submitId下某个状态文件的个数
	 * @param submitId 
	 * @param dealFlag 0-文件已生成，1-上报失败，2-上报成功，其它值-查询所有状态的文件
	 * @return
	 */
	public int countSubmitFile(@Param("submitId")Long submitId, @Param("dealFlag")int dealFlag);

	/**
	 * 更新提交上报日志表的状态
	 * @param submitId
	 * @param dealFlag 处理状态（0-未处理, 1-处理上报中, 2-处理上报失败, 3-处理上报成功）
	 * @return
	 */
	public int updateSubmitLogStatus(@Param("submitId")Long submitId, @Param("dealFlag")int dealFlag);

	/**
	 * 更新管局返回的999信息
	 * @param fileName
	 * @param cacheStr json 字符串，对象定义见 {@link SmmsResultCache}
	 */
	public void updateReturnInfor(@Param("fileName")String fileName, @Param("cacheStr")String cacheStr);
	
	/**
	 * 文件上报流水表
	 * @param domain
	 * @return
	 */
	int insertApproveProcess(WaitApproveProcess domain);

	int updateApproveProcess(WaitApproveProcess domain);

	int getApproveProcess(WaitApproveProcess domain);
}
