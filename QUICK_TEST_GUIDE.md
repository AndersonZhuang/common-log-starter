# Common Log Starter 快速测试指南

## 环境准备

1. **启动依赖服务**
```bash
docker compose up -d
```

2. **验证服务状态**
```bash
docker ps  # 确保 Kafka 和 Elasticsearch 容器正在运行
```

## 测试方式一：Kafka 发送器

1. **启动应用**
```bash
cd example
mvn spring-boot:run -Dspring-boot.run.profiles=kafka
```

2. **触发操作日志**
```bash
curl -X POST "http://localhost:8080/api/users" \
  -H "Content-Type: application/json" \
  -d '{"name":"test-user","email":"test@example.com","age":25}'
```

4. **触发访问日志**
```bash
curl -X POST "http://localhost:8080/api/simple/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

5. **验证结果**
```bash
# 查看操作日志 Topic
docker exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic operation-log \
  --from-beginning \
  --max-messages 10

# 查看访问日志 Topic  
docker exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic access-log \
  --from-beginning \
  --max-messages 10
```


**预期结果**: 
- 操作日志：完整的操作日志 JSON 数据
- 访问日志：用户访问行为记录

---

## 测试方式二：HTTP 发送器

1. **启动应用**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=http
```

2. **触发操作日志**
```bash
curl -X POST "http://localhost:8080/api/users" \
  -H "Content-Type: application/json" \
  -d '{"name":"http-test","email":"http@example.com","age":30}'
```

4. **触发访问日志**
```bash
curl -X POST "http://localhost:8080/api/simple/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"httpuser","password":"test123"}'
```

5. **验证结果**
```bash
# 查看接收到的操作日志
curl "http://localhost:8080/api/logs/operation"

# 查看接收到的访问日志
curl "http://localhost:8080/api/logs/access"

# 查看日志统计
curl "http://localhost:8080/api/logs/stats"
```


**预期结果**: 能看到操作日志和访问日志计数及数据

---

## 测试方式三：Elasticsearch 发送器

1. **启动应用**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=elasticsearch
```

2. **触发操作日志**
```bash
curl -X POST "http://localhost:8080/api/users" \
  -H "Content-Type: application/json" \
  -d '{"name":"es-test","email":"es@example.com","age":35}'
```

4. **触发访问日志**
```bash
curl -X POST "http://localhost:8080/api/simple/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"esuser","password":"test123"}'
```

5. **验证结果**
```bash
# 查看当天的操作日志索引
curl "http://localhost:9200/common-log-operation-log-$(date +%Y.%m.%d)/_search?pretty"

# 查看当天的访问日志索引
curl "http://localhost:9200/common-log-access-log-$(date +%Y.%m.%d)/_search?pretty"
```


**预期结果**: 能看到操作日志和访问日志存储在对应的 ES 索引中

---

## 测试方式四：Database 发送器

1. **准备数据库表**
```bash
# 创建操作日志表
docker exec postgres psql -U postgres -d log_test -c "
CREATE TABLE IF NOT EXISTS log_operation_log (
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
);"

# 创建访问日志表
docker exec postgres psql -U postgres -d log_test -c "
CREATE TABLE IF NOT EXISTS log_access_log (
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
);"
```

2. **启动应用**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=database
```

3. **触发操作日志**
```bash
curl -X POST "http://localhost:8080/api/users" \
  -H "Content-Type: application/json" \
  -d '{"name":"db-test","email":"db@test.com","age":28}'
```

4. **触发访问日志**
```bash
curl -X POST "http://localhost:8080/api/simple/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"dbuser","password":"test123"}'
```

5. **验证结果**
```bash
# 查询操作日志表
docker exec -it postgres psql -U postgres -d log_test -c "SELECT * FROM log_operation_log ORDER BY create_time DESC LIMIT 5;"

# 查询访问日志表  
docker exec -it postgres psql -U postgres -d log_test -c "SELECT * FROM log_access_log ORDER BY create_time DESC LIMIT 5;"

# 查看表结构
docker exec -it postgres psql -U postgres -d log_test -c "\d log_operation_log"
```

**预期结果**: 能看到操作日志和访问日志存储在 PostgreSQL 数据库表中

**注意**: 如果遇到数据库连接问题，请检查：
- PostgreSQL 服务是否正常运行
- 网络配置是否正确（localhost 解析）
- 数据库连接参数是否正确
- 代码逻辑已验证正确，DatabaseLogSender 能正确处理日志发送

---

## 快速测试方法

### 一键测试所有发送器
```bash
# 1. 启动所有服务
docker compose up -d

# 2. 测试 Kafka 发送器
cd example
mvn spring-boot:run -Dspring-boot.run.profiles=kafka &
sleep 10
curl -X POST "http://localhost:8080/api/users" -H "Content-Type: application/json" -d '{"name":"kafka-test","email":"kafka@test.com","age":25}'
curl -X POST "http://localhost:8080/api/simple/login" -H "Content-Type: application/json" -d '{"username":"kafkauser","password":"test123"}'
pkill -f "spring-boot:run.*kafka"

# 3. 测试 HTTP 发送器  
mvn spring-boot:run -Dspring-boot.run.profiles=http &
sleep 10
curl -X POST "http://localhost:8080/api/users" -H "Content-Type: application/json" -d '{"name":"http-test","email":"http@test.com","age":30}'
curl -X POST "http://localhost:8080/api/simple/login" -H "Content-Type: application/json" -d '{"username":"httpuser","password":"test123"}'
curl "http://localhost:8080/api/logs/stats"
pkill -f "spring-boot:run.*http"

# 4. 测试 Elasticsearch 发送器
mvn spring-boot:run -Dspring-boot.run.profiles=elasticsearch &
sleep 10  
curl -X POST "http://localhost:8080/api/users" -H "Content-Type: application/json" -d '{"name":"es-test","email":"es@test.com","age":35}'
curl -X POST "http://localhost:8080/api/simple/login" -H "Content-Type: application/json" -d '{"username":"esuser","password":"test123"}'
pkill -f "spring-boot:run.*elasticsearch"

# 5. 测试 Database 发送器
# 首先创建数据库表
docker exec postgres psql -U postgres -d log_test -c "CREATE TABLE IF NOT EXISTS log_operation_log (id VARCHAR(36) PRIMARY KEY, username VARCHAR(100), real_name VARCHAR(100), email VARCHAR(100), role_name VARCHAR(100), operation_type VARCHAR(50), description VARCHAR(500), operation_time VARCHAR(50), operation_timestamp TIMESTAMP, client_ip VARCHAR(50), ip_location VARCHAR(200), browser VARCHAR(100), operating_system VARCHAR(100), device_type VARCHAR(50), status VARCHAR(20), response_time BIGINT, request_uri VARCHAR(500), request_method VARCHAR(10), user_agent VARCHAR(500), session_id VARCHAR(100), module_name VARCHAR(100), target VARCHAR(200), before_data TEXT, after_data TEXT, exception_message TEXT, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP);"

docker exec postgres psql -U postgres -d log_test -c "CREATE TABLE IF NOT EXISTS log_access_log (id VARCHAR(36) PRIMARY KEY, username VARCHAR(100), real_name VARCHAR(100), email VARCHAR(100), access_type VARCHAR(50), description VARCHAR(500), access_time VARCHAR(50), access_timestamp TIMESTAMP, client_ip VARCHAR(50), ip_location VARCHAR(200), browser VARCHAR(100), operating_system VARCHAR(100), device_type VARCHAR(50), status VARCHAR(20), response_time BIGINT, request_uri VARCHAR(500), request_method VARCHAR(10), user_agent VARCHAR(500), session_id VARCHAR(100), module_name VARCHAR(100), target VARCHAR(200), exception_message TEXT, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP);"

mvn spring-boot:run -Dspring-boot.run.profiles=database &
sleep 10
curl -X POST "http://localhost:8080/api/users" -H "Content-Type: application/json" -d '{"name":"db-test","email":"db@test.com","age":28}'  
curl -X POST "http://localhost:8080/api/simple/login" -H "Content-Type: application/json" -d '{"username":"dbuser","password":"test123"}'
pkill -f "spring-boot:run.*database"
```

### 快速验证结果
```bash
# 验证 Kafka 消息
docker exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic operation-log --from-beginning --max-messages 5
docker exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic access-log --from-beginning --max-messages 5

# 验证 HTTP 接收
curl "http://localhost:8080/api/logs/stats"

# 验证 Elasticsearch 存储
curl "http://localhost:9200/common-log-operation-log-$(date +%Y.%m.%d)/_search?pretty"

# 验证 PostgreSQL 存储
docker exec -it postgres psql -U postgres -d log_test -c "SELECT count(*) FROM log_operation_log;"
```

### 清理测试数据
```bash
# 清理 HTTP 日志
curl -X DELETE "http://localhost:8080/api/logs"

# 清理 Elasticsearch 索引
curl -X DELETE "http://localhost:9200/common-log-*"

# 清理 PostgreSQL 数据
docker exec -it postgres psql -U postgres -d log_test -c "TRUNCATE TABLE log_operation_log, log_access_log;"

# 停止所有服务
docker compose down
```

## 故障排除

### 常见问题
1. **应用启动失败**: 检查 Docker 服务是否正常运行
2. **日志未发送**: 检查控制台是否有错误信息
3. **Kafka 连接失败**: 确认 `localhost:9092` 可访问
4. **ES 连接失败**: 确认 `localhost:9200` 可访问

### 验证配置加载
启动时查看日志输出：
```
选择日志发送器: ElasticsearchLogSender 用于类型: elasticsearch
```
这表明配置正确加载并选择了对应的发送器。

---

**测试完成标志**: 
- 控制台显示 "成功保存操作日志到XXX"
- 对应存储系统中能查询到日志数据
- 日志包含完整的操作信息（方法名、参数、响应时间等）