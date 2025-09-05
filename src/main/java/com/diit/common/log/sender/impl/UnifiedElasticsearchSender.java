package com.diit.common.log.sender.impl;

import com.diit.common.log.entity.BaseLogEntity;
import com.diit.common.log.sender.GenericLogSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 统一Elasticsearch发送器
 * 支持任何继承自BaseLogEntity的实体类，包括自定义字段
 * 
 * @author zzx
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "diit.log.elasticsearch", name = "enabled", havingValue = "true", matchIfMissing = false)
public class UnifiedElasticsearchSender implements GenericLogSender<BaseLogEntity> {
    
    @Autowired(required = false)
    private RestTemplate restTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .findAndRegisterModules();
    
    private static final String ES_BASE_URL = "http://localhost:9200";
    
    @Override
    public void send(BaseLogEntity logEntity) {
        sendGenericLog(logEntity);
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
            log.info("🚀 批量发送{}条日志到Elasticsearch", logEntities.size());
            for (BaseLogEntity entity : logEntities) {
                send(entity);
            }
        } catch (Exception e) {
            log.error("批量发送日志到Elasticsearch失败", e);
            throw new RuntimeException("Failed to batch send logs to Elasticsearch", e);
        }
    }
    
    @Override
    public String getSenderType() {
        return "elasticsearch";
    }
    
    @Override
    public boolean supports(Class<? extends BaseLogEntity> entityClass) {
        // ES发送器支持所有继承自BaseLogEntity的实体类
        return BaseLogEntity.class.isAssignableFrom(entityClass);
    }
    
    /**
     * 统一的日志发送方法
     * 支持任何继承自BaseLogEntity的实体类，包括自定义字段
     */
    private void sendGenericLog(BaseLogEntity logEntity) {
        try {
            // 确保基础字段被正确设置（只在为null时设置）
            if (logEntity.getTimestamp() == null) {
                logEntity.setTimestamp(java.time.LocalDateTime.now());
            }
            if (logEntity.getContent() == null) {
                logEntity.setContent("操作记录");
            }
            if (logEntity.getLevel() == null) {
                logEntity.setLevel(org.springframework.boot.logging.LogLevel.INFO);
            }
            
            // 生成ES索引名称
            String indexName = generateIndexName(logEntity);
            
            // 生成文档ID
            String documentId = logEntity.getId();
            
            // 构建ES文档（包含所有字段，包括自定义字段）
            String jsonDocument = objectMapper.writeValueAsString(logEntity);
            
            if (restTemplate != null) {
                // 真实发送到Elasticsearch
                // 使用POST方法创建文档，让Elasticsearch自动生成ID
                String url = String.format("%s/%s/_doc", ES_BASE_URL, indexName);
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> request = new HttpEntity<>(jsonDocument, headers);
                
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> response = restTemplate.exchange(
                            url, HttpMethod.POST, request, Map.class).getBody();
                    
                    log.info("✅ Elasticsearch日志发送成功 - Index: {}, ID: {}", indexName, documentId);
                    log.debug("   Response: {}", response);
                    
                } catch (Exception e) {
                    log.error("❌ Elasticsearch日志发送失败 - Index: {}, ID: {}, Error: {}", 
                             indexName, documentId, e.getMessage());
                }
                
            } else {
                // 模拟模式
                log.warn("⚠️ RestTemplate不可用，使用模拟模式:");
                log.info("   Index: {}", indexName);
                log.info("   Document ID: {}", documentId);
                log.info("   实体类型: {}", logEntity.getClass().getSimpleName());
                log.info("   自定义字段: {}", hasCustomFields(logEntity) ? "是" : "否");
                log.info("   Document: {}", jsonDocument);
            }
            
        } catch (Exception e) {
            log.error("Elasticsearch发送日志失败", e);
            throw new RuntimeException("Failed to send log to Elasticsearch", e);
        }
    }
    
    /**
     * 生成ES索引名称
     * 格式：logs-{entityType}-YYYY-MM
     */
    private String generateIndexName(BaseLogEntity logEntity) {
        String entityType = logEntity.getClass().getSimpleName()
                .replaceAll("LogEntity", "")
                .toLowerCase();
        
        String yearMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        
        return String.format("logs-%s-%s", entityType, yearMonth);
    }
    
    /**
     * 检查实体是否包含自定义字段
     */
    private boolean hasCustomFields(BaseLogEntity logEntity) {
        // 如果不是DefaultLogEntity，则认为包含自定义字段
        return !"DefaultLogEntity".equals(logEntity.getClass().getSimpleName());
    }
}
