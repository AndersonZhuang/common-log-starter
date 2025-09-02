package com.diit.common.log.sender.impl;

import com.diit.common.log.entity.BaseLogEntity;
import com.diit.common.log.sender.GenericLogSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;

/**
 * 统一数据库发送器
 * 支持任何继承自BaseLogEntity的实体类，包括自定义字段
 * 使用动态表结构存储自定义字段
 * 
 * @author diit
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "diit.log.database", name = "enabled", havingValue = "true", matchIfMissing = false)
public class UnifiedDatabaseSender implements GenericLogSender<BaseLogEntity> {
    
    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;
    
    // 基础字段的插入SQL
    private static final String BASE_INSERT_SQL = """
        INSERT INTO common_logs (id, username, description, client_ip, status, create_time, 
                                module, target, operation_type, exception_message, 
                                entity_type, custom_fields_json) 
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
    
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
            log.info("🚀 批量保存{}条日志到数据库", logEntities.size());
            for (BaseLogEntity entity : logEntities) {
                send(entity);
            }
        } catch (Exception e) {
            log.error("批量保存日志到数据库失败", e);
            throw new RuntimeException("Failed to batch save logs to database", e);
        }
    }
    
    @Override
    public String getSenderType() {
        return "database";
    }
    
    @Override
    public boolean supports(Class<? extends BaseLogEntity> entityClass) {
        // 数据库发送器支持所有继承自BaseLogEntity的实体类
        return BaseLogEntity.class.isAssignableFrom(entityClass);
    }
    
    /**
     * 统一的日志发送方法
     * 支持任何继承自BaseLogEntity的实体类，包括自定义字段
     */
    private void sendGenericLog(BaseLogEntity logEntity) {
        try {
            if (jdbcTemplate != null) {
                // 真实插入到数据库
                int rowsAffected = insertGenericLog(logEntity);
                
                if (rowsAffected > 0) {
                    log.info("✅ 数据库日志保存成功 - 插入记录ID: {}, 类型: {}", 
                            logEntity.getId(), logEntity.getClass().getSimpleName());
                    log.debug("   自定义字段: {}", hasCustomFields(logEntity) ? "是" : "否");
                } else {
                    log.warn("⚠️ 数据库日志插入未生效 - ID: {}", logEntity.getId());
                }
                
            } else {
                // 模拟模式
                log.warn("⚠️ JdbcTemplate不可用，使用模拟模式:");
                log.info("   实体类型: {}", logEntity.getClass().getSimpleName());
                log.info("   自定义字段: {}", hasCustomFields(logEntity) ? "是" : "否");
                log.info("   ID: {}, User: {}, Status: {}", 
                        logEntity.getId(), logEntity.getUsername(), logEntity.getStatus());
            }
            
        } catch (Exception e) {
            log.error("数据库保存日志失败 - ID: {}", logEntity.getId(), e);
            throw new RuntimeException("Failed to save log to database", e);
        }
    }
    
    /**
     * 插入通用日志（支持自定义字段）
     */
    private int insertGenericLog(BaseLogEntity logEntity) {
        // 获取自定义字段的JSON表示
        String customFieldsJson = extractCustomFieldsAsJson(logEntity);
        
        return jdbcTemplate.update(BASE_INSERT_SQL,
                logEntity.getId(),
                logEntity.getUsername(),
                logEntity.getDescription(),
                logEntity.getClientIp(),
                logEntity.getStatus(),
                logEntity.getCreateTime() != null ? 
                    Timestamp.valueOf(logEntity.getCreateTime()) : null,
                getFieldValue(logEntity, "module"),
                getFieldValue(logEntity, "target"),
                getFieldValue(logEntity, "operationType"),
                getFieldValue(logEntity, "exceptionMessage"),
                logEntity.getClass().getSimpleName(), // 实体类型
                customFieldsJson // 自定义字段JSON
        );
    }
    
    /**
     * 提取自定义字段为JSON字符串
     */
    private String extractCustomFieldsAsJson(BaseLogEntity logEntity) {
        try {
            // 使用反射获取所有字段
            java.lang.reflect.Field[] fields = logEntity.getClass().getDeclaredFields();
            java.util.Map<String, Object> customFields = new java.util.HashMap<>();
            
            for (java.lang.reflect.Field field : fields) {
                // 跳过BaseLogEntity中的基础字段
                if (isBaseField(field.getName())) {
                    continue;
                }
                
                field.setAccessible(true);
                Object value = field.get(logEntity);
                if (value != null) {
                    customFields.put(field.getName(), value);
                }
            }
            
            if (customFields.isEmpty()) {
                return null;
            }
            
            // 使用简单的JSON序列化
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(customFields);
            
        } catch (Exception e) {
            log.debug("提取自定义字段失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 检查是否为BaseLogEntity的基础字段
     */
    private boolean isBaseField(String fieldName) {
        return fieldName.equals("id") || fieldName.equals("username") || 
               fieldName.equals("description") || fieldName.equals("clientIp") ||
               fieldName.equals("status") || fieldName.equals("createTime") ||
               fieldName.equals("module") || fieldName.equals("target") ||
               fieldName.equals("operationType") || fieldName.equals("exceptionMessage");
    }
    
    /**
     * 安全地获取字段值
     */
    private Object getFieldValue(Object obj, String fieldName) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException e) {
            return null;
        } catch (Exception e) {
            log.debug("获取字段值失败: {}={}", fieldName, e.getMessage());
            return null;
        }
    }
    
    /**
     * 检查实体是否包含自定义字段
     */
    private boolean hasCustomFields(BaseLogEntity logEntity) {
        // 如果不是DefaultLogEntity，则认为包含自定义字段
        return !"DefaultLogEntity".equals(logEntity.getClass().getSimpleName());
    }
}
