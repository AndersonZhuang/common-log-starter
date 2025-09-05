package com.diit.common.log.sender;

import com.diit.common.log.entity.UserAccessLogEntity;
import com.diit.common.log.entity.OperationLogEntity;

/**
 * 日志发送器接口
 * 
 * @author zzx
 */
public interface LogSender {
    
    /**
     * 发送访问日志
     * 
     * @param log 访问日志实体
     */
    void sendAccessLog(UserAccessLogEntity log);
    
    /**
     * 发送操作日志
     * 
     * @param log 操作日志实体
     */
    void sendOperationLog(OperationLogEntity log);
    
    /**
     * 检查是否支持指定的日志类型
     * 
     * @param logType 日志类型
     * @return 是否支持
     */
    boolean supports(String logType);
    
    /**
     * 获取发送器名称
     * 
     * @return 发送器名称
     */
    String getName();
}
