package com.diit.common.log.sender.impl;

import com.diit.common.log.database.LogTableManager;
import com.diit.common.log.database.PresetLogTableManager;
import com.diit.common.log.entity.BaseLogEntity;
import com.diit.common.log.entity.OperationLogEntity;
import com.diit.common.log.entity.UserAccessLogEntity;
import com.diit.common.log.exception.LogResultCode;
import com.diit.common.log.exception.LogSenderException;
import com.diit.common.log.sender.GenericLogSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    
    @Autowired(required = false)
    private LogTableManager logTableManager;
    
    @Autowired(required = false)
    private PresetLogTableManager presetLogTableManager;
    
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
            throw new LogSenderException(LogResultCode.DB_INSERT_FAILED, 
                "批量保存失败", e);
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
     * 检查是否为预设实体类
     */
    public boolean isPresetEntity(Class<?> entityClass) {
        return entityClass == OperationLogEntity.class || 
               entityClass == UserAccessLogEntity.class;
    }
    
    /**
     * 统一的日志发送方法
     * 支持任何继承自BaseLogEntity的实体类，包括自定义字段
     * 自动创建对应的数据库表
     */
    private void sendGenericLog(BaseLogEntity logEntity) {
        try {
            if (jdbcTemplate == null) {
                throw new LogSenderException(LogResultCode.DB_SENDER_CONFIG_ERROR, 
                    "JdbcTemplate未配置，请检查数据源配置");
            }
            
            // 检查是否为预设实体类
            if (isPresetEntity(logEntity.getClass())) {
                sendPresetEntity(logEntity);
            } else {
                sendBaseEntity(logEntity);
            }
            
        } catch (LogSenderException e) {
            throw e;
        } catch (Exception e) {
            log.error("数据库保存日志失败 - ID: {}", logEntity.getId(), e);
            throw new LogSenderException(LogResultCode.DB_INSERT_FAILED, 
                "数据库保存日志失败", e);
        }
    }
    
    /**
     * 发送预设实体类日志
     */
    private void sendPresetEntity(BaseLogEntity logEntity) {
        if (presetLogTableManager == null) {
            throw new LogSenderException(LogResultCode.DB_SENDER_CONFIG_ERROR, 
                "PresetLogTableManager未配置，请检查配置");
        }
        
        // 确保表存在，如果不存在则创建
        String tableName = presetLogTableManager.ensureTableExists(logEntity.getClass());
        if (tableName == null) {
            throw new LogSenderException(LogResultCode.DB_SENDER_CONFIG_ERROR, 
                "无法确定预设表名");
        }
        
        // 获取表信息和插入SQL
        PresetLogTableManager.TableInfo tableInfo = presetLogTableManager.getTableInfo(tableName, logEntity.getClass());
        
        // 动态插入数据
        int rowsAffected = insertPresetEntityData(logEntity, tableInfo);
        
        if (rowsAffected > 0) {
            log.info("✅ 预设实体数据库日志保存成功 - 表: {}, ID: {}, 类型: {}", 
                    tableName, logEntity.getId(), logEntity.getClass().getSimpleName());
        } else {
            log.warn("⚠️ 预设实体数据库日志插入未生效 - 表: {}, ID: {}", tableName, logEntity.getId());
        }
    }
    
    /**
     * 发送基础实体类日志
     */
    private void sendBaseEntity(BaseLogEntity logEntity) {
        if (logTableManager == null) {
            throw new LogSenderException(LogResultCode.DB_SENDER_CONFIG_ERROR, 
                "LogTableManager未配置，请检查配置");
        }
        
        // 确保表存在，如果不存在则创建
        String tableName = logTableManager.ensureTableExists(logEntity.getClass());
        if (tableName == null) {
            throw new LogSenderException(LogResultCode.DB_SENDER_CONFIG_ERROR, 
                "无法确定表名");
        }
        
        // 获取表信息和插入SQL
        LogTableManager.TableInfo tableInfo = logTableManager.getTableInfo(tableName, logEntity.getClass());
        
        // 动态插入数据
        int rowsAffected = insertEntityData(logEntity, tableInfo);
        
        if (rowsAffected > 0) {
            log.info("✅ 数据库日志保存成功 - 表: {}, ID: {}, 类型: {}", 
                    tableName, logEntity.getId(), logEntity.getClass().getSimpleName());
            log.debug("   自定义字段: {}", hasCustomFields(logEntity) ? "是" : "否");
        } else {
            log.warn("⚠️ 数据库日志插入未生效 - 表: {}, ID: {}", tableName, logEntity.getId());
        }
    }
    
    /**
     * 动态插入实体数据
     */
    private int insertEntityData(BaseLogEntity logEntity, LogTableManager.TableInfo tableInfo) {
        List<Object> values = new ArrayList<>();
        
        // 按照表字段顺序准备参数值
        for (String fieldName : tableInfo.getFields()) {
            Object value = getEntityFieldValue(logEntity, fieldName);
            values.add(value);
        }
        
        log.debug("插入SQL: {}", tableInfo.getInsertSql());
        log.debug("参数值: {}", values);
        
        return jdbcTemplate.update(tableInfo.getInsertSql(), values.toArray());
    }
    
    /**
     * 动态插入预设实体数据
     */
    private int insertPresetEntityData(BaseLogEntity logEntity, PresetLogTableManager.TableInfo tableInfo) {
        List<Object> values = new ArrayList<>();
        
        // 按照表字段顺序准备参数值
        for (String fieldName : tableInfo.getFields()) {
            Object value = getPresetEntityFieldValue(logEntity, fieldName);
            values.add(value);
        }
        
        log.debug("预设实体插入SQL: {}", tableInfo.getInsertSql());
        log.debug("预设实体参数值: {}", values);
        
        return jdbcTemplate.update(tableInfo.getInsertSql(), values.toArray());
    }
    
    /**
     * 获取实体字段值（支持基础字段和自定义字段）
     */
    private Object getEntityFieldValue(BaseLogEntity logEntity, String fieldName) {
        try {
            // 处理基础字段
            switch (fieldName.toLowerCase()) {
                case "id":
                    return logEntity.getId();
                case "timestamp":
                    return logEntity.getTimestamp() != null ? 
                           Timestamp.valueOf(logEntity.getTimestamp()) : null;
                case "content":
                    return logEntity.getContent();
                case "level":
                    return logEntity.getLevel() != null ? 
                           logEntity.getLevel().toString() : null;
                case "created_at":
                case "updated_at":
                    return Timestamp.valueOf(LocalDateTime.now());
            }
            
            // 尝试通过反射获取字段值
            Object fieldValue = getFieldValue(logEntity, fieldName);
            
            // 特殊类型处理
            if (fieldValue instanceof LocalDateTime) {
                return Timestamp.valueOf((LocalDateTime) fieldValue);
            } else if (fieldValue instanceof java.time.LocalDate) {
                return java.sql.Date.valueOf((java.time.LocalDate) fieldValue);
            } else if (fieldValue instanceof java.time.LocalTime) {
                return java.sql.Time.valueOf((java.time.LocalTime) fieldValue);
            } else if (fieldValue instanceof Enum) {
                return fieldValue.toString();
            } else if (fieldValue != null && !isPrimitiveOrWrapper(fieldValue.getClass())) {
                // 复杂对象转换为JSON字符串
                try {
                    return new com.fasterxml.jackson.databind.ObjectMapper()
                            .writeValueAsString(fieldValue);
                } catch (Exception e) {
                    log.debug("序列化字段 {} 失败，使用toString: {}", fieldName, e.getMessage());
                    return fieldValue.toString();
                }
            }
            
            return fieldValue;
            
        } catch (Exception e) {
            log.debug("获取字段 {} 值失败: {}", fieldName, e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取预设实体字段值
     */
    private Object getPresetEntityFieldValue(BaseLogEntity logEntity, String fieldName) {
        try {
            // 处理审计字段
            switch (fieldName.toLowerCase()) {
                case "created_at":
                case "updated_at":
                    return Timestamp.valueOf(LocalDateTime.now());
            }
            
            // 尝试通过反射获取字段值
            Object fieldValue = getFieldValue(logEntity, fieldName);
            
            // 特殊类型处理
            if (fieldValue instanceof LocalDateTime) {
                return Timestamp.valueOf((LocalDateTime) fieldValue);
            } else if (fieldValue instanceof java.time.LocalDate) {
                return java.sql.Date.valueOf((java.time.LocalDate) fieldValue);
            } else if (fieldValue instanceof java.time.LocalTime) {
                return java.sql.Time.valueOf((java.time.LocalTime) fieldValue);
            } else if (fieldValue instanceof Enum) {
                return fieldValue.toString();
            } else if (fieldValue != null && !isPrimitiveOrWrapper(fieldValue.getClass())) {
                // 复杂对象转换为JSON字符串
                try {
                    return new com.fasterxml.jackson.databind.ObjectMapper()
                            .writeValueAsString(fieldValue);
                } catch (Exception e) {
                    log.debug("序列化预设实体字段 {} 失败，使用toString: {}", fieldName, e.getMessage());
                    return fieldValue.toString();
                }
            }
            
            return fieldValue;
            
        } catch (Exception e) {
            log.debug("获取预设实体字段 {} 值失败: {}", fieldName, e.getMessage());
            return null;
        }
    }
    
    /**
     * 检查是否为基本类型或包装类型
     */
    private boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() || 
               clazz == String.class ||
               clazz == Integer.class ||
               clazz == Long.class ||
               clazz == Double.class ||
               clazz == Float.class ||
               clazz == Boolean.class ||
               clazz == BigDecimal.class ||
               Number.class.isAssignableFrom(clazz);
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
