# Common Log Starter

一个功能强大的Spring Boot日志记录Starter，支持多种存储方式和动态字段映射。

## 功能特性

### 🚀 核心功能
- **多种存储方式**：支持Kafka、Elasticsearch、Database、HTTP四种存储方式
- **动态字段映射**：根据实体类字段自动创建数据库列，支持PostgreSQL
- **AOP切面记录**：基于注解的日志记录，支持操作日志、用户访问日志、通用日志
- **异步处理**：支持异步日志发送，提高性能
- **批量处理**：支持批量日志发送
- **可配置表名**：数据库存储支持自定义表名

### 📝 日志类型
- **操作日志**：`@OperationLog` - 记录用户操作行为
- **用户访问日志**：`@UserAccessLog` - 记录用户访问行为  
- **通用日志**：`@GenericLog` - 支持自定义实体类

### 🗄️ 存储方式
- **Kafka**：分布式消息队列存储
- **Elasticsearch**：全文搜索引擎存储
- **Database**：关系型数据库存储（PostgreSQL）
- **HTTP**：HTTP接口存储

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.diit</groupId>
    <artifactId>common-log-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 配置存储方式

#### Kafka存储
```yaml
diit:
  log:
    storage:
      type: kafka
    kafka:
      enabled: true
      bootstrapServers: localhost:9092
```

#### Elasticsearch存储
```yaml
diit:
  log:
    storage:
      type: elasticsearch
    elasticsearch:
      enabled: true
      hosts: localhost:9200
      indexPrefix: logs
```

#### Database存储
```yaml
diit:
  log:
    storage:
      type: database
    database:
      enabled: true
      tableName: common_logs  # 可自定义表名
      autoCreateTable: true   # 是否自动建表
```

#### HTTP存储
```yaml
diit:
  log:
    storage:
      type: http
    http:
      enabled: true
      genericEndpoint: http://localhost:8080/api/logs/generic
```

### 3. 使用注解

#### 操作日志
```java
@OperationLog(module = "用户管理", operation = "删除用户", description = "删除用户ID: #{#userId}")
public void deleteUser(@RequestParam Long userId) {
    // 业务逻辑
}
```

#### 用户访问日志
```java
@UserAccessLog(description = "访问用户列表页面")
public String userList() {
    return "user/list";
}
```

#### 通用日志（自定义实体）
```java
@GenericLog(description = "业务操作: #{#businessType}")
public void businessOperation(@RequestParam String businessType) {
    // 业务逻辑
}
```

## 自定义实体类

### 1. 创建实体类
```java
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessLogEntity extends BaseLogEntity {
    private String businessType;
    private String department;
    private String project;
}
```

### 2. 使用@GenericLog注解
```java
@GenericLog(description = "业务处理: #{#businessType}")
public void processBusiness(@RequestBody BusinessLogEntity businessLog) {
    // 业务逻辑
}
```

## 配置说明

### 完整配置示例
```yaml
diit:
  log:
    enabled: true
    storage:
      type: database  # kafka, elasticsearch, database, http
      async: true
      batchSize: 100
      batchInterval: 1000
    
    # 数据库配置
    database:
      enabled: true
      tableName: common_logs
      autoCreateTable: true
    
    # Kafka配置
    kafka:
      enabled: false
      bootstrapServers: localhost:9092
      accessLogTopic: access-log
      operationLogTopic: operation-log
    
    # Elasticsearch配置
    elasticsearch:
      enabled: false
      hosts: localhost:9200
      indexPrefix: logs
    
    # HTTP配置
    http:
      enabled: false
      genericEndpoint: http://localhost:8080/api/logs/generic
    
    # 记录配置
    record:
      recordParams: true
      recordResponse: false
      recordStackTrace: true
      sensitiveFields: password,token,secret
```

## 动态字段映射

当使用Database存储时，系统会根据实体类字段自动创建数据库列：

### 字段类型映射
- `String` → `VARCHAR(500)`
- `Integer/int` → `INTEGER`
- `Long/long` → `BIGINT`
- `Boolean/boolean` → `BOOLEAN`
- `LocalDateTime` → `TIMESTAMP`
- `LocalDate` → `DATE`
- `Double/double` → `DOUBLE PRECISION`
- `Float/float` → `REAL`
- `BigDecimal` → `NUMERIC(19,2)`
- `byte[]` → `BYTEA`
- `Enum` → `VARCHAR(100)`
- 其他类型 → `TEXT`

### 列名转换
- 驼峰命名自动转换为下划线命名
- 例如：`businessType` → `business_type`

## 环境要求

- Java 17+
- Spring Boot 3.2+
- Maven 3.6+

## 测试

运行示例项目：
```bash
# 启动Kafka模式
mvn spring-boot:run -Dspring-boot.run.profiles=kafka

# 启动Elasticsearch模式  
mvn spring-boot:run -Dspring-boot.run.profiles=elasticsearch

# 启动Database模式
mvn spring-boot:run -Dspring-boot.run.profiles=database

# 启动HTTP模式
mvn spring-boot:run -Dspring-boot.run.profiles=http
```

## 许可证

MIT License
