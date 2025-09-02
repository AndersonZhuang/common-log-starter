package com.diit.common.log.sender.impl;

import com.diit.common.log.entity.BaseLogEntity;
import com.diit.common.log.sender.GenericLogSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 统一Kafka发送器
 * 支持任何继承自BaseLogEntity的实体类，包括自定义字段
 * 
 * @author diit
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "diit.log.kafka", name = "enabled", havingValue = "true", matchIfMissing = false)
public class UnifiedKafkaSender implements GenericLogSender<BaseLogEntity> {
    
    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .findAndRegisterModules();
    
    // ==================== GenericLogSender接口实现 ====================
    
    @Override
    public void send(BaseLogEntity logEntity) {
        sendLogEntity(logEntity, "generic");
    }
    
    @Override
    @Async("logExecutor")
    public void sendAsync(BaseLogEntity logEntity) {
        send(logEntity);
    }
    
    @Override
    public void sendBatch(List<BaseLogEntity> logEntities) {
        if (logEntities == null || logEntities.isEmpty()) {
            return;
        }
        
        try {
            log.info("🚀 批量发送{}条日志到Kafka", logEntities.size());
            for (BaseLogEntity entity : logEntities) {
                send(entity);
            }
        } catch (Exception e) {
            log.error("批量发送日志到Kafka失败", e);
            throw new RuntimeException("Failed to batch send logs to Kafka", e);
        }
    }
    
    @Override
    public String getSenderType() {
        return "kafka";
    }
    
    @Override
    public boolean supports(Class<? extends BaseLogEntity> entityClass) {
        // 支持所有继承自BaseLogEntity的实体类
        return BaseLogEntity.class.isAssignableFrom(entityClass);
    }
    
    // ==================== 核心发送逻辑 ====================
    
    /**
     * 统一的日志发送方法
     * 支持任何日志实体类，包括自定义字段
     * 
     * @param logEntity 日志实体
     * @param logCategory 日志分类（access/operation/generic）
     */
    private void sendLogEntity(BaseLogEntity logEntity, String logCategory) {
        try {
            // 将日志实体序列化为JSON（包含所有自定义字段）
            String message = objectMapper.writeValueAsString(logEntity);
            
            // 生成消息key
            String key = generateMessageKey(logEntity);
            
            // 确定Topic名称
            String topic = generateTopicName(logEntity, logCategory);
            
            if (kafkaTemplate != null) {
                // 真实发送到Kafka
                CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, message);
                
                future.whenComplete((result, failure) -> {
                    if (failure != null) {
                        log.error("❌ Kafka日志发送失败 - Topic: {}, Key: {}, Error: {}", 
                                 topic, key, failure.getMessage());
                    } else {
                        log.info("✅ Kafka日志发送成功 - Topic: {}, Key: {}, Partition: {}, Offset: {}", 
                                topic, key, 
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
                
                // 显示发送详情
                log.info("🚀 发送日志到Kafka:");
                log.info("   Topic: {}", topic);
                log.info("   Key: {}", key);
                log.info("   Category: {}", logCategory);
                log.info("   实体类型: {}", logEntity.getClass().getSimpleName());
                log.info("   自定义字段: {}", hasCustomFields(logEntity) ? "是" : "否");
                log.debug("   Message: {}", message);
                
            } else {
                // 模拟模式（KafkaTemplate不可用时）
                log.warn("⚠️ KafkaTemplate不可用，使用模拟模式:");
                log.info("   Topic: {}", topic);
                log.info("   Key: {}", key);
                log.info("   Category: {}", logCategory);
                log.info("   实体类型: {}", logEntity.getClass().getSimpleName());
                log.info("   自定义字段: {}", hasCustomFields(logEntity) ? "是" : "否");
                log.info("   Message: {}", message);
            }
            
        } catch (Exception e) {
            log.error("Kafka发送日志失败", e);
            throw new RuntimeException("Failed to send log to Kafka", e);
        }
    }
    
    /**
     * 生成消息Key
     */
    private String generateMessageKey(BaseLogEntity logEntity) {
        if (logEntity.getUsername() != null) {
            return logEntity.getUsername();
        }
        if (logEntity.getId() != null) {
            return logEntity.getId();
        }
        return "unknown";
    }
    
    /**
     * 根据实体类型和分类生成Topic名称
     */
    private String generateTopicName(BaseLogEntity logEntity, String logCategory) {
        String entityName = logEntity.getClass().getSimpleName();
        
        // 对于自定义实体，根据实体类型生成topic
        String topicName = entityName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
        return "log_" + topicName;
    }
    
    /**
     * 检查实体是否包含自定义字段
     */
    private boolean hasCustomFields(BaseLogEntity logEntity) {
        // 如果不是DefaultLogEntity，则认为包含自定义字段
        return !"DefaultLogEntity".equals(logEntity.getClass().getSimpleName());
    }
}
