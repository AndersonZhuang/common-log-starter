package com.diit.common.log.sender.impl;

import com.diit.common.log.entity.BaseLogEntity;
import com.diit.common.log.properties.LogProperties;
import com.diit.common.log.sender.GenericLogSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * 统一HTTP发送器
 * 支持任何继承自BaseLogEntity的实体类，包括自定义字段
 * 
 * @author zzx
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "diit.log.http", name = "enabled", havingValue = "true", matchIfMissing = false)
public class UnifiedHttpSender implements GenericLogSender<BaseLogEntity> {
    
    @Autowired(required = false)
    private RestTemplate restTemplate;
    
    @Autowired
    private LogProperties logProperties;
    
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .findAndRegisterModules();
    
    // 默认HTTP端点配置
    private static final String DEFAULT_GENERIC_LOG_ENDPOINT = "http://localhost:8080/api/logs/receive";
    
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
            log.info("🚀 批量发送{}条日志到HTTP端点", logEntities.size());
            for (BaseLogEntity entity : logEntities) {
                send(entity);
            }
        } catch (Exception e) {
            log.error("批量发送日志到HTTP失败", e);
            throw new RuntimeException("Failed to batch send logs to HTTP", e);
        }
    }
    
    @Override
    public String getSenderType() {
        return "http";
    }
    
    @Override
    public boolean supports(Class<? extends BaseLogEntity> entityClass) {
        // HTTP发送器支持所有继承自BaseLogEntity的实体类
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
            
            // 确定HTTP端点 - 优先使用配置的端点
            String endpoint = getConfiguredEndpoint();
            
            // 将日志实体序列化为JSON（包含所有自定义字段）
            String json = objectMapper.writeValueAsString(logEntity);
            
            if (restTemplate != null) {
                // 真实发送HTTP请求
                sendHttpRequest(endpoint, json, logEntity);
            } else {
                // 模拟模式
                log.warn("⚠️ RestTemplate不可用，使用模拟模式:");
                log.info("   Endpoint: {}", endpoint);
                log.info("   实体类型: {}", logEntity.getClass().getSimpleName());
                log.info("   自定义字段: {}", hasCustomFields(logEntity) ? "是" : "否");
                log.info("   JSON: {}", json);
            }
            
        } catch (Exception e) {
            log.error("HTTP发送日志失败", e);
            throw new RuntimeException("Failed to send log to HTTP", e);
        }
    }
    
    /**
     * 发送HTTP请求
     */
    private void sendHttpRequest(String endpoint, String json, BaseLogEntity logEntity) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Entity-Type", logEntity.getClass().getSimpleName());
            headers.set("X-Log-Source", "UnifiedHttpSender");
            
            HttpEntity<String> request = new HttpEntity<>(json, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ HTTP日志发送成功 - Endpoint: {}, Status: {}", 
                        endpoint, response.getStatusCode());
                log.debug("   Entity: {}, Custom Fields: {}", 
                         logEntity.getClass().getSimpleName(), 
                         hasCustomFields(logEntity) ? "是" : "否");
            } else {
                log.warn("⚠️ HTTP日志发送失败 - Endpoint: {}, Status: {}", 
                        endpoint, response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("❌ HTTP请求异常 - Endpoint: {}, Error: {}", endpoint, e.getMessage());
        }
    }
    
    /**
     * 检查实体是否包含自定义字段
     */
    private boolean hasCustomFields(BaseLogEntity logEntity) {
        // 如果不是DefaultLogEntity，则认为包含自定义字段
        return !"DefaultLogEntity".equals(logEntity.getClass().getSimpleName());
    }
    
    /**
     * 获取配置的HTTP端点
     */
    private String getConfiguredEndpoint() {
        if (logProperties != null && logProperties.getHttp() != null) {
            // 优先使用accessLogEndpoint，如果没有则使用operationLogEndpoint
            String endpoint = logProperties.getHttp().getAccessLogEndpoint();
            if (endpoint == null || endpoint.trim().isEmpty()) {
                endpoint = logProperties.getHttp().getOperationLogEndpoint();
            }
            if (endpoint != null && !endpoint.trim().isEmpty()) {
                log.debug("使用配置的HTTP端点: {}", endpoint);
                return endpoint;
            }
        }
        
        log.debug("使用默认HTTP端点: {}", DEFAULT_GENERIC_LOG_ENDPOINT);
        return DEFAULT_GENERIC_LOG_ENDPOINT;
    }
}
