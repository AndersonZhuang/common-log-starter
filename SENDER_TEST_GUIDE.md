# Sender测试指南

本指南介绍如何测试Common Log Starter的所有发送器（Sender）功能。

## 环境准备

### 1. 启动Docker服务
```bash
# 启动Kafka、Elasticsearch、PostgreSQL等依赖服务
docker compose up -d
```

### 2. 启动HTTP日志服务器
```bash
# 启动HTTP日志接收服务器（用于测试HTTP发送器）
python3 http-log-server.py
```

## LogTestController接口说明

示例项目提供了`LogTestController`，包含三个专门的日志测试接口：

### 接口列表

#### 1. 用户访问日志测试（预设）
- **接口**: `POST /api/log-test/user-access`
- **注解**: `@UserAccessLog`
- **功能**: 测试预设的用户访问日志记录
- **参数**: `{"username":"testuser","password":"123456"}`

#### 2. 操作日志测试（预设）
- **接口**: `POST /api/log-test/operation`
- **注解**: `@OperationLog`
- **功能**: 测试预设的操作日志记录
- **参数**: `{"username":"newuser","email":"newuser@example.com"}`

#### 3. 自定义业务日志测试
- **接口**: `POST /api/log-test/business`
- **注解**: `@GenericLog`
- **功能**: 测试自定义实体类的日志记录
- **参数**: `businessType=订单处理&description=测试订单创建&department=技术部&project=电商系统`

## 测试各种Sender

### 1. Kafka Sender测试

#### 启动应用
```bash
cd example
mvn spring-boot:run -Dspring-boot.run.profiles=kafka
```

#### 测试操作日志
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"张三","email":"zhangsan@example.com"}'
```

#### 测试用户访问日志
```bash
curl -X GET http://localhost:8080/api/users
```

#### 测试通用日志（自定义实体）
```bash
curl -X POST http://localhost:8080/api/logs/test/business \
  -H "Content-Type: application/json" \
  -d '{"businessType":"订单处理","department":"技术部","project":"电商系统"}'
```

#### 测试LogTestController的三个接口

##### 1. 用户访问日志测试（预设）
```bash
curl -X POST http://localhost:8080/api/log-test/user-access \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}'
```

##### 2. 操作日志测试（预设）
```bash
curl -X POST http://localhost:8080/api/log-test/operation \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","email":"newuser@example.com"}'
```

##### 3. 自定义业务日志测试
```bash
curl -X POST "http://localhost:8080/api/log-test/business?businessType=订单处理&description=测试订单创建&department=技术部&project=电商系统"
```

#### 查看Kafka消息
```bash
# 查看操作日志
docker exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic operation-log --from-beginning --max-messages 5

# 查看用户访问日志
docker exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic access-log --from-beginning --max-messages 5

# 查看通用日志（根据实体类名生成topic）
docker exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic log_business_log_entity --from-beginning --max-messages 5
```

### 2. Elasticsearch Sender测试

#### 启动应用
```bash
cd example
mvn spring-boot:run -Dspring-boot.run.profiles=elasticsearch
```

#### 执行测试请求
```bash
# 测试操作日志
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"李四","email":"lisi@example.com"}'

# 测试用户访问日志
curl -X GET http://localhost:8080/api/users

# 测试通用日志
curl -X POST http://localhost:8080/api/logs/test/business \
  -H "Content-Type: application/json" \
  -d '{"businessType":"用户管理","department":"运营部","project":"CRM系统"}'

# 测试LogTestController的三个接口
curl -X POST http://localhost:8080/api/log-test/user-access \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}'

curl -X POST http://localhost:8080/api/log-test/operation \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","email":"newuser@example.com"}'

curl -X POST "http://localhost:8080/api/log-test/business?businessType=订单处理&description=测试订单创建&department=技术部&project=电商系统"
```

#### 查看Elasticsearch数据
```bash
# 查看所有索引
curl -X GET "localhost:9200/_cat/indices?v"

# 查看日志数据
curl -X GET "localhost:9200/logs-*/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": {
    "match_all": {}
  },
  "size": 10
}'
```

#### 在Kibana中查看
1. 访问 http://localhost:5601
2. 创建索引模式：`.ds-logs-*`
3. 在Discover中查看日志数据

### 3. Database Sender测试

#### 启动应用
```bash
cd example
mvn spring-boot:run -Dspring-boot.run.profiles=database
```

#### 执行测试请求
```bash
# 测试操作日志
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"王五","email":"wangwu@example.com"}'

# 测试用户访问日志
curl -X GET http://localhost:8080/api/users

# 测试通用日志
curl -X POST http://localhost:8080/api/logs/test/business \
  -H "Content-Type: application/json" \
  -d '{"businessType":"数据同步","department":"数据部","project":"数据平台"}'

# 测试LogTestController的三个接口
curl -X POST http://localhost:8080/api/log-test/user-access \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}'

curl -X POST http://localhost:8080/api/log-test/operation \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","email":"newuser@example.com"}'

curl -X POST "http://localhost:8080/api/log-test/business?businessType=订单处理&description=测试订单创建&department=技术部&project=电商系统"
```

#### 查看数据库数据
```bash
# 连接PostgreSQL
docker exec -it postgres psql -U postgres -d postgres

# 查看表结构
\d common_logs

# 查看日志数据
SELECT * FROM common_logs ORDER BY timestamp DESC LIMIT 10;

# 查看自定义字段
SELECT id, timestamp, content, level, entity_type, business_type, department, project 
FROM common_logs 
WHERE entity_type = 'BusinessLogEntity' 
ORDER BY timestamp DESC;
```

### 4. HTTP Sender测试

#### 启动应用
```bash
cd example
mvn spring-boot:run -Dspring-boot.run.profiles=http
```

#### 执行测试请求
```bash
# 测试操作日志
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"赵六","email":"zhaoliu@example.com"}'

# 测试用户访问日志
curl -X GET http://localhost:8080/api/users

# 测试通用日志
curl -X POST http://localhost:8080/api/logs/test/business \
  -H "Content-Type: application/json" \
  -d '{"businessType":"系统监控","department":"运维部","project":"监控平台"}'

# 测试LogTestController的三个接口
curl -X POST http://localhost:8080/api/log-test/user-access \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}'

curl -X POST http://localhost:8080/api/log-test/operation \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","email":"newuser@example.com"}'

curl -X POST "http://localhost:8080/api/log-test/business?businessType=订单处理&description=测试订单创建&department=技术部&project=电商系统"
```

#### 查看HTTP日志服务器输出
HTTP日志服务器会显示接收到的日志数据：
```
📥 接收到HTTP日志 - 14:30:15
{
  "id": "log_20240904_143015_001",
  "timestamp": "2024-09-04T14:30:15",
  "content": "业务操作: 系统监控",
  "level": "INFO",
  "entityType": "BusinessLogEntity",
  "businessType": "系统监控",
  "department": "运维部",
  "project": "监控平台"
}
```

## 测试场景

### 1. 基础功能测试
- [x] 操作日志记录（UserController）
- [x] 用户访问日志记录（UserController）
- [x] 通用日志记录（LogTestController）
- [x] 预设日志测试（LogTestController - 用户访问日志）
- [x] 预设日志测试（LogTestController - 操作日志）
- [x] 自定义日志测试（LogTestController - 业务日志）
- [x] 异步发送
- [x] 批量发送

### 2. 存储方式测试
- [x] Kafka存储
- [x] Elasticsearch存储
- [x] Database存储
- [x] HTTP存储

### 3. 动态字段测试
- [x] 自动创建数据库表
- [x] 动态添加列
- [x] 字段类型映射
- [x] 列名转换

### 4. 配置测试
- [x] 自定义表名
- [x] 自动建表开关
- [x] 多环境配置
- [x] 敏感字段过滤

## 故障排除

### 1. Kafka连接失败
```bash
# 检查Kafka是否运行
docker ps | grep kafka

# 重启Kafka
docker compose restart kafka
```

### 2. Elasticsearch连接失败
```bash
# 检查Elasticsearch状态
curl -X GET "localhost:9200/_cluster/health?pretty"

# 重启Elasticsearch
docker compose restart elasticsearch
```

### 3. 数据库连接失败
```bash
# 检查PostgreSQL状态
docker ps | grep postgres

# 重启PostgreSQL
docker compose restart postgres
```

### 4. 端口冲突
```bash
# 查看端口占用
lsof -i :8080

# 杀死占用进程
lsof -ti :8080 | xargs kill -9
```

## 性能测试

### 1. 批量发送测试
```bash
# 发送大量请求测试批量处理
for i in {1..100}; do
  curl -X POST http://localhost:8080/api/logs/test/business \
    -H "Content-Type: application/json" \
    -d "{\"businessType\":\"测试$i\",\"department\":\"测试部\",\"project\":\"性能测试\"}" &
done
wait
```

### 2. 异步处理测试
```bash
# 测试异步处理性能
time curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"性能测试","email":"perf@example.com"}'
```

## 监控指标

### 1. 应用日志
查看应用启动日志中的发送器注册信息：
```
注册日志发送器: kafka -> UnifiedKafkaSender
注册日志发送器: elasticsearch -> UnifiedElasticsearchSender
注册日志发送器: database -> UnifiedDatabaseSender
注册日志发送器: http -> UnifiedHttpSender
```

### 2. 数据库监控
```sql
-- 查看表大小
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
FROM pg_tables 
WHERE tablename = 'common_logs';

-- 查看记录数
SELECT COUNT(*) FROM common_logs;
```

### 3. Elasticsearch监控
```bash
# 查看索引统计
curl -X GET "localhost:9200/logs-*/_stats?pretty"
```
