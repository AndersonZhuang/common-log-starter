# 测试指南

本文档详细说明如何测试 `common-log-starter` 的各种功能。

## 🚀 快速测试

### 1. 启动项目

```bash
cd example
mvn spring-boot:run
```

### 2. 访问接口文档

- **Knife4j文档**: http://localhost:8080/doc.html
- **Swagger JSON**: http://localhost:8080/v3/api-docs

## 📋 接口测试方法

### 基础测试接口

#### 1. 正常访问测试
```bash
curl -X GET "http://localhost:8080/api/test/normal"
```

#### 2. GET方式测试（推荐新手使用）
```bash
# 带查询参数
curl -X GET "http://localhost:8080/api/test/get-test?name=test&value=123"

# 多个参数
curl -X GET "http://localhost:8080/api/test/get-test?username=admin&email=admin@example.com&role=admin"
```

### 高级测试接口

#### 3. 通用测试接口（支持多种Content-Type）
```bash
# 方式1: 只使用查询参数（推荐）
curl -X POST "http://localhost:8080/api/test/universal?name=test&value=123"

# 方式2: 使用JSON请求体
curl -X POST "http://localhost:8080/api/test/universal" \
  -H "Content-Type: application/json" \
  -d '{"name":"test","value":"123"}'

# 方式3: 混合使用（查询参数+请求体）
curl -X POST "http://localhost:8080/api/test/universal?type=user&id=123" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com"}'
```

#### 4. 带参数访问测试
```bash
# 只使用查询参数
curl -X POST "http://localhost:8080/api/test/with-params?name=test&value=123"

# 使用JSON请求体
curl -X POST "http://localhost:8080/api/test/with-params" \
  -H "Content-Type: application/json" \
  -d '{"name":"test","value":"123"}'
```

#### 5. 操作日志测试
```bash
# 只使用查询参数
curl -X POST "http://localhost:8080/api/test/operation?action=create&target=user"

# 使用JSON请求体
curl -X POST "http://localhost:8080/api/test/operation" \
  -H "Content-Type: application/json" \
  -d '{"action":"create","target":"user","data":{"name":"test"}}'
```

### 特殊测试接口

#### 6. 异常测试
```bash
curl -X GET "http://localhost:8080/api/test/exception"
```
注意：这个接口会随机抛出异常，用于测试异常日志记录。

#### 7. 性能测试
```bash
curl -X GET "http://localhost:8080/api/test/performance"
```
这个接口会模拟处理时间，用于测试性能监控。

#### 8. 敏感信息测试
```bash
# 测试敏感信息过滤
curl -X POST "http://localhost:8080/api/test/sensitive?username=admin&password=secret123&token=abc123"
```
注意：密码和token等敏感信息会被自动过滤，不会记录到日志中。

### 用户管理接口测试

#### 9. 用户登录（访问日志）
```bash
curl -X POST "http://localhost:8080/api/users/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

#### 10. 创建用户（操作日志）
```bash
curl -X POST "http://localhost:8080/api/users" \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","email":"new@example.com","nickname":"新用户"}'
```

#### 11. 查询用户（操作日志）
```bash
curl -X GET "http://localhost:8080/api/users/12345"
```

## 🔍 查看测试结果

### 1. 查看记录的日志
```bash
# 查看访问日志
curl -X GET "http://localhost:8080/api/logs/access"

# 查看操作日志
curl -X GET "http://localhost:8080/api/logs/operation"

# 查看日志统计
curl -X GET "http://localhost:8080/api/logs/stats"
```

### 2. 查看控制台输出
项目启动后，控制台会显示：
- 日志Starter初始化信息
- 切面组件加载状态
- 日志发送和接收信息

## 🚨 常见问题解决

### 问题1: Content-Type不支持
**错误信息**: `Content-Type 'application/x-www-form-urlencoded;charset=UTF-8' is not supported`

**解决方案**:
1. 使用查询参数方式（推荐）：
   ```bash
   curl -X POST "http://localhost:8080/api/test/universal?name=test&value=123"
   ```

2. 或者指定正确的Content-Type：
   ```bash
   curl -X POST "http://localhost:8080/api/test/universal" \
     -H "Content-Type: application/json" \
     -d '{"name":"test","value":"123"}'
   ```

### 问题2: 参数接收失败
**解决方案**:
- 使用 `@RequestParam` 接收查询参数
- 使用 `@RequestBody` 接收请求体参数
- 我们的接口已经支持两种方式

### 问题3: 日志没有记录
**检查步骤**:
1. 确认 `diit.log.enabled=true`
2. 检查控制台是否有错误信息
3. 查看日志接收接口是否正常工作

## 🧪 测试建议

### 新手测试顺序
1. 先测试 `GET` 接口（如 `/api/test/get-test`）
2. 再测试带查询参数的 `POST` 接口
3. 最后测试需要JSON请求体的接口

### 进阶测试
1. 测试异常情况
2. 测试性能监控
3. 测试敏感信息过滤
4. 测试批量操作

### 生产环境测试
1. 切换到生产环境配置
2. 测试Kafka日志发送
3. 测试异步日志处理
4. 测试性能影响

## 📊 测试结果验证

### 成功标志
- 接口返回 `"success": true`
- 控制台显示日志记录信息
- 日志接收接口能查看到记录

### 失败标志
- 接口返回错误信息
- 控制台显示异常堆栈
- 日志接收接口无数据

## 🔧 自定义测试

您可以根据需要修改测试接口，添加更多测试场景：

1. 修改 `TestController.java`
2. 添加新的测试方法
3. 使用相应的日志注解
4. 重新编译和测试

## 📞 获取帮助

如果遇到问题：
1. 查看控制台错误信息
2. 检查配置文件
3. 查看日志接收接口
4. 提交GitHub Issue
