# Common Log Starter

通用日志记录Starter，支持访问日志和操作日志的自动记录。

## 功能特性

- 🚀 **即插即用**：引入依赖即可使用，无需额外配置
- 🔧 **配置灵活**：支持多种存储方式和配置选项
- 📊 **多种存储**：支持Kafka、Elasticsearch、数据库、HTTP等多种存储方式
- 🎯 **AOP切面**：基于Spring AOP，非侵入式日志记录
- 🔒 **安全过滤**：自动过滤敏感字段，保护用户隐私
- 📱 **信息丰富**：记录IP、地理位置、浏览器、操作系统、设备类型等详细信息
- ⚡ **异步处理**：异步发送日志，不影响接口响应速度
- 🎨 **扩展性强**：支持自定义字段、过滤器等扩展功能

## 快速开始

### 1. 引入依赖

```xml
<dependency>
    <groupId>com.diit</groupId>
    <artifactId>common-log-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 启用日志功能

```yaml
diit:
  log:
    enabled: true
    storage:
      type: kafka  # 选择存储方式
```

### 3. 使用注解

#### 访问日志

```java
@UserAccessLog(type = "登录", description = "用户登录系统")
public APIResponse<LoginResponse> login(@RequestBody LoginRequest request) {
    // 业务逻辑
}
```

#### 操作日志

```java
@OperationLog(type = "新增", description = "创建用户", module = "用户管理")
public APIResponse<User> createUser(@RequestBody CreateUserRequest request) {
    // 业务逻辑
}
```

## 配置说明

### 基础配置

```yaml
diit:
  log:
    enabled: true                    # 是否启用日志功能
    storage:
      type: kafka                    # 存储类型：kafka, elasticsearch, database, http
      async: true                    # 是否异步发送
      batch-size: 100               # 批量发送大小
      batch-interval: 1000          # 批量发送间隔（毫秒）
```

### Kafka配置

```yaml
diit:
  log:
    kafka:
      enabled: true                  # 是否启用Kafka
      access-log-topic: access-log   # 访问日志Topic
      operation-log-topic: operation-log  # 操作日志Topic
      bootstrap-servers: localhost:9092   # 服务器地址
      producer:
        retries: 3                   # 重试次数
        batch-size: 16384            # 批量大小
        linger-ms: 1                 # 延迟时间
        buffer-memory: 33554432      # 缓冲区大小
```

### Elasticsearch配置

```yaml
diit:
  log:
    elasticsearch:
      enabled: true                  # 是否启用Elasticsearch
      hosts: localhost:9200          # 服务器地址
      index-prefix: log              # 索引前缀
      username: elastic              # 用户名
      password:                      # 密码
      connect-timeout: 5000          # 连接超时时间
      read-timeout: 30000            # 读取超时时间
```

### HTTP配置

```yaml
diit:
  log:
    http:
      enabled: true                  # 是否启用HTTP发送
      access-log-endpoint: http://localhost:8080/api/logs/access
      operation-log-endpoint: http://localhost:8080/api/logs/operation
      connect-timeout: 5000          # 连接超时时间
      read-timeout: 30000            # 读取超时时间
```

### 日志记录配置

```yaml
diit:
  log:
    record:
      record-params: true            # 是否记录请求参数
      record-response: false         # 是否记录响应结果
      record-stack-trace: true       # 是否记录异常堆栈
      record-ip-location: true       # 是否记录IP地理位置
      record-user-agent: true        # 是否记录用户代理信息
      sensitive-fields: password,token,secret  # 敏感字段（不记录）
```

## 注解说明

### @UserAccessLog

用于标记需要记录访问日志的方法。

```java
@UserAccessLog(
    type = "登录",                    // 访问类型
    description = "用户登录系统",      // 访问描述
    recordParams = false,             // 是否记录请求参数
    recordResponse = false,           // 是否记录响应结果
    recordStackTrace = true,          // 是否记录异常堆栈
    module = "认证模块",              // 操作模块
    target = "用户登录"               // 操作对象
)
```

### @OperationLog

用于标记需要记录操作日志的方法。

```java
@OperationLog(
    type = "新增",                    // 操作类型
    description = "创建用户",          // 操作描述
    recordParams = true,              // 是否记录方法参数
    recordResponse = false,           // 是否记录响应结果
    recordStackTrace = true,          // 是否记录异常堆栈
    module = "用户管理",              // 操作模块
    target = "用户信息",              // 操作对象
    recordDataChange = true           // 是否记录数据变更（前后对比）
)
```

## 日志字段说明

### 访问日志字段

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | String | 日志ID |
| username | String | 用户名 |
| realName | String | 真实姓名 |
| email | String | 邮箱 |
| accessType | String | 访问类型 |
| description | String | 访问描述 |
| accessTime | String | 访问时间 |
| accessTimestamp | LocalDateTime | 访问时间戳 |
| clientIp | String | 客户端IP |
| ipLocation | String | IP地理位置 |
| browser | String | 浏览器信息 |
| operatingSystem | String | 操作系统 |
| deviceType | String | 设备类型 |
| status | String | 访问状态 |
| responseTime | Long | 响应时间 |
| requestUri | String | 请求URI |
| requestMethod | String | 请求方法 |
| userAgent | String | 用户代理 |
| sessionId | String | 会话ID |
| createTime | LocalDateTime | 创建时间 |

### 操作日志字段

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | String | 日志ID |
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
| responseTime | Long | 响应时间 |
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

## 扩展功能

### 自定义日志字段

```java
@UserAccessLog(type = "登录", description = "用户登录系统")
@CustomLogField(key = "loginType", value = "password")
public APIResponse<LoginResponse> login() {
    // 业务逻辑
}
```

### 日志过滤器

```java
@Component
public class CustomLogFilter implements LogFilter {
    @Override
    public boolean shouldLog(LogContext context) {
        // 自定义过滤逻辑
        return true;
    }
}
```

### 日志转换器

```java
@Component
public class CustomLogConverter implements LogConverter<CustomLog> {
    @Override
    public CustomLog convert(Object source) {
        // 自定义转换逻辑
        return new CustomLog();
    }
}
```

## 监控和管理

### 健康检查

Starter自动注册健康检查端点：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,logs
  endpoint:
    health:
      show-details: always
```

### 管理端点

- `/actuator/health` - 健康检查
- `/actuator/logs` - 日志管理

## 环境适配

### 开发环境

```yaml
spring:
  profiles:
    active: dev

diit:
  log:
    storage:
      type: kafka
    kafka:
      bootstrap-servers: localhost:9092
```

### 生产环境

```yaml
spring:
  profiles:
    active: prod

diit:
  log:
    storage:
      type: elasticsearch
    elasticsearch:
      hosts: elasticsearch:9200
      username: elastic
      password: ${ES_PASSWORD}
```

## 常见问题

### Q: 如何禁用日志功能？

A: 在配置文件中设置 `diit.log.enabled=false`

### Q: 如何切换存储方式？

A: 修改 `diit.log.storage.type` 配置项

### Q: 如何自定义日志字段？

A: 使用 `@CustomLogField` 注解或继承相关类

### Q: 日志发送失败怎么办？

A: 检查网络连接和配置信息，日志会自动重试

## 版本兼容性

- Spring Boot: 2.7.x, 3.x
- Java: 8, 11, 17
- Spring Framework: 5.3.x, 6.x

## 贡献指南

欢迎提交Issue和Pull Request！

## 许可证

MIT License

## 联系方式

- 作者: diit
- 邮箱: support@diit.com
- 项目地址: https://github.com/diit/common-log-starter
