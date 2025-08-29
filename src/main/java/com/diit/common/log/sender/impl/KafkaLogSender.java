package com.diit.common.log.sender.impl;

import com.diit.common.log.entity.UserAccessLogEntity;
import com.diit.common.log.entity.OperationLogEntity;
import com.diit.common.log.properties.LogProperties;
import com.diit.common.log.sender.LogSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka日志发送器实现
 * 
 * @author diit
 */
@Slf4j
@Component
public class KafkaLogSender implements LogSender {
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    private LogProperties logProperties;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public void sendAccessLog(UserAccessLogEntity accessLog) {
        try {
            String message = objectMapper.writeValueAsString(accessLog);
            String key = accessLog.getUsername() != null ? accessLog.getUsername() : "unknown";
            String topic = logProperties.getKafka().getAccessLogTopic();
            
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, message);
            future.whenComplete((result, failure) -> {
                if (failure != null) {
                    log.error("发送访问日志到Kafka失败 - Topic:{}, Key:{}, Error:{}", 
                             topic, key, failure.getMessage());
                } else {
                    log.debug("成功发送访问日志到Kafka - Topic:{}, Key:{}", topic, key);
                }
            });
        } catch (Exception e) {
            log.error("序列化访问日志失败: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void sendOperationLog(OperationLogEntity operationLog) {
        try {
            String message = objectMapper.writeValueAsString(operationLog);
            String key = operationLog.getUsername() != null ? operationLog.getUsername() : "unknown";
            String topic = logProperties.getKafka().getOperationLogTopic();
            
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, message);
            future.whenComplete((result, failure) -> {
                if (failure != null) {
                    log.error("发送操作日志到Kafka失败 - Topic:{}, Key:{}, Error:{}", 
                             topic, key, failure.getMessage());
                } else {
                    log.debug("成功发送操作日志到Kafka - Topic:{}, Key:{}", topic, key);
                }
            });
        } catch (Exception e) {
            log.error("序列化操作日志失败: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public boolean supports(String logType) {
        return "kafka".equals(logType) && logProperties.getKafka().isEnabled();
    }
    
    @Override
    public String getName() {
        return "KafkaLogSender";
    }
}
