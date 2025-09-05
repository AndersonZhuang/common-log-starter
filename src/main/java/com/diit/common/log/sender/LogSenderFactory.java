package com.diit.common.log.sender;

import com.diit.common.log.entity.BaseLogEntity;
import com.diit.common.log.properties.LogProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日志发送器工厂
 * 管理传统LogSender和通用GenericLogSender
 * 
 * @author zzx
 */
@Slf4j
@Component
public class LogSenderFactory {
    
    @Autowired(required = false)
    private List<LogSender> logSenders;
    
    @Autowired(required = false)
    private List<GenericLogSender<? extends BaseLogEntity>> genericLogSenders;
    
    @Autowired
    private LogProperties logProperties;
    
    private final Map<String, LogSender> senderCache = new ConcurrentHashMap<>();
    private final Map<String, GenericLogSender<? extends BaseLogEntity>> genericSenderCache = new ConcurrentHashMap<>();
    
    /**
     * 获取访问日志发送器
     * 
     * @return 访问日志发送器
     */
    public LogSender getAccessLogSender() {
        return getLogSender("access");
    }
    
    /**
     * 获取操作日志发送器
     * 
     * @return 操作日志发送器
     */
    public LogSender getOperationLogSender() {
        return getLogSender("operation");
    }
    
    /**
     * 获取日志发送器
     * 
     * @param logType 日志类型
     * @return 日志发送器
     */
    public LogSender getLogSender(String logType) {
        String storageType = logProperties.getStorage().getType();
        
        return senderCache.computeIfAbsent(storageType, type -> {
            if (logSenders == null || logSenders.isEmpty()) {
                log.warn("没有可用的日志发送器，使用NoOpLogSender");
                return new NoOpLogSender();
            }
            
            for (LogSender sender : logSenders) {
                if (sender.supports(type)) {
                    log.info("选择日志发送器: {} 用于类型: {}", sender.getName(), type);
                    return sender;
                }
            }
            
            log.warn("未找到支持的日志发送器，类型: {}", type);
            return new NoOpLogSender();
        });
    }
    
    /**
     * 获取通用日志发送器
     * 
     * @param senderType 发送器类型
     * @return 通用日志发送器
     */
    public GenericLogSender<? extends BaseLogEntity> getGenericLogSender(String senderType) {
        if (genericLogSenders == null || genericLogSenders.isEmpty()) {
            log.warn("没有可用的通用日志发送器，使用NoOpGenericLogSender");
            return new NoOpGenericLogSender();
        }
        
        // 如果指定了发送器类型，优先使用指定的
        if (senderType != null && !senderType.trim().isEmpty()) {
            return genericSenderCache.computeIfAbsent(senderType, type -> {
                for (GenericLogSender<? extends BaseLogEntity> sender : genericLogSenders) {
                    if (type.equals(sender.getSenderType())) {
                        log.info("选择指定通用发送器: {} -> {}", type, sender.getClass().getSimpleName());
                        return sender;
                    }
                }
                log.warn("未找到指定的通用发送器类型: {}", type);
                return new NoOpGenericLogSender();
            });
        }
        
        // 使用配置中的默认发送器类型
        String defaultType = logProperties.getStorage().getType();
        return genericSenderCache.computeIfAbsent(defaultType, type -> {
            for (GenericLogSender<? extends BaseLogEntity> sender : genericLogSenders) {
                if (type.equals(sender.getSenderType())) {
                    log.info("选择默认通用发送器: {} -> {}", type, sender.getClass().getSimpleName());
                    return sender;
                }
            }
            
            log.warn("未找到支持的通用日志发送器，类型: {}", type);
            return new NoOpGenericLogSender();
        });
    }
    
    /**
     * 获取所有可用的日志发送器
     * 
     * @return 日志发送器列表
     */
    public List<LogSender> getAllSenders() {
        return logSenders != null ? logSenders : java.util.Collections.emptyList();
    }
    
    /**
     * 获取所有可用的通用日志发送器
     * 
     * @return 通用日志发送器列表
     */
    public List<GenericLogSender<? extends BaseLogEntity>> getAllGenericSenders() {
        return genericLogSenders != null ? genericLogSenders : java.util.Collections.emptyList();
    }
    
    /**
     * 检查是否存在指定类型的发送器
     * 
     * @param senderType 发送器类型
     * @return 是否存在
     */
    public boolean hasSender(String senderType) {
        if (genericLogSenders == null || genericLogSenders.isEmpty()) {
            return false;
        }
        return genericLogSenders.stream()
                .anyMatch(sender -> senderType.equals(sender.getSenderType()));
    }
    
    /** todo
     * 无操作日志发送器（兜底实现）
     */
    private static class NoOpLogSender implements LogSender {
        @Override
        public void sendAccessLog(com.diit.common.log.entity.UserAccessLogEntity log) {
            // 不执行任何操作
        }
        
        @Override
        public void sendOperationLog(com.diit.common.log.entity.OperationLogEntity log) {
            // 不执行任何操作
        }
        
        @Override
        public boolean supports(String logType) {
            return false;
        }
        
        @Override
        public String getName() {
            return "NoOpLogSender";
        }
    }
    
    /**
     * 无操作通用日志发送器（兜底实现）
     */
    private static class NoOpGenericLogSender implements GenericLogSender<BaseLogEntity> {
        @Override
        public void send(BaseLogEntity logEntity) {
            // 不执行任何操作
        }
        
        @Override
        public void sendAsync(BaseLogEntity logEntity) {
            // 不执行任何操作
        }
        
        @Override
        public void sendBatch(java.util.List<BaseLogEntity> logEntities) {
            // 不执行任何操作
        }
        
        @Override
        public String getSenderType() {
            return "noop";
        }
        
        @Override
        public boolean supports(Class<? extends BaseLogEntity> entityClass) {
            return true; // 支持所有类型，但不执行任何操作
        }
    }
}
