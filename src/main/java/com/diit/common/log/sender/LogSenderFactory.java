package com.diit.common.log.sender;

import com.diit.common.log.properties.LogProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日志发送器工厂
 * 
 * @author diit
 */
@Slf4j
@Component
public class LogSenderFactory {
    
    @Autowired
    private List<LogSender> logSenders;
    
    @Autowired
    private LogProperties logProperties;
    
    private final Map<String, LogSender> senderCache = new ConcurrentHashMap<>();
    
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
     * 获取所有可用的日志发送器
     * 
     * @return 日志发送器列表
     */
    public List<LogSender> getAllSenders() {
        return logSenders;
    }
    
    /**
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
}
