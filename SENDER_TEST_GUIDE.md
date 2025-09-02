# Sender 测试完整指南

本指南将详细说明如何测试四种不同的日志发送器（Database、Kafka、Elasticsearch、HTTP）的注册和功能。

## 📋 测试前准备

### 1. 环境要求
- Java 17+
- Maven 3.6+
- PostgreSQL（用于Database sender测试）
- Kafka（用于Kafka sender测试）
- Elasticsearch（用于Elasticsearch sender测试）

### 2. 启动依赖服务

#### 启动PostgreSQL
```bash
# 使用Docker启动PostgreSQL
docker run --name postgres-test -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=postgres -p 5432:5432 -d postgres:13

# 或者使用本地PostgreSQL服务
# 确保PostgreSQL运行在localhost:5432，用户名/密码为postgres/postgres
```

#### 启动Kafka
```bash
# 使用Docker Compose启动Kafka
docker-compose up -d

# 或者使用本地Kafka服务
# 确保Kafka运行在localhost:9092
```

#### 启动Elasticsearch
```bash
# 使用Docker启动Elasticsearch
docker run --name elasticsearch-test -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" -d elasticsearch:7.17.0

# 或者使用本地Elasticsearch服务
# 确保Elasticsearch运行在localhost:9200
```

## 🧪 测试流程

### 测试1: Database Sender

#### 1.1 启动应用
```bash
cd example
mvn spring-boot:run -Dspring-boot.run.profiles=database
```

#### 1.2 查看启动日志
启动后，在控制台中查找以下关键日志：

**✅ 成功标志：**
```
[main] INFO  c.d.c.log.config.LogConfiguration - 初始化JdbcTemplate
[main] INFO  c.d.c.log.service.LogSenderService - 注册日志发送器: database -> UnifiedDatabaseSender
[main] INFO  c.d.c.log.service.LogSenderService - 日志发送器初始化完成，共注册1个发送器
```

**❌ 失败标志：**
```
[main] WARN  c.d.c.log.service.LogSenderService - 未找到支持的日志发送器，类型: database
```

#### 1.3 测试日志发送
```bash
# 测试用户登录（访问日志）
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass"}'

# 测试用户创建（操作日志）
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","email":"newuser@example.com"}'
```

#### 1.4 验证数据库存储
```sql
-- 连接到PostgreSQL
psql -h localhost -U postgres -d postgres

-- 查看日志表（如果自动创建了表）
\dt log_*

-- 查看日志数据
SELECT * FROM common_logs ORDER BY create_time DESC LIMIT 10;
```

#### 1.5 停止应用
```bash
# 在启动应用的终端中按 Ctrl+C
# 或者使用pkill命令
pkill -f "spring-boot:run"
```

---

### 测试2: Kafka Sender

#### 2.1 启动应用
```bash
cd example
mvn spring-boot:run -Dspring-boot.run.profiles=kafka
```

#### 2.2 查看启动日志
启动后，在控制台中查找以下关键日志：

**✅ 成功标志：**
```
[main] INFO  c.d.c.log.config.LogConfiguration - 配置Kafka生产者工厂: {bootstrap.servers=localhost:9092, ...}
[main] INFO  c.d.c.log.config.LogConfiguration - 初始化KafkaTemplate
[main] INFO  c.d.c.log.service.LogSenderService - 注册日志发送器: kafka -> UnifiedKafkaSender
[main] INFO  c.d.c.log.service.LogSenderService - 日志发送器初始化完成，共注册1个发送器
```

**❌ 失败标志：**
```
[main] WARN  c.d.c.log.service.LogSenderService - 未找到支持的日志发送器，类型: kafka
[main] ERROR o.a.kafka.clients.NetworkClient - [Producer clientId=producer-1] Connection to node -1 could not be established
```

#### 2.3 测试日志发送
```bash
# 测试用户登录（访问日志）
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass"}'

# 测试用户创建（操作日志）
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","email":"newuser@example.com"}'
```

#### 2.4 验证Kafka消息
```bash
# 使用Kafka控制台消费者查看消息
kafka-console-consumer --bootstrap-server localhost:9092 --topic access-log --from-beginning
kafka-console-consumer --bootstrap-server localhost:9092 --topic operation-log --from-beginning

# 或者查看所有日志相关的topic
kafka-topics --bootstrap-server localhost:9092 --list | grep log
```

#### 2.5 停止应用
```bash
# 在启动应用的终端中按 Ctrl+C
# 或者使用pkill命令
pkill -f "spring-boot:run"
```

---

### 测试3: Elasticsearch Sender

#### 3.1 启动应用
```bash
cd example
mvn spring-boot:run -Dspring-boot.run.profiles=elasticsearch
```

#### 3.2 查看启动日志
启动后，在控制台中查找以下关键日志：

**✅ 成功标志：**
```
[main] INFO  c.d.c.log.config.LogConfiguration - 初始化RestTemplate
[main] INFO  c.d.c.log.service.LogSenderService - 注册日志发送器: elasticsearch -> UnifiedElasticsearchSender
[main] INFO  c.d.c.log.service.LogSenderService - 日志发送器初始化完成，共注册1个发送器
```

**❌ 失败标志：**
```
[main] WARN  c.d.c.log.service.LogSenderService - 未找到支持的日志发送器，类型: elasticsearch
[main] ERROR c.d.c.log.sender.impl.UnifiedElasticsearchSender - ❌ Elasticsearch日志发送失败
```

#### 3.3 测试日志发送
```bash
# 测试用户登录（访问日志）
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass"}'

# 测试用户创建（操作日志）
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","email":"newuser@example.com"}'
```

#### 3.4 验证Elasticsearch存储
```bash
# 检查Elasticsearch健康状态
curl -X GET "localhost:9200/_cluster/health?pretty"

# 查看所有日志相关的索引
curl -X GET "localhost:9200/_cat/indices?v" | grep log

# 查看具体索引的数据
curl -X GET "localhost:9200/logs-useraccesslog-2024-01/_search?pretty"
curl -X GET "localhost:9200/logs-operationlog-2024-01/_search?pretty"
```

#### 3.5 停止应用
```bash
# 在启动应用的终端中按 Ctrl+C
# 或者使用pkill命令
pkill -f "spring-boot:run"
```

---

### 测试4: HTTP Sender

#### 4.1 启动应用
```bash
cd example
mvn spring-boot:run -Dspring-boot.run.profiles=http
```

#### 4.2 查看启动日志
启动后，在控制台中查找以下关键日志：

**✅ 成功标志：**
```
[main] INFO  c.d.c.log.config.LogConfiguration - 初始化RestTemplate
[main] INFO  c.d.c.log.service.LogSenderService - 注册日志发送器: http -> UnifiedHttpSender
[main] INFO  c.d.c.log.service.LogSenderService - 日志发送器初始化完成，共注册1个发送器
```

**❌ 失败标志：**
```
[main] WARN  c.d.c.log.service.LogSenderService - 未找到支持的日志发送器，类型: http
[main] ERROR c.d.c.log.sender.impl.UnifiedHttpSender - ❌ HTTP请求异常
```

#### 4.3 测试日志发送
```bash
# 测试用户登录（访问日志）
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass"}'

# 测试用户创建（操作日志）
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","email":"newuser@example.com"}'
```

#### 4.4 验证HTTP发送
HTTP sender会将日志发送到配置的端点。由于配置的端点是本地地址，您需要：

1. **查看应用日志**：在控制台中查找HTTP发送的日志
2. **设置接收端点**：可以启动另一个应用来接收HTTP日志
3. **使用网络抓包工具**：如Wireshark或tcpdump来查看HTTP请求

**查看HTTP发送日志：**
```
[main] INFO  c.d.c.log.sender.impl.UnifiedHttpSender - ✅ HTTP日志发送成功 - Endpoint: http://localhost:8080/api/logs/generic, Status: 200
```

#### 4.5 停止应用
```bash
# 在启动应用的终端中按 Ctrl+C
# 或者使用pkill命令
pkill -f "spring-boot:run"
```

---

## 🔍 日志分析指南

### 关键日志模式

#### 1. Sender注册日志
```
[main] INFO  c.d.c.log.service.LogSenderService - 注册日志发送器: {type} -> {SenderClass}
[main] INFO  c.d.c.log.service.LogSenderService - 日志发送器初始化完成，共注册{N}个发送器
```

#### 2. 配置加载日志
```
[main] INFO  c.d.c.log.config.LogConfiguration - 初始化{BeanName}
[main] INFO  c.d.c.log.config.LogConfiguration - 配置{ComponentName}: {config}
```

#### 3. 日志发送日志
```
[main] INFO  c.d.c.log.sender.impl.{SenderClass} - ✅ {Type}日志发送成功
[main] ERROR c.d.c.log.sender.impl.{SenderClass} - ❌ {Type}日志发送失败
```

### 常见问题排查

#### 1. Sender未注册
**问题**：日志显示"未找到支持的日志发送器"
**排查步骤**：
1. 检查配置文件中的`enabled`设置
2. 检查`@ConditionalOnProperty`条件
3. 检查依赖服务是否启动

#### 2. 依赖服务连接失败
**问题**：数据库/Kafka/Elasticsearch连接失败
**排查步骤**：
1. 检查服务是否启动
2. 检查端口是否正确
3. 检查网络连接

#### 3. Bean注册失败
**问题**：相关Bean未注册
**排查步骤**：
1. 检查`@ConditionalOnClass`条件
2. 检查依赖是否在classpath中
3. 检查自动配置是否生效

## 📊 测试结果记录

### 测试结果表格

| Sender类型 | 配置文件 | 注册状态 | 功能测试 | 备注 |
|-----------|---------|---------|---------|------|
| Database | application-database.yml | ✅/❌ | ✅/❌ | |
| Kafka | application-kafka.yml | ✅/❌ | ✅/❌ | |
| Elasticsearch | application-elasticsearch.yml | ✅/❌ | ✅/❌ | |
| HTTP | application-http.yml | ✅/❌ | ✅/❌ | |

### 测试日志文件

建议为每次测试保存日志文件：

```bash
# 测试Database sender
mvn spring-boot:run -Dspring-boot.run.profiles=database > database_test.log 2>&1

# 测试Kafka sender  
mvn spring-boot:run -Dspring-boot.run.profiles=kafka > kafka_test.log 2>&1

# 测试Elasticsearch sender
mvn spring-boot:run -Dspring-boot.run.profiles=elasticsearch > es_test.log 2>&1

# 测试HTTP sender
mvn spring-boot:run -Dspring-boot.run.profiles=http > http_test.log 2>&1
```

## 🚀 自动化测试脚本

### 快速测试脚本
```bash
#!/bin/bash
# quick_test.sh

echo "=== 开始测试所有Sender ==="

# 测试Database
echo "🔍 测试Database Sender..."
mvn spring-boot:run -Dspring-boot.run.profiles=database > database_test.log 2>&1 &
sleep 10
grep -q "注册日志发送器.*database" database_test.log && echo "✅ Database Sender 注册成功" || echo "❌ Database Sender 注册失败"
pkill -f "spring-boot:run"
sleep 2

# 测试Kafka
echo "🔍 测试Kafka Sender..."
mvn spring-boot:run -Dspring-boot.run.profiles=kafka > kafka_test.log 2>&1 &
sleep 10
grep -q "注册日志发送器.*kafka" kafka_test.log && echo "✅ Kafka Sender 注册成功" || echo "❌ Kafka Sender 注册失败"
pkill -f "spring-boot:run"
sleep 2

# 测试Elasticsearch
echo "🔍 测试Elasticsearch Sender..."
mvn spring-boot:run -Dspring-boot.run.profiles=elasticsearch > es_test.log 2>&1 &
sleep 10
grep -q "注册日志发送器.*elasticsearch" es_test.log && echo "✅ Elasticsearch Sender 注册成功" || echo "❌ Elasticsearch Sender 注册失败"
pkill -f "spring-boot:run"
sleep 2

# 测试HTTP
echo "🔍 测试HTTP Sender..."
mvn spring-boot:run -Dspring-boot.run.profiles=http > http_test.log 2>&1 &
sleep 10
grep -q "注册日志发送器.*http" http_test.log && echo "✅ HTTP Sender 注册成功" || echo "❌ HTTP Sender 注册失败"
pkill -f "spring-boot:run"

echo "=== 测试完成 ==="
```

## 📝 总结

通过以上测试流程，您可以：

1. **验证Sender注册**：确认各个sender在相应配置下正确注册
2. **测试功能完整性**：验证日志发送功能是否正常工作
3. **排查问题**：通过日志分析快速定位问题
4. **记录测试结果**：为后续优化提供依据

记住：每次测试前确保相关依赖服务已启动，测试后及时停止应用以释放端口。
