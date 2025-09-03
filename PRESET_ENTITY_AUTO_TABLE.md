# 预设实体类自动建表功能

## 概述

为了支持 `OperationLogEntity` 和 `UserAccessLogEntity` 这两个预设日志实体类的自动建表功能，我们创建了专门的 `PresetLogTableManager` 来管理这些实体类的数据库表。

## 功能特性

### 1. 自动建表
- **OperationLogEntity** → 自动创建表 `log_operation`
- **UserAccessLogEntity** → 自动创建表 `log_user_access`

### 2. 表结构完全匹配
- 表结构完全基于实体类的字段定义
- 不添加任何预定义的额外字段
- 确保表结构与实体类结构完全一致

### 3. 智能索引创建
- 为常用查询字段自动创建索引
- 根据实体类类型创建特定索引
- 操作日志：`operation_type`, `module`, `target`, `client_ip`
- 用户访问日志：`access_type`, `client_ip`, `session_id`

### 4. 审计字段
- 自动添加 `created_at` 和 `updated_at` 审计字段
- 支持字段结构动态更新

## 使用方法

### 1. 配置启用
```yaml
diit:
  log:
    database:
      enabled: true  # 启用数据库功能
```

### 2. 直接使用预设实体类
```java
// 创建操作日志
OperationLogEntity operationLog = OperationLogEntity.builder()
    .id(UUID.randomUUID().toString())
    .username("admin")
    .operationType("新增")
    .description("创建用户")
    .operationTimestamp(LocalDateTime.now())
    .status("成功")
    .createTime(LocalDateTime.now())
    .build();

// 发送到数据库（会自动建表）
databaseSender.send(operationLog);
```

```java
// 创建用户访问日志
UserAccessLogEntity accessLog = UserAccessLogEntity.builder()
    .id(UUID.randomUUID().toString())
    .username("user123")
    .accessType("登录")
    .description("用户登录")
    .accessTimestamp(LocalDateTime.now())
    .status("成功")
    .createTime(LocalDateTime.now())
    .build();

// 发送到数据库（会自动建表）
databaseSender.send(accessLog);
```

### 3. 测试接口
我们提供了测试控制器来验证功能：

- `GET /api/preset-test/operation-log` - 测试操作日志自动建表
- `GET /api/preset-test/user-access-log` - 测试用户访问日志自动建表
- `GET /api/preset-test/batch-test` - 测试批量发送预设实体

## 技术实现

### 1. PresetLogTableManager
- 专门管理预设实体类的表创建
- 支持 `OperationLogEntity` 和 `UserAccessLogEntity`
- 提供表信息查询和字段映射

### 2. UnifiedDatabaseSender 增强
- 自动识别预设实体类
- 根据实体类型选择对应的表管理器
- 支持预设实体类的数据插入

### 3. 表名映射
```java
private static final Map<Class<?>, String> PRESET_ENTITY_TABLES = Map.of(
    OperationLogEntity.class, "log_operation",
    UserAccessLogEntity.class, "log_user_access"
);
```

## 与原有功能的区别

| 功能 | 原有LogTableManager | 新增PresetLogTableManager |
|------|-------------------|-------------------------|
| 支持实体类 | 继承BaseLogEntity的类 | OperationLogEntity, UserAccessLogEntity |
| 表名规则 | log_ + 类名下划线 | 预定义表名 |
| 字段处理 | 排除基础字段 | 包含所有字段 |
| 索引策略 | 通用索引 | 针对性的特定索引 |

## 注意事项

1. **配置要求**：需要启用 `diit.log.database.enabled=true`
2. **数据源配置**：需要正确配置数据库连接
3. **权限要求**：应用需要有创建表和索引的权限
4. **表名冲突**：确保预定义的表名不与现有表冲突

## 示例表结构

### log_operation 表结构
```sql
CREATE TABLE log_operation (
    id VARCHAR(64) PRIMARY KEY,
    username VARCHAR(255),
    real_name VARCHAR(255),
    email VARCHAR(255),
    role_name VARCHAR(255),
    operation_type VARCHAR(255),
    description TEXT,
    operation_time VARCHAR(255),
    operation_timestamp TIMESTAMP,
    client_ip VARCHAR(255),
    ip_location VARCHAR(255),
    browser VARCHAR(255),
    operating_system VARCHAR(255),
    device_type VARCHAR(255),
    status VARCHAR(255),
    response_time BIGINT,
    request_uri VARCHAR(255),
    request_method VARCHAR(255),
    user_agent VARCHAR(255),
    session_id VARCHAR(255),
    module VARCHAR(255),
    target VARCHAR(255),
    before_data TEXT,
    after_data TEXT,
    exception_message TEXT,
    create_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### log_user_access 表结构
```sql
CREATE TABLE log_user_access (
    id VARCHAR(64) PRIMARY KEY,
    username VARCHAR(255),
    real_name VARCHAR(255),
    email VARCHAR(255),
    access_type VARCHAR(255),
    description TEXT,
    module VARCHAR(255),
    target VARCHAR(255),
    access_time VARCHAR(255),
    access_timestamp TIMESTAMP,
    client_ip VARCHAR(255),
    ip_location VARCHAR(255),
    browser VARCHAR(255),
    operating_system VARCHAR(255),
    device_type VARCHAR(255),
    status VARCHAR(255),
    response_time BIGINT,
    request_uri VARCHAR(255),
    request_method VARCHAR(255),
    user_agent VARCHAR(255),
    session_id VARCHAR(255),
    create_time TIMESTAMP,
    exception_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

这样，预设实体类现在也具备了完整的自动建表功能，与自定义实体类保持一致的用户体验。
