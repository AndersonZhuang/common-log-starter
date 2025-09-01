# Common Log Starter

Spring Boot 通用日志记录 Starter，支持访问日志和操作日志的自动记录，提供多种存储方式。

## 功能特性

- 🚀 **自动日志记录**: 基于 AOP 自动捕获操作日志和访问日志
- 🎯 **注解驱动**: 简单的 `@OperationLog` 和 `@UserAccessLog` 注解
- 🔄 **多存储支持**: Kafka、Elasticsearch、PostgreSQL、HTTP 四种存储方式
- ⚡ **异步处理**: 支持异步和批量发送，不影响业务性能
- 🛠️ **灵活配置**: 丰富的配置选项，满足不同场景需求

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.diit</groupId>
    <artifactId>common-log-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 基础配置

在 `application.yml` 中添加配置：

```yaml
diit:
  log:
    enabled: true
    storage:
      type: kafka  # 存储类型：kafka、database、elasticsearch、http
      async: true  # 异步发送
```

### 3. 使用注解

```java
@RestController
public class UserController {
    
    @OperationLog(type = "新增", description = "创建新用户", module = "用户管理", target = "用户信息")
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // 业务逻辑
        return ResponseEntity.ok(savedUser);
    }
    
    @UserAccessLog(type = "登录", description = "用户登录系统", module = "认证模块", target = "用户登录")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        // 登录逻辑
        return ResponseEntity.ok(response);
    }
}
```

## 存储方式配置

### Kafka 存储

```yaml
diit:
  log:
    storage:
      type: kafka
    kafka:
      enabled: true
      bootstrapServers: localhost:9092
      accessLogTopic: access-log
      operationLogTopic: operation-log
```

### Elasticsearch 存储

```yaml
diit:
  log:
    storage:
      type: elasticsearch
    elasticsearch:
      enabled: true
      hosts: localhost:9200
      indexPrefix: common-log
      username: elastic  # 可选
      password: password  # 可选
```

### PostgreSQL 数据库存储

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/your_database
    username: postgres
    password: password

diit:
  log:
    storage:
      type: database
    database:
      enabled: true
      tablePrefix: log_
      autoCreateTable: true
```

### HTTP 存储

```yaml
diit:
  log:
    storage:
      type: http
    http:
      enabled: true
      accessLogEndpoint: http://localhost:8080/api/logs/access
      operationLogEndpoint: http://localhost:8080/api/logs/operation
```

## 注解详解

### @OperationLog 操作日志注解

用于记录用户的操作行为（增删改查等）。

```java
@OperationLog(
    type = "新增",                    // 操作类型
    description = "创建新用户",         // 操作描述
    module = "用户管理",               // 操作模块
    target = "用户信息",               // 操作对象
    recordParams = true,             // 是否记录参数
    recordResponse = false,          // 是否记录响应
    recordStackTrace = true,         // 是否记录异常堆栈
    recordDataChange = false         // 是否记录数据变更
)
```

### @UserAccessLog 访问日志注解

用于记录用户的访问行为（登录、注销等）。

```java
@UserAccessLog(
    type = "登录",                    // 访问类型
    description = "用户登录系统",       // 访问描述
    module = "认证模块",               // 访问模块
    target = "用户登录",               // 访问对象
    recordParams = false,            // 是否记录参数
    recordResponse = false,          // 是否记录响应
    recordStackTrace = true          // 是否记录异常堆栈
)
```

## 高级配置

### 完整配置示例

```yaml
diit:
  log:
    enabled: true
    
    # 存储配置
    storage:
      type: kafka
      async: true
      batchSize: 100
      batchInterval: 1000
    
    # Kafka 配置
    kafka:
      enabled: true
      bootstrapServers: localhost:9092
      accessLogTopic: access-log
      operationLogTopic: operation-log
      producer:
        retries: 3
        batchSize: 16384
        lingerMs: 1
        bufferMemory: 33554432
    
    # Elasticsearch 配置
    elasticsearch:
      enabled: false
      hosts: localhost:9200
      indexPrefix: log
      connectTimeout: 5000
      readTimeout: 30000
    
    # 数据库配置
    database:
      enabled: false
      tablePrefix: log_
      autoCreateTable: true
    
    # HTTP 配置
    http:
      enabled: false
      accessLogEndpoint: http://localhost:8080/api/logs/access
      operationLogEndpoint: http://localhost:8080/api/logs/operation
      connectTimeout: 5000
      readTimeout: 30000
    
    # 记录配置
    record:
      recordParams: true
      recordResponse: false
      recordStackTrace: true
      recordIpLocation: true
      recordUserAgent: true
      sensitiveFields: password,token,secret
```

## 日志字段说明

### 操作日志字段

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 主键ID |
| username | String | 用户名 |
| realName | String | 真实姓名 |
| email | String | 邮箱 |
| roleName | String | 角色名称 |
| operationType | String | 操作类型 |
| description | String | 操作描述 |
| operationTime | String | 操作时间 |
| operationTimestamp | LocalDateTime | 操作时间戳 |
| clientIp | String | 客户端IP |
| ipLocation | String | IP地理位置 |
| browser | String | 浏览器信息 |
| operatingSystem | String | 操作系统 |
| deviceType | String | 设备类型 |
| status | String | 操作状态 |
| responseTime | Long | 响应时间（毫秒） |
| requestUri | String | 请求URI |
| requestMethod | String | 请求方法 |
| userAgent | String | 用户代理 |
| sessionId | String | 会话ID |
| module | String | 操作模块 |
| target | String | 操作对象 |
| beforeData | String | 操作前数据 |
| afterData | String | 操作后数据 |
| exceptionMessage | String | 异常信息 |
| createTime | LocalDateTime | 创建时间 |

### 访问日志字段

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 主键ID |
| username | String | 用户名 |
| realName | String | 真实姓名 |
| email | String | 邮箱 |
| accessType | String | 访问类型 |
| description | String | 访问描述 |
| module | String | 访问模块 |
| target | String | 访问对象 |
| accessTime | String | 访问时间 |
| accessTimestamp | LocalDateTime | 访问时间戳 |
| clientIp | String | 客户端IP |
| ipLocation | String | IP地理位置 |
| browser | String | 浏览器信息 |
| operatingSystem | String | 操作系统 |
| deviceType | String | 设备类型 |
| status | String | 访问状态 |
| responseTime | Long | 响应时间（毫秒） |
| requestUri | String | 请求URI |
| requestMethod | String | 请求方法 |
| userAgent | String | 用户代理 |
| sessionId | String | 会话ID |
| createTime | LocalDateTime | 创建时间 |
| exceptionMessage | String | 异常信息 |

## 数据库表结构

### 操作日志表

```sql
CREATE TABLE log_operation_log (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(100),
    real_name VARCHAR(100),
    email VARCHAR(100),
    role_name VARCHAR(100),
    operation_type VARCHAR(50),
    description VARCHAR(500),
    operation_time VARCHAR(50),
    operation_timestamp TIMESTAMP,
    client_ip VARCHAR(50),
    ip_location VARCHAR(200),
    browser VARCHAR(100),
    operating_system VARCHAR(100),
    device_type VARCHAR(50),
    status VARCHAR(20),
    response_time BIGINT,
    request_uri VARCHAR(500),
    request_method VARCHAR(10),
    user_agent VARCHAR(500),
    session_id VARCHAR(100),
    module_name VARCHAR(100),
    target VARCHAR(200),
    before_data TEXT,
    after_data TEXT,
    exception_message TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 访问日志表

```sql
CREATE TABLE log_access_log (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(100),
    real_name VARCHAR(100),
    email VARCHAR(100),
    access_type VARCHAR(50),
    description VARCHAR(500),
    access_time VARCHAR(50),
    access_timestamp TIMESTAMP,
    client_ip VARCHAR(50),
    ip_location VARCHAR(200),
    browser VARCHAR(100),
    operating_system VARCHAR(100),
    device_type VARCHAR(50),
    status VARCHAR(20),
    response_time BIGINT,
    request_uri VARCHAR(500),
    request_method VARCHAR(10),
    user_agent VARCHAR(500),
    session_id VARCHAR(100),
    module_name VARCHAR(100),
    target VARCHAR(200),
    exception_message TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 使用示例

### 用户管理模块

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @OperationLog(type = "新增", description = "创建新用户", module = "用户管理", target = "用户信息")
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User savedUser = userService.save(user);
        return ResponseEntity.ok(savedUser);
    }
    
    @OperationLog(type = "编辑", description = "更新用户信息", module = "用户管理", target = "用户信息", recordDataChange = true)
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        User updatedUser = userService.update(id, user);
        return ResponseEntity.ok(updatedUser);
    }
    
    @OperationLog(type = "删除", description = "删除用户", module = "用户管理", target = "用户信息")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok().build();
    }
}
```

### 认证模块

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @UserAccessLog(type = "登录", description = "用户登录系统", module = "认证模块", target = "用户登录")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @UserAccessLog(type = "注销", description = "用户注销系统", module = "认证模块", target = "用户注销")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok().build();
    }
}
```

## 集成步骤

### 1. 引入依赖

```xml
<dependency>
    <groupId>com.diit</groupId>
    <artifactId>common-log-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 启用AOP

在主类上添加 `@EnableAspectJAutoProxy` 注解：

```java
@SpringBootApplication
@EnableAspectJAutoProxy
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

### 3. 配置存储方式

选择一种存储方式并添加相应配置。

#### Kafka 存储依赖

```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

#### Elasticsearch 存储依赖

```xml
<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-elasticsearch</artifactId>
</dependency>
```

#### 数据库存储依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>
```

#### HTTP 存储依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

### 4. 添加注解

在需要记录日志的方法上添加相应注解：

- 操作日志：`@OperationLog`
- 访问日志：`@UserAccessLog`

## 配置参数详解

### 基础配置

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `diit.log.enabled` | boolean | true | 是否启用日志功能 |
| `diit.log.storage.type` | String | kafka | 存储类型 |
| `diit.log.storage.async` | boolean | true | 是否异步发送 |
| `diit.log.storage.batchSize` | int | 100 | 批量发送大小 |
| `diit.log.storage.batchInterval` | long | 1000 | 批量发送间隔（毫秒） |

### Kafka 配置

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `diit.log.kafka.enabled` | boolean | true | 是否启用Kafka |
| `diit.log.kafka.bootstrapServers` | String | localhost:9092 | Kafka服务器地址 |
| `diit.log.kafka.accessLogTopic` | String | access-log | 访问日志Topic |
| `diit.log.kafka.operationLogTopic` | String | operation-log | 操作日志Topic |

### Elasticsearch 配置

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `diit.log.elasticsearch.enabled` | boolean | false | 是否启用Elasticsearch |
| `diit.log.elasticsearch.hosts` | String | localhost:9200 | ES服务器地址 |
| `diit.log.elasticsearch.indexPrefix` | String | log | 索引前缀 |
| `diit.log.elasticsearch.username` | String | - | 用户名（可选） |
| `diit.log.elasticsearch.password` | String | - | 密码（可选） |

### 数据库配置

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `diit.log.database.enabled` | boolean | false | 是否启用数据库存储 |
| `diit.log.database.tablePrefix` | String | log_ | 表前缀 |
| `diit.log.database.autoCreateTable` | boolean | true | 是否自动创建表 |

### HTTP 配置

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `diit.log.http.enabled` | boolean | false | 是否启用HTTP发送 |
| `diit.log.http.accessLogEndpoint` | String | - | 访问日志端点 |
| `diit.log.http.operationLogEndpoint` | String | - | 操作日志端点 |
| `diit.log.http.connectTimeout` | int | 5000 | 连接超时时间 |
| `diit.log.http.readTimeout` | int | 30000 | 读取超时时间 |

### 记录配置

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `diit.log.record.recordParams` | boolean | true | 是否记录请求参数 |
| `diit.log.record.recordResponse` | boolean | false | 是否记录响应结果 |
| `diit.log.record.recordStackTrace` | boolean | true | 是否记录异常堆栈 |
| `diit.log.record.recordIpLocation` | boolean | true | 是否记录IP地理位置 |
| `diit.log.record.recordUserAgent` | boolean | true | 是否记录用户代理信息 |
| `diit.log.record.sensitiveFields` | String[] | password,token,secret | 敏感字段 |

## 最佳实践

### 1. 性能优化

- 使用异步发送：`storage.async=true`
- 合理设置批量大小：`storage.batchSize=100`
- 避免记录大量响应数据：`recordResponse=false`

### 2. 安全考虑

- 配置敏感字段过滤：`sensitiveFields=password,token,secret`
- 避免记录敏感参数和响应

### 3. 存储选择

- **高吞吐量**: 选择 Kafka + Logstash + Elasticsearch
- **简单部署**: 选择数据库存储
- **实时分析**: 选择 Elasticsearch 直接存储
- **轻量级**: 选择 HTTP 存储

## 故障排除

### 常见问题

1. **启动失败**
   - 检查是否添加 `@EnableAspectJAutoProxy` 注解
   - 确认相关存储服务正常运行

2. **日志未记录**
   - 检查注解是否正确添加
   - 确认配置是否正确加载
   - 查看控制台错误信息

3. **连接失败**
   - 检查存储服务连接参数
   - 确认网络连通性
   - 查看防火墙设置

### 调试模式

启用调试日志：

```yaml
logging:
  level:
    com.diit.common.log: DEBUG
```

## 版本信息

- **当前版本**: 1.0.0
- **Spring Boot**: 3.2.3+
- **Java**: 17+

## 许可证

MIT License