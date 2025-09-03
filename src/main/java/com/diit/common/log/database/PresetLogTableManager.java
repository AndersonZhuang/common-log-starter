package com.diit.common.log.database;

import com.diit.common.log.entity.OperationLogEntity;
import com.diit.common.log.entity.UserAccessLogEntity;
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
 * 预设日志实体表管理器
 * 专门负责OperationLogEntity和UserAccessLogEntity的自动建表
 * 
 * @author diit
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "diit.log.database", name = "enabled", havingValue = "true", matchIfMissing = false)
public class PresetLogTableManager {
    
    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;
    
    @Autowired(required = false)
    private DataSource dataSource;
    
    // 缓存已经检查过的表，避免重复检查
    private final Set<String> checkedTables = ConcurrentHashMap.newKeySet();
    
    // 预设实体类映射
    private static final Map<Class<?>, String> PRESET_ENTITY_TABLES = Map.of(
        OperationLogEntity.class, "log_operation",
        UserAccessLogEntity.class, "log_user_access"
    );
    
    /**
     * 确保预设日志表存在，如果不存在则创建
     * 
     * @param entityClass 预设日志实体类
     * @return 表名
     */
    public String ensureTableExists(Class<?> entityClass) {
        if (jdbcTemplate == null || dataSource == null) {
            log.warn("数据库未配置，跳过预设表检查");
            return null;
        }
        
        if (!PRESET_ENTITY_TABLES.containsKey(entityClass)) {
            log.warn("不支持的预设实体类: {}", entityClass.getSimpleName());
            return null;
        }
        
        String tableName = PRESET_ENTITY_TABLES.get(entityClass);
        
        // 如果已经检查过这个表，直接返回
        if (checkedTables.contains(tableName)) {
            return tableName;
        }
        
        try {
            if (!tableExists(tableName)) {
                createPresetLogTable(tableName, entityClass);
                log.info("✅ 为预设实体类 {} 创建了数据库表: {}", entityClass.getSimpleName(), tableName);
            } else {
                log.debug("预设表 {} 已存在，跳过创建", tableName);
            }
            
            // 检查字段是否需要更新
            updateTableStructure(tableName, entityClass);
            
            // 标记为已检查
            checkedTables.add(tableName);
            
            return tableName;
            
        } catch (Exception e) {
            log.error("检查或创建预设表 {} 失败", tableName, e);
            throw new RuntimeException("预设数据库表操作失败", e);
        }
    }
    
    /**
     * 检查表是否存在
     */
    private boolean tableExists(String tableName) throws Exception {
        try (var connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet tables = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
                boolean exists = tables.next();
                log.debug("检查预设表 {} 是否存在: {}", tableName, exists);
                return exists;
            }
        }
    }
    
    /**
     * 创建预设日志表
     */
    private void createPresetLogTable(String tableName, Class<?> entityClass) {
        StringBuilder createSql = new StringBuilder();
        createSql.append("CREATE TABLE ").append(tableName).append(" (\n");
        
        List<String> columns = new ArrayList<>();
        
        // 添加所有字段
        Map<String, String> allFields = extractAllFields(entityClass);
        allFields.forEach((field, type) -> {
            // 为id字段添加主键约束
            if ("id".equals(field)) {
                columns.add("    " + field + " " + type + " PRIMARY KEY");
            } else {
                columns.add("    " + field + " " + type);
            }
        });
        
        // 添加审计字段
        columns.add("    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
        columns.add("    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
        
        createSql.append(String.join(",\n", columns));
        createSql.append("\n)");
        
        log.info("执行预设表建表SQL: {}", createSql.toString());
        jdbcTemplate.execute(createSql.toString());
        
        // 创建索引
        createIndexes(tableName, entityClass);
    }
    
    /**
     * 提取所有字段
     */
    private Map<String, String> extractAllFields(Class<?> entityClass) {
        Map<String, String> allFields = new LinkedHashMap<>();
        
        // 递归获取所有字段，包括父类字段
        Class<?> currentClass = entityClass;
        while (currentClass != null && currentClass != Object.class) {
            Field[] fields = currentClass.getDeclaredFields();
            for (Field field : fields) {
                String fieldName = field.getName();
                
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
        
        log.debug("为预设实体类 {} 提取到所有字段: {}", entityClass.getSimpleName(), allFields);
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
    private void updateTableStructure(String tableName, Class<?> entityClass) {
        try {
            Set<String> existingColumns = getExistingColumns(tableName);
            Map<String, String> allFields = extractAllFields(entityClass);
            
            for (Map.Entry<String, String> entry : allFields.entrySet()) {
                String fieldName = entry.getKey();
                String sqlType = entry.getValue();
                
                if (!existingColumns.contains(fieldName.toLowerCase())) {
                    String alterSql = String.format("ALTER TABLE %s ADD COLUMN %s %s", 
                                                  tableName, fieldName, sqlType);
                    log.info("为预设表添加缺失字段: {}", alterSql);
                    jdbcTemplate.execute(alterSql);
                }
            }
        } catch (Exception e) {
            log.warn("更新预设表结构失败: {}", e.getMessage());
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
    private void createIndexes(String tableName, Class<?> entityClass) {
        try {
            // 为常用查询字段创建索引
            String[] commonIndexFields = {"username", "status", "create_time", "operation_time", "access_time"};
            
            for (String field : commonIndexFields) {
                try {
                    String indexSql = String.format("CREATE INDEX IF NOT EXISTS idx_%s_%s ON %s(%s)", 
                                                  tableName, field, tableName, field);
                    jdbcTemplate.execute(indexSql);
                } catch (Exception e) {
                    log.debug("创建预设表索引失败（可能字段不存在）: {}", e.getMessage());
                }
            }
            
            // 根据实体类类型创建特定索引
            if (entityClass == OperationLogEntity.class) {
                createOperationLogIndexes(tableName);
            } else if (entityClass == UserAccessLogEntity.class) {
                createUserAccessLogIndexes(tableName);
            }
            
        } catch (Exception e) {
            log.warn("创建预设表索引时出现问题: {}", e.getMessage());
        }
    }
    
    /**
     * 为操作日志创建特定索引
     */
    private void createOperationLogIndexes(String tableName) {
        String[] operationIndexFields = {"operation_type", "module", "target", "client_ip"};
        
        for (String field : operationIndexFields) {
            try {
                String indexSql = String.format("CREATE INDEX IF NOT EXISTS idx_%s_%s ON %s(%s)", 
                                              tableName, field, tableName, field);
                jdbcTemplate.execute(indexSql);
            } catch (Exception e) {
                log.debug("创建操作日志索引失败: {}", e.getMessage());
            }
        }
    }
    
    /**
     * 为用户访问日志创建特定索引
     */
    private void createUserAccessLogIndexes(String tableName) {
        String[] accessIndexFields = {"access_type", "client_ip", "session_id"};
        
        for (String field : accessIndexFields) {
            try {
                String indexSql = String.format("CREATE INDEX IF NOT EXISTS idx_%s_%s ON %s(%s)", 
                                              tableName, field, tableName, field);
                jdbcTemplate.execute(indexSql);
            } catch (Exception e) {
                log.debug("创建用户访问日志索引失败: {}", e.getMessage());
            }
        }
    }
    
    /**
     * 获取插入SQL和字段映射
     */
    public TableInfo getTableInfo(String tableName, Class<?> entityClass) {
        try {
            Set<String> existingColumns = getExistingColumns(tableName);
            List<String> fields = new ArrayList<>();
            List<String> placeholders = new ArrayList<>();
            
            // 添加所有字段
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
            log.error("获取预设表信息失败", e);
            throw new RuntimeException("获取预设表信息失败", e);
        }
    }
    
    /**
     * 检查是否为预设实体类
     */
    public boolean isPresetEntity(Class<?> entityClass) {
        return PRESET_ENTITY_TABLES.containsKey(entityClass);
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
