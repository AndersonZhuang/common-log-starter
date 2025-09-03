package com.diit.common.log.database;

import com.diit.common.log.entity.BaseLogEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日志表管理器
 * 负责根据自定义日志实体类动态创建数据库表
 * 
 * @author diit
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "diit.log.database", name = "enabled", havingValue = "true", matchIfMissing = false)
public class LogTableManager {
    
    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;
    
    @Autowired(required = false)
    private DataSource dataSource;
    
    // 缓存已经检查过的表，避免重复检查
    private final Set<String> checkedTables = ConcurrentHashMap.newKeySet();
    
    // 基础字段映射（来自BaseLogEntity）
    private static final Map<String, String> BASE_FIELD_MAPPING = Map.of(
        "id", "VARCHAR(64) PRIMARY KEY",
        "timestamp", "TIMESTAMP",
        "content", "TEXT",
        "level", "VARCHAR(20)"
    );
    
    /**
     * 确保日志表存在，如果不存在则创建
     * 
     * @param entityClass 日志实体类
     * @return 表名
     */
    public String ensureTableExists(Class<? extends BaseLogEntity> entityClass) {
        if (jdbcTemplate == null || dataSource == null) {
            log.warn("数据库未配置，跳过表检查");
            return null;
        }
        
        String tableName = generateTableName(entityClass);
        
        // 如果已经检查过这个表，直接返回
        if (checkedTables.contains(tableName)) {
            return tableName;
        }
        
        try {
            if (!tableExists(tableName)) {
                createLogTable(tableName, entityClass);
                log.info("✅ 为实体类 {} 创建了数据库表: {}", entityClass.getSimpleName(), tableName);
            } else {
                log.debug("表 {} 已存在，跳过创建", tableName);
            }
            
            // 检查字段是否需要更新
            updateTableStructure(tableName, entityClass);
            
            // 标记为已检查
            checkedTables.add(tableName);
            
            return tableName;
            
        } catch (Exception e) {
            log.error("检查或创建表 {} 失败", tableName, e);
            throw new RuntimeException("数据库表操作失败", e);
        }
    }
    
    /**
     * 生成表名
     * 规则: log_ + 类名转下划线小写
     */
    private String generateTableName(Class<? extends BaseLogEntity> entityClass) {
        String className = entityClass.getSimpleName();
        // 将驼峰命名转换为下划线命名
        String underscoreName = className.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
        return "log_" + underscoreName;
    }
    
    /**
     * 检查表是否存在
     */
    private boolean tableExists(String tableName) throws Exception {
        try (var connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet tables = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
                boolean exists = tables.next();
                log.debug("检查表 {} 是否存在: {}", tableName, exists);
                return exists;
            }
        }
    }
    
    /**
     * 创建日志表
     */
    private void createLogTable(String tableName, Class<? extends BaseLogEntity> entityClass) {
        StringBuilder createSql = new StringBuilder();
        createSql.append("CREATE TABLE ").append(tableName).append(" (\n");
        
        List<String> columns = new ArrayList<>();
        
        // 添加基础字段
        BASE_FIELD_MAPPING.forEach((field, type) -> {
            columns.add("    " + field + " " + type);
        });
        
        // 添加自定义字段（包括继承的字段）
        Map<String, String> customFields = extractAllFields(entityClass);
        customFields.forEach((field, type) -> {
            columns.add("    " + field + " " + type);
        });
        
        // 添加审计字段
        columns.add("    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
        columns.add("    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
        
        createSql.append(String.join(",\n", columns));
        createSql.append("\n)");
        
        log.info("执行建表SQL: {}", createSql.toString());
        jdbcTemplate.execute(createSql.toString());
        
        // 创建索引
        createIndexes(tableName);
    }
    
    /**
     * 提取所有字段（包括继承的字段，但排除基础字段）
     */
    private Map<String, String> extractAllFields(Class<? extends BaseLogEntity> entityClass) {
        Map<String, String> allFields = new LinkedHashMap<>();
        
        // 递归获取所有字段，包括父类字段
        Class<?> currentClass = entityClass;
        while (currentClass != null && currentClass != Object.class) {
            Field[] fields = currentClass.getDeclaredFields();
            for (Field field : fields) {
                String fieldName = field.getName();
                
                // 跳过基础字段（BaseLogEntity中的字段）
                if (BASE_FIELD_MAPPING.containsKey(fieldName)) {
                    continue;
                }
                
                // 跳过静态字段和合成字段
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) || 
                    field.isSynthetic()) {
                    continue;
                }
                
                // 如果字段已经存在，跳过（子类字段优先）
                if (allFields.containsKey(fieldName)) {
                    continue;
                }
                
                String sqlType = mapJavaTypeToSqlType(field.getType());
                allFields.put(fieldName, sqlType);
            }
            currentClass = currentClass.getSuperclass();
        }
        
        log.debug("为实体类 {} 提取到所有字段: {}", entityClass.getSimpleName(), allFields);
        return allFields;
    }
    
    /**
     * 将Java类型映射为SQL类型
     */
    private String mapJavaTypeToSqlType(Class<?> javaType) {
        if (javaType == String.class) {
            return "VARCHAR(255)";
        } else if (javaType == Integer.class || javaType == int.class) {
            return "INTEGER";
        } else if (javaType == Long.class || javaType == long.class) {
            return "BIGINT";
        } else if (javaType == Double.class || javaType == double.class) {
            return "DOUBLE PRECISION";
        } else if (javaType == Float.class || javaType == float.class) {
            return "REAL";
        } else if (javaType == Boolean.class || javaType == boolean.class) {
            return "BOOLEAN";
        } else if (javaType == BigDecimal.class) {
            return "DECIMAL(19,2)";
        } else if (javaType == LocalDateTime.class) {
            return "TIMESTAMP";
        } else if (javaType == java.time.LocalDate.class) {
            return "DATE";
        } else if (javaType == java.time.LocalTime.class) {
            return "TIME";
        } else if (javaType.isEnum()) {
            return "VARCHAR(50)";
        } else {
            // 默认使用TEXT存储复杂对象的JSON表示
            return "TEXT";
        }
    }
    
    /**
     * 更新表结构（添加缺失的字段）
     */
    private void updateTableStructure(String tableName, Class<? extends BaseLogEntity> entityClass) {
        try {
            Set<String> existingColumns = getExistingColumns(tableName);
            Map<String, String> allFields = extractAllFields(entityClass);
            
            for (Map.Entry<String, String> entry : allFields.entrySet()) {
                String fieldName = entry.getKey();
                String sqlType = entry.getValue();
                
                if (!existingColumns.contains(fieldName.toLowerCase())) {
                    String alterSql = String.format("ALTER TABLE %s ADD COLUMN %s %s", 
                                                  tableName, fieldName, sqlType);
                    log.info("添加缺失字段: {}", alterSql);
                    jdbcTemplate.execute(alterSql);
                }
            }
        } catch (Exception e) {
            log.warn("更新表结构失败: {}", e.getMessage());
        }
    }
    
    /**
     * 获取表的现有字段
     */
    private Set<String> getExistingColumns(String tableName) throws Exception {
        Set<String> columns = new HashSet<>();
        
        try (var connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet rs = metaData.getColumns(null, null, tableName, null)) {
                while (rs.next()) {
                    columns.add(rs.getString("COLUMN_NAME").toLowerCase());
                }
            }
        }
        
        return columns;
    }
    
    /**
     * 创建常用索引
     */
    private void createIndexes(String tableName) {
        try {
            // 为常用查询字段创建索引
            String[] indexFields = {"timestamp", "username", "status", "create_time"};
            
            for (String field : indexFields) {
                try {
                    String indexSql = String.format("CREATE INDEX IF NOT EXISTS idx_%s_%s ON %s(%s)", 
                                                  tableName, field, tableName, field);
                    jdbcTemplate.execute(indexSql);
                } catch (Exception e) {
                    log.debug("创建索引失败（可能字段不存在）: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("创建索引时出现问题: {}", e.getMessage());
        }
    }
    
    /**
     * 获取插入SQL和字段映射
     */
    public TableInfo getTableInfo(String tableName, Class<? extends BaseLogEntity> entityClass) {
        try {
            Set<String> existingColumns = getExistingColumns(tableName);
            List<String> fields = new ArrayList<>();
            List<String> placeholders = new ArrayList<>();
            
            // 按顺序添加字段
            for (String field : BASE_FIELD_MAPPING.keySet()) {
                if (existingColumns.contains(field.toLowerCase())) {
                    fields.add(field);
                    placeholders.add("?");
                }
            }
            
            // 添加自定义字段
            Map<String, String> allFields = extractAllFields(entityClass);
            for (String field : allFields.keySet()) {
                if (existingColumns.contains(field.toLowerCase())) {
                    fields.add(field);
                    placeholders.add("?");
                }
            }
            
            String insertSql = String.format("INSERT INTO %s (%s) VALUES (%s)",
                                           tableName,
                                           String.join(", ", fields),
                                           String.join(", ", placeholders));
            
            return new TableInfo(insertSql, fields);
            
        } catch (Exception e) {
            log.error("获取表信息失败", e);
            throw new RuntimeException("获取表信息失败", e);
        }
    }
    
    /**
     * 表信息类
     */
    public static class TableInfo {
        private final String insertSql;
        private final List<String> fields;
        
        public TableInfo(String insertSql, List<String> fields) {
            this.insertSql = insertSql;
            this.fields = fields;
        }
        
        public String getInsertSql() {
            return insertSql;
        }
        
        public List<String> getFields() {
            return fields;
        }
    }
}