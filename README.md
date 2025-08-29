# Common Log Starter

一个功能强大的Spring Boot日志记录Starter，支持访问日志和操作日志的自动记录，并提供多种日志存储方式。

## ✨ 功能特性

- 🔍 **自动日志记录**：通过注解自动记录用户访问日志和操作日志
- 🚀 **零配置启动**：默认配置即可使用，无需复杂配置
- 🔧 **灵活配置**：支持多种日志存储方式（Kafka、Elasticsearch、数据库、HTTP）
- 📊 **异步处理**：支持异步日志记录，不影响业务性能
- 🎯 **条件化加载**：智能检测项目环境，按需加载组件
- 🛡️ **安全友好**：支持JWT token解析，自动提取用户信息

## 🚀 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.diit</groupId>
    <artifactId>common-log-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 基础配置（可选）

```yaml
diit:
  log:
    enabled: true  # 启用日志功能（默认true）
    storage:
      type: kafka  # 存储类型：kafka, elasticsearch, database, http
      async: true  # 异步发送（默认true）
      batchSize: 100  # 批量发送大小
      batchInterval: 1000  # 批量发送间隔（毫秒）
```

### 3. 使用注解

#### 访问日志
```java
@RestController
public class UserController {
    
    @UserAccessLog
    @GetMapping("/users")
    public List<User> getUsers() {
        return userService.findAll();
    }
}
```

#### 操作日志
```java
@RestController
public class UserController {
    
    @OperationLog(operation = "创建用户", description = "创建新用户账户")
    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        return userService.save(user);
    }
}
```

## ⚙️ 详细配置

### Kafka配置
```yaml
diit:
  log:
    kafka:
      enabled: true
      bootstrapServers: localhost:9092
      accessLogTopic: access-log
      operationLogTopic: operation-log
      producer:
        acks: all
        retries: 3
```

### Elasticsearch配置
```yaml
diit:
  log:
    elasticsearch:
      enabled: true
      hosts: localhost:9200
      indexPrefix: log-
      username: elastic
      password: password
```

### 数据库配置
```yaml
diit:
  log:
    database:
      enabled: true
      url: jdbc:mysql://localhost:3306/logs
      username: root
      password: password
      tablePrefix: log_
```

### HTTP配置
```yaml
diit:
  log:
    http:
      enabled: true
      url: http://localhost:8080/api/logs
      method: POST
      headers:
        Authorization: Bearer ${token}
```

## 🏗️ 架构设计

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   注解层        │    │   切面层        │    │   发送器层      │
│  @UserAccessLog │───▶│  UserAccessLog  │───▶│  LogSender      │
│  @OperationLog  │    │  Aspect         │    │  Factory        │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │                       │
                                ▼                       ▼
                       ┌─────────────────┐    ┌─────────────────┐
                       │   配置层        │    │   存储层        │
                       │  LogProperties  │    │  Kafka/ES/DB    │
                       │  AutoConfig     │    │  HTTP           │
                       └─────────────────┘    └─────────────────┘
```

## 🔧 自定义扩展

### 自定义日志发送器
```java
@Component
public class CustomLogSender implements LogSender {
    
    @Override
    public void send(OperationLogEntity log) {
        // 自定义发送逻辑
    }
    
    @Override
    public void send(UserAccessLogEntity log) {
        // 自定义发送逻辑
    }
}
```

### 自定义日志实体
```java
@Data
public class CustomLogEntity extends OperationLogEntity {
    private String customField;
    // 其他自定义字段
}
```

## 📋 日志字段说明

### 访问日志字段
- `requestId`: 请求唯一标识
- `userId`: 用户ID
- `username`: 用户名
- `ip`: 客户端IP
- `userAgent`: 用户代理
- `requestUrl`: 请求URL
- `requestMethod`: 请求方法
- `requestParams`: 请求参数
- `responseStatus`: 响应状态
- `responseTime`: 响应时间
- `timestamp`: 时间戳

### 操作日志字段
- `operation`: 操作名称
- `description`: 操作描述
- `userId`: 操作用户ID
- `username`: 操作用户名
- `ip`: 操作IP
- `requestParams`: 请求参数
- `result`: 操作结果
- `timestamp`: 操作时间

## 🚨 注意事项

1. **性能考虑**：日志记录是异步的，但大量日志可能影响性能
2. **存储配置**：确保配置的存储服务（Kafka、ES等）可用
3. **敏感信息**：注意日志中不要记录敏感信息（密码、token等）
4. **版本兼容**：支持Spring Boot 3.x版本

## 🤝 贡献

欢迎提交Issue和Pull Request！

## 📄 许可证

MIT License

## 📞 联系方式

如有问题，请通过以下方式联系：
- 提交GitHub Issue
- 发送邮件至：[your-email@example.com]

---

**Made with ❤️ by [Your Name]**
