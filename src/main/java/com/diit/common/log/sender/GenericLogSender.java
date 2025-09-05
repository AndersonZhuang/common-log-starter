package com.diit.common.log.sender;

import com.diit.common.log.entity.BaseLogEntity;

/**
 * 通用日志发送器接口
 * 支持任何继承自BaseLogEntity的实体类
 * 
 * @param <T> 日志实体类型，必须继承自BaseLogEntity
 * @author zzx
 */
public interface GenericLogSender<T extends BaseLogEntity> {
    
    /**
     * 发送日志
     * 
     * @param logEntity 日志实体
     */
    void send(T logEntity);
    
    /**
     * 异步发送日志
     * 
     * @param logEntity 日志实体
     */
    void sendAsync(T logEntity);
    
    /**
     * 批量发送日志
     * 
     * @param logEntities 日志实体列表
     */
    void sendBatch(java.util.List<T> logEntities);
    
    /**
     * 获取发送器类型
     * 
     * @return 发送器类型标识
     */
    String getSenderType();
    
    /**
     * 检查发送器是否支持指定的实体类型
     * 
     * @param entityClass 实体类型
     * @return 是否支持
     */
    boolean supports(Class<? extends BaseLogEntity> entityClass);
}