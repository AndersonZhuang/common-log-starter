# 自定义日志测试指南

本指南将详细说明如何测试自定义日志功能，包括`@GenericLog`注解的各种使用场景。

## 🎯 自定义日志功能概览

### 核心特性
- ✅ **自定义实体类**：支持继承`BaseLogEntity`的自定义日志实体
- ✅ **SpEL表达式**：支持动态获取方法参数和返回值
- ✅ **多种发送器**：支持指定不同的日志发送器（database、kafka、elasticsearch、http）
- ✅ **异步/同步发送**：支持异步和同步日志发送
- ✅ **优先级控制**：支持日志优先级设置
- ✅ **异常处理**：支持异常日志记录

### 已实现的自定义实体
- `BusinessLogEntity`：业务日志实体，包含业务特定字段

## 🚀 快速开始测试

### 1. 启动应用
```bash
cd example

# 启动Kafka配置（推荐用于测试自定义日志）
mvn spring-boot:run -Dspring-boot.run.profiles=kafka

# 或者启动其他配置
mvn spring-boot:run -Dspring-boot.run.profiles=database
mvn spring-boot:run -Dspring-boot.run.profiles=elasticsearch
mvn spring-boot:run -Dspring-boot.run.profiles=http
```

### 2. 访问测试接口
应用启动后，可以通过以下地址访问测试接口：
- **基础URL**: `http://localhost:8080/api/generic`
- **API文档**: `http://localhost:8080/api/users` (Knife4j文档)

## 📋 详细测试用例

### 测试1: 基础通用日志
**接口**: `GET /api/generic/basic`
```bash
curl -X GET http://localhost:8080/api/generic/basic
```

**预期日志**:
```
[main] INFO  c.d.c.log.aspect.GenericLogAspect - 🚀 通用日志记录开始: 基础通用日志测试
[main] INFO  c.d.c.log.aspect.GenericLogAspect - ✅ 通用日志记录完成: 基础通用日志测试
```

### 测试2: SpEL表达式测试
**接口**: `POST /api/generic/spel`
```bash
curl -X POST "http://localhost:8080/api/generic/spel?username=testuser&action=登录"
```

**预期日志**:
```
[main] INFO  c.d.c.log.aspect.GenericLogAspect - 🚀 通用日志记录开始: 用户操作：testuser 执行了 登录
```

### 测试3: 自定义实体类测试
**接口**: `POST /api/generic/custom-entity`
```bash
curl -X POST "http://localhost:8080/api/generic/custom-entity?businessType=订单处理&department=技术部&project=电商系统"
```

**预期日志**:
```
[main] INFO  c.d.c.log.aspect.GenericLogAspect - 🚀 通用日志记录开始: 业务操作：订单处理
[main] INFO  c.d.c.log.sender.impl.UnifiedKafkaSender - 🚀 发送日志到Kafka:
   Topic: log_businesslogentity
   Key: testuser
   Category: generic
   实体类型: BusinessLogEntity
   自定义字段: 是
```

### 测试4: 指定Kafka发送器
**接口**: `POST /api/generic/kafka-sender`
```bash
curl -X POST "http://localhost:8080/api/generic/kafka-sender?message=Kafka测试消息"
```

**预期日志**:
```
[main] INFO  c.d.c.log.aspect.GenericLogAspect - 🚀 通用日志记录开始: Kafka专用日志：Kafka测试消息
[main] INFO  c.d.c.log.sender.impl.UnifiedKafkaSender - ✅ Kafka日志发送成功
```

### 测试5: 指定数据库发送器
**接口**: `POST /api/generic/database-sender`
```bash
curl -X POST "http://localhost:8080/api/generic/database-sender?message=数据库测试消息"
```

**预期日志**:
```
[main] INFO  c.d.c.log.aspect.GenericLogAspect - 🚀 通用日志记录开始: 数据库专用日志：数据库测试消息
[main] INFO  c.d.c.log.sender.impl.UnifiedDatabaseSender - ✅ 数据库日志保存成功
```

### 测试6: 指定Elasticsearch发送器
**接口**: `POST /api/generic/elasticsearch-sender`
```bash
curl -X POST "http://localhost:8080/api/generic/elasticsearch-sender?message=ES测试消息"
```

**预期日志**:
```
[main] INFO  c.d.c.log.aspect.GenericLogAspect - 🚀 通用日志记录开始: Elasticsearch专用日志：ES测试消息
[main] INFO  c.d.c.log.sender.impl.UnifiedElasticsearchSender - ✅ Elasticsearch日志发送成功
```

### 测试7: 异步发送测试
**接口**: `POST /api/generic/async`
```bash
curl -X POST "http://localhost:8080/api/generic/async?message=异步测试消息"
```

**预期日志**:
```
[main] INFO  c.d.c.log.aspect.GenericLogAspect - 🚀 通用日志记录开始: 异步日志测试：异步测试消息
[main] INFO  c.d.c.log.aspect.GenericLogAspect - ✅ 通用日志记录完成: 异步日志测试：异步测试消息
```

### 测试8: 同步发送测试
**接口**: `POST /api/generic/sync`
```bash
curl -X POST "http://localhost:8080/api/generic/sync?message=同步测试消息"
```

**预期日志**:
```
[main] INFO  c.d.c.log.aspect.GenericLogAspect - 🚀 通用日志记录开始: 同步日志测试：同步测试消息
[main] INFO  c.d.c.log.aspect.GenericLogAspect - ✅ 通用日志记录完成: 同步日志测试：同步测试消息
```

### 测试9: 高优先级测试
**接口**: `POST /api/generic/high-priority`
```bash
curl -X POST "http://localhost:8080/api/generic/high-priority?message=高优先级消息"
```

**预期日志**:
```
[main] INFO  c.d.c.log.aspect.GenericLogAspect - 🚀 通用日志记录开始: 高优先级日志：高优先级消息
[main] INFO  c.d.c.log.aspect.GenericLogAspect - 📊 日志优先级: 1 (高优先级)
```

### 测试10: 低优先级测试
**接口**: `POST /api/generic/low-priority`
```bash
curl -X POST "http://localhost:8080/api/generic/low-priority?message=低优先级消息"
```

**预期日志**:
```
[main] INFO  c.d.c.log.aspect.GenericLogAspect - 🚀 通用日志记录开始: 低优先级日志：低优先级消息
[main] INFO  c.d.c.log.aspect.GenericLogAspect - 📊 日志优先级: 9 (低优先级)
```

### 测试11: 异常处理测试
**接口**: `POST /api/generic/exception`
```bash
# 正常情况
curl -X POST "http://localhost:8080/api/generic/exception?message=正常消息"

# 异常情况
curl -X POST "http://localhost:8080/api/generic/exception?message=error"
```

**预期日志（异常情况）**:
```
[main] ERROR c.d.c.log.aspect.GenericLogAspect - ❌ 通用日志记录异常: 异常测试：error
[main] INFO  c.d.c.log.aspect.GenericLogAspect - 📝 异常信息: java.lang.RuntimeException: 这是一个测试异常，用于验证异常日志记录功能
```

### 测试12: 复杂SpEL表达式测试
**接口**: `POST /api/generic/complex-spel`
```bash
curl -X POST http://localhost:8080/api/generic/complex-spel \
  -H "Content-Type: application/json" \
  -d '{
    "user": {
      "username": "张三",
      "department": "技术部"
    },
    "action": "数据导出",
    "result": "成功"
  }'
```

**预期日志**:
```
[main] INFO  c.d.c.log.aspect.GenericLogAspect - 🚀 通用日志记录开始: 复杂操作：用户 张三 在 技术部 部门执行了 数据导出 操作，结果：成功
```

### 测试13: 批量操作测试
**接口**: `POST /api/generic/batch`
```bash
curl -X POST "http://localhost:8080/api/generic/batch?count=100"
```

**预期日志**:
```
[main] INFO  c.d.c.log.aspect.GenericLogAspect - 🚀 通用日志记录开始: 批量操作：处理 100 条记录
[main] INFO  c.d.c.log.aspect.GenericLogAspect - 📝 方法参数: [100]
[main] INFO  c.d.c.log.aspect.GenericLogAspect - 📝 返回值: {count=100, processed=100, message=批量操作完成, timestamp=...}
```

### 测试14: 性能测试
**接口**: `POST /api/generic/performance`
```bash
curl -X POST http://localhost:8080/api/generic/performance
```

**预期日志**:
```
[main] INFO  c.d.c.log.aspect.GenericLogAspect - 🚀 通用日志记录开始: 性能测试：处理时间 523ms
[main] INFO  c.d.c.log.aspect.GenericLogAspect - ⏱️ 方法执行时间: 523ms
```

### 测试15: 综合测试
**接口**: `POST /api/generic/comprehensive`
```bash
curl -X POST "http://localhost:8080/api/generic/comprehensive?testType=集成测试&description=全面功能验证&businessType=系统测试"
```

**预期日志**:
```
[main] INFO  c.d.c.log.aspect.GenericLogAspect - 🚀 通用日志记录开始: 综合测试：集成测试 - 全面功能验证
[main] INFO  c.d.c.log.aspect.GenericLogAspect - 📝 方法参数: [集成测试, 全面功能验证, 系统测试]
[main] INFO  c.d.c.log.aspect.GenericLogAspect - 📝 返回值: {testType=集成测试, description=全面功能验证, businessType=系统测试, message=综合测试完成, timestamp=...}
[main] INFO  c.d.c.log.aspect.GenericLogAspect - 📊 日志优先级: 5 (中等优先级)
[main] INFO  c.d.c.log.aspect.GenericLogAspect - 🔄 异步发送: true
```

## 🔍 日志查看和分析

### 1. 应用启动日志
启动应用时，查看以下关键日志：
```
[main] INFO  c.d.c.log.service.LogSenderService - 注册日志发送器: kafka -> UnifiedKafkaSender
[main] INFO  c.d.c.log.service.LogSenderService - 日志发送器初始化完成，共注册1个发送器
```

### 2. 自定义日志记录日志
每次调用测试接口时，查看以下日志模式：
```
[main] INFO  c.d.c.log.aspect.GenericLogAspect - 🚀 通用日志记录开始: {描述}
[main] INFO  c.d.c.log.aspect.GenericLogAspect - ✅ 通用日志记录完成: {描述}
```

### 3. 发送器日志
根据配置的发送器，查看相应的发送日志：
- **Kafka**: `UnifiedKafkaSender` 相关日志
- **Database**: `UnifiedDatabaseSender` 相关日志
- **Elasticsearch**: `UnifiedElasticsearchSender` 相关日志
- **HTTP**: `UnifiedHttpSender` 相关日志

## 🛠️ 自定义实体类开发

### 1. 创建自定义实体类
```java
@Data
@EqualsAndHashCode(callSuper = true)
public class MyCustomLogEntity extends BaseLogEntity {
    
    /** 自定义字段1 */
    private String customField1;
    
    /** 自定义字段2 */
    private String customField2;
    
    /** 业务数据 */
    private String businessData;
}
```

### 2. 使用自定义实体类
```java
@GenericLog(
    value = "自定义操作：#{#action}",
    entityClass = MyCustomLogEntity.class,
    module = "自定义模块",
    target = "自定义对象"
)
public void customMethod(String action) {
    // 方法实现
}
```

## 📊 测试结果验证

### 1. Kafka验证
```bash
# 查看Kafka消息
kafka-console-consumer --bootstrap-server localhost:9092 --topic log_businesslogentity --from-beginning
```

### 2. 数据库验证
```sql
-- 查看日志表
SELECT * FROM common_logs ORDER BY create_time DESC LIMIT 10;
```

### 3. Elasticsearch验证
```bash
# 查看索引
curl -X GET "localhost:9200/_cat/indices?v" | grep log

# 查看数据
curl -X GET "localhost:9200/logs-businesslogentity-2024-01/_search?pretty"
```

## 🚨 常见问题排查

### 1. 自定义实体类未生效
**问题**: 日志中显示的是默认实体类而不是自定义实体类
**排查**:
- 检查`entityClass`参数是否正确设置
- 确认自定义实体类继承自`BaseLogEntity`
- 检查自定义实体类是否有正确的注解

### 2. SpEL表达式解析失败
**问题**: SpEL表达式没有正确解析
**排查**:
- 检查表达式语法是否正确
- 确认参数名称是否匹配
- 查看是否有SpEL解析异常日志

### 3. 发送器未找到
**问题**: 指定的发送器类型未找到
**排查**:
- 检查`senderType`参数是否正确
- 确认对应的sender是否已注册
- 查看发送器注册日志

## 🎯 测试建议

1. **按顺序测试**: 建议按照测试用例的顺序进行测试
2. **观察日志**: 每次测试后观察应用日志，确认功能正常
3. **验证数据**: 根据配置的发送器，验证数据是否正确存储
4. **异常测试**: 特别测试异常情况，确保异常日志记录正常
5. **性能测试**: 进行多次性能测试，观察日志记录对性能的影响

## 📝 总结

自定义日志功能提供了强大的灵活性，支持：
- ✅ 自定义实体类和字段
- ✅ 动态SpEL表达式
- ✅ 多种发送器选择
- ✅ 异步/同步发送
- ✅ 优先级控制
- ✅ 异常处理

通过以上测试用例，您可以全面验证自定义日志功能的各个方面。
