package com.diit.common.log.sender.impl;

import com.diit.common.log.entity.BaseLogEntity;
import com.diit.common.log.properties.LogProperties;
import com.diit.common.log.sender.GenericLogSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
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
    
    @Autowired
    private LogProperties logProperties;
    
    // 基础字段的插入SQL（将在运行时动态构建）
    private String baseInsertSql;
    
    /**
     * 获取配置的表名
     */
    private String getTableName() {
        return logProperties.getDatabase().getTableName();
    }
    
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
                // 确保表存在
                ensureTableExists();
                
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
                log.info("   ID: {}, Content: {}, Level: {}", 
                        logEntity.getId(), logEntity.getContent(), logEntity.getLevel());
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
        // 确保表包含所有必要的字段
        ensureTableHasAllFields(logEntity);
        
        // 动态构建插入SQL
        String insertSql = buildDynamicInsertSql(logEntity);
        
        // 动态构建参数
        Object[] params = buildDynamicInsertParams(logEntity);
        
        return jdbcTemplate.update(insertSql, params);
    }
    
    /**
     * 确保表包含实体类的所有字段
     */
    private void ensureTableHasAllFields(BaseLogEntity logEntity) {
        java.lang.reflect.Field[] fields = logEntity.getClass().getDeclaredFields();
        
        for (java.lang.reflect.Field field : fields) {
            if (!isBaseLogEntityField(field.getName())) {
                String columnName = convertFieldNameToColumnName(field.getName());
                addColumnIfNotExists(columnName, field.getType());
            }
        }
    }
    
    /**
     * 如果列不存在则添加列
     */
    private void addColumnIfNotExists(String columnName, Class<?> fieldType) {
        try {
            String tableName = getTableName();
            
            // 检查列是否存在
            String checkColumnSql = """
                SELECT COUNT(*) FROM information_schema.columns 
                WHERE table_name = ? AND column_name = ?
                """;
            
            Integer count = jdbcTemplate.queryForObject(checkColumnSql, Integer.class, tableName, columnName);
            
            if (count == null || count == 0) {
                // 列不存在，添加列
                String columnType = getColumnType(fieldType);
                String addColumnSql = String.format("ALTER TABLE %s ADD COLUMN %s %s", tableName, columnName, columnType);
                jdbcTemplate.execute(addColumnSql);
                log.info("为表 {} 添加新列: {} ({})", tableName, columnName, columnType);
            }
        } catch (Exception e) {
            log.error("添加列 {} 失败: {}", columnName, e.getMessage());
            throw new RuntimeException("Failed to add column " + columnName, e);
        }
    }
    
    
    /**
     * 动态构建插入SQL
     */
    private String buildDynamicInsertSql(BaseLogEntity logEntity) {
        String tableName = getTableName();
        StringBuilder sql = new StringBuilder(String.format("INSERT INTO %s (id, timestamp, content, level, entity_type", tableName));
        StringBuilder values = new StringBuilder(" VALUES (?, ?, ?, ?, ?");
        
        // 添加自定义字段列
        java.lang.reflect.Field[] fields = logEntity.getClass().getDeclaredFields();
        for (java.lang.reflect.Field field : fields) {
            if (!isBaseLogEntityField(field.getName())) {
                String columnName = convertFieldNameToColumnName(field.getName());
                sql.append(", ").append(columnName);
                values.append(", ?");
            }
        }
        
        sql.append(")").append(values).append(")");
        return sql.toString();
    }
    
    /**
     * 动态构建插入参数
     */
    private Object[] buildDynamicInsertParams(BaseLogEntity logEntity) {
        java.util.List<Object> params = new java.util.ArrayList<>();
        
        // 基础字段参数
        params.add(logEntity.getId());
        params.add(logEntity.getTimestamp() != null ? 
            Timestamp.valueOf(logEntity.getTimestamp()) : null);
        params.add(logEntity.getContent());
        params.add(logEntity.getLevel() != null ? logEntity.getLevel().name() : null);
        params.add(logEntity.getClass().getSimpleName());
        
        // 自定义字段参数
        java.lang.reflect.Field[] fields = logEntity.getClass().getDeclaredFields();
        for (java.lang.reflect.Field field : fields) {
            if (!isBaseLogEntityField(field.getName())) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(logEntity);
                    params.add(value);
                } catch (IllegalAccessException e) {
                    log.warn("无法访问字段 {}: {}", field.getName(), e.getMessage());
                    params.add(null);
                }
            }
        }
        
        return params.toArray();
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
    
    /**
     * 安全地获取字段值，如果方法不存在则返回null
     */
    private Object getFieldSafely(BaseLogEntity entity, String methodName) {
        try {
            Class<?> entityClass = entity.getClass();
            Method method = entityClass.getMethod(methodName);
            return method.invoke(entity);
        } catch (Exception e) {
            // 方法不存在或调用失败，返回null
            log.debug("无法获取字段 {}: {}", methodName, e.getMessage());
            return null;
        }
    }
    
    /**
     * 确保表存在，根据配置决定是否自动创建
     */
    private void ensureTableExists() {
        try {
            String tableName = getTableName();
            
            // 检查表是否存在
            String checkTableSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?";
            Integer count = jdbcTemplate.queryForObject(checkTableSql, Integer.class, tableName);
            
            if (count == null || count == 0) {
                if (logProperties.getDatabase().isAutoCreateTable()) {
                    log.info("📋 自动创建表: {}...", tableName);
                    createTable();
                    log.info("✅ 表 {} 创建成功", tableName);
                } else {
                    log.error("❌ 表 {} 不存在，且未启用自动建表功能", tableName);
                    throw new RuntimeException("Table '" + tableName + "' does not exist and autoCreateTable is disabled");
                }
            } else {
                log.debug("✅ 表 {} 已存在", tableName);
            }
        } catch (Exception e) {
            log.error("检查/创建表失败", e);
            throw new RuntimeException("Failed to ensure table exists", e);
        }
    }
    
    /**
     * 创建表
     * 根据BaseLogEntity的字段结构设计表，并动态添加自定义字段列
     */
    private void createTable() {
        String tableName = getTableName();
        
        // 基础字段的建表SQL
        StringBuilder createTableSql = new StringBuilder(String.format("""
            CREATE TABLE %s (
                id VARCHAR(50) PRIMARY KEY,
                timestamp TIMESTAMP,
                content TEXT,
                level VARCHAR(20),
                entity_type VARCHAR(100)
            """, tableName));
        
        // 动态添加自定义字段列
        addCustomFieldsToTable(createTableSql);
        
        createTableSql.append(")");
        
        jdbcTemplate.execute(createTableSql.toString());
    }
    
    /**
     * 为表添加自定义字段列
     * 只基于实际使用的实体类来创建字段
     */
    private void addCustomFieldsToTable(StringBuilder createTableSql) {
        // 这里不预定义字段，而是在实际插入时动态处理
        // 如果表已存在，会在插入时检查字段是否存在，不存在则动态添加
        log.debug("表创建完成，自定义字段将在首次插入时动态添加");
    }
    
    
    /**
     * 检查是否为BaseLogEntity的基础字段
     */
    private boolean isBaseLogEntityField(String fieldName) {
        return "id".equals(fieldName) || 
               "timestamp".equals(fieldName) || 
               "content".equals(fieldName) || 
               "level".equals(fieldName);
    }
    
    /**
     * 将字段名转换为数据库列名
     */
    private String convertFieldNameToColumnName(String fieldName) {
        // 驼峰命名转下划线命名
        return fieldName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
    
    /**
     * 根据Java类型获取PostgreSQL数据库列类型
     */
    private String getColumnType(Class<?> javaType) {
        if (javaType == String.class) {
            return "VARCHAR(500)";
        } else if (javaType == Integer.class || javaType == int.class) {
            return "INTEGER";
        } else if (javaType == Long.class || javaType == long.class) {
            return "BIGINT";
        } else if (javaType == Boolean.class || javaType == boolean.class) {
            return "BOOLEAN";
        } else if (javaType == java.time.LocalDateTime.class) {
            return "TIMESTAMP";
        } else if (javaType == java.time.LocalDate.class) {
            return "DATE";
        } else if (javaType == java.time.LocalTime.class) {
            return "TIME";
        } else if (javaType == Double.class || javaType == double.class) {
            return "DOUBLE PRECISION";
        } else if (javaType == Float.class || javaType == float.class) {
            return "REAL";
        } else if (javaType == java.math.BigDecimal.class) {
            return "NUMERIC(19,2)";
        } else if (javaType == java.util.Date.class) {
            return "TIMESTAMP";
        } else if (javaType == java.sql.Timestamp.class) {
            return "TIMESTAMP";
        } else if (javaType == java.sql.Date.class) {
            return "DATE";
        } else if (javaType == java.sql.Time.class) {
            return "TIME";
        } else if (javaType == byte[].class) {
            return "BYTEA";
        } else if (javaType.isEnum()) {
            return "VARCHAR(100)";
        } else {
            // 其他类型使用TEXT存储
            return "TEXT";
        }
    }
}
