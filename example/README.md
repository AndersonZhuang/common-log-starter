# Common Log Starter Example

这是 `common-log-starter` 的使用示例项目，展示了如何使用日志注解和配置。

## 🚀 快速开始

### 1. 运行项目

```bash
# 进入example目录
cd example

# 编译项目
mvn clean compile

# 运行项目
mvn spring-boot:run
```

### 2. 访问接口文档

项目启动后，访问以下地址查看API文档：

- **Knife4j接口文档**: http://localhost:8080/doc.html
- **Swagger JSON**: http://localhost:8080/v3/api-docs

## 📋 接口说明

### 用户管理接口

- `POST /api/users/login` - 用户登录（访问日志）
- `POST /api/users/logout` - 用户登出（访问日志）
- `POST /api/users` - 创建用户（操作日志）
- `PUT /api/users/{id}` - 更新用户（操作日志）
- `DELETE /api/users/{id}` - 删除用户（操作日志）
- `GET /api/users/{id}` - 查询用户（操作日志）

### 测试接口

- `GET /api/test/normal` - 正常访问测试
- `POST /api/test/with-params` - 带参数访问测试
- `POST /api/test/operation` - 操作日志测试
- `GET /api/test/exception` - 异常测试
- `POST /api/test/batch` - 批量操作测试
- `POST /api/test/sensitive` - 敏感信息测试
- `GET /api/test/performance` - 性能测试

### 日志管理接口

- `POST /api/logs/access` - 接收访问日志
- `POST /api/logs/operation` - 接收操作日志
- `GET /api/logs/access` - 查看访问日志
- `GET /api/logs/operation` - 查看操作日志
- `GET /api/logs/stats` - 获取日志统计
- `DELETE /api/logs` - 清空所有日志

## 🔧 配置说明

### 开发环境配置 (application-dev.yml)

- 使用HTTP方式发送日志（便于测试）
- 同步发送日志（便于调试）
- 详细日志输出

### 生产环境配置 (application-prod.yml)

- 使用Kafka发送日志
- 异步发送日志
- 精简日志输出

### 日志配置

```yaml
diit:
  log:
    enabled: true
    storage:
      type: http  # 开发环境
      async: false  # 开发环境同步
    record:
      recordParams: true
      recordResponse: true
      sensitiveFields: password,token,secret
```

## 🧪 测试步骤

### 1. 启动项目

```bash
mvn spring-boot:run
```

### 2. 查看接口文档

访问 http://localhost:8080/doc.html

### 3. 测试日志记录

#### 测试访问日志
```bash
# 正常访问
curl -X GET "http://localhost:8080/api/test/normal"

# 带参数访问
curl -X POST "http://localhost:8080/api/test/with-params" \
  -H "Content-Type: application/json" \
  -d '{"name":"test","value":"123"}'
```

#### 测试操作日志
```bash
# 创建用户
curl -X POST "http://localhost:8080/api/users" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com"}'

# 查询用户
curl -X GET "http://localhost:8080/api/users/12345"
```

#### 测试异常日志
```bash
# 异常测试（可能抛出异常）
curl -X GET "http://localhost:8080/api/test/exception"
```

### 4. 查看记录的日志

```bash
# 查看访问日志
curl -X GET "http://localhost:8080/api/logs/access"

# 查看操作日志
curl -X GET "http://localhost:8080/api/logs/operation"

# 查看日志统计
curl -X GET "http://localhost:8080/api/logs/stats"
```

## 📊 日志字段说明

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

## 🔍 调试技巧

### 1. 查看控制台日志

项目启动时会显示详细的日志配置信息，包括：
- 日志Starter初始化状态
- 切面组件加载状态
- 发送器配置信息

### 2. 查看HTTP日志

开发环境使用HTTP方式发送日志，可以在控制台看到：
- 日志发送请求
- 日志接收响应
- 错误信息（如果有）

### 3. 切换存储方式

修改 `application.yml` 中的配置：

```yaml
diit:
  log:
    storage:
      type: kafka  # 或 elasticsearch, database, http
```

## 🚨 注意事项

1. **开发环境**：使用HTTP方式便于测试，但生产环境建议使用Kafka或Elasticsearch
2. **敏感信息**：密码、token等敏感字段会自动过滤，不会记录到日志中
3. **性能影响**：开发环境同步发送可能影响性能，生产环境建议异步发送
4. **存储清理**：测试完成后可以调用清空接口清理日志数据

## 📚 更多信息

- [Common Log Starter 主项目](../README.md)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Knife4j 官方文档](https://doc.xiaominfo.com/)
