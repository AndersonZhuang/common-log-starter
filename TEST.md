# CommonLog Starter 测试文档

## 📋 快速导航

- [🚀 快速开始](#-快速开始) - 启动中间件和应用
- [📝 预设实体测试](#3-测试预设实体自动建表) - 测试OperationLogEntity和UserAccessLogEntity
- [🔧 自定义日志测试](#4-测试自定义日志和自定义字段) - 测试自定义字段和业务日志
- [💾 存储方式测试](#5-各种-sender-启动方法) - 数据库、Kafka、Elasticsearch、HTTP
- [🔍 日志查询](#6-查看中间件中的日志) - 查看各种存储中的日志数据
- [🛠️ 故障排除](#-故障排除) - 常见问题和解决方案

## 🚀 快速开始

### 1. 启动中间件
```bash
docker-compose up -d
```

### 2. 各种 Sender 启动方法

#### 数据库 Sender（预设实体自动建表）
```bash
cd example
mvn spring-boot:run -Dspring-boot.run.profiles=database
```

#### Kafka Sender
```bash
cd example
mvn spring-boot:run -Dspring-boot.run.profiles=kafka
```

#### Elasticsearch Sender
```bash
cd example
mvn spring-boot:run -Dspring-boot.run.profiles=elasticsearch
```

#### HTTP Sender
```bash
cd example
mvn spring-boot:run -Dspring-boot.run.profiles=http
```

### 3. 测试预设实体自动建表

> **重要更新**: `PresetEntityTestController` 已更新为支持多种存储方式。现在所有测试端点都会根据当前配置自动选择相应的发送器（数据库、Kafka、Elasticsearch、HTTP），无需修改代码即可在不同存储方式间切换。

### 4. 测试自定义日志和自定义字段

> **自定义日志功能**: 支持创建包含自定义字段的日志实体，自动建表存储，支持多种存储方式。

#### 4.1 基础日志测试（操作日志、访问日志）
```bash
# 测试操作日志
curl -X POST "http://localhost:8080/api/log-test/operation-log" \
  -d "userName=张三&email=zhangsan@example.com&role=管理员"

# 测试访问日志
curl -X POST "http://localhost:8080/api/log-test/access-log" \
  -d "username=testuser&password=123456"

# 测试自定义日志实体（包含自定义字段）
curl -X POST "http://localhost:8080/api/log-test/custom-log" \
  -d "transactionType=转账&amount=1000.50&currency=CNY&riskLevel=中等"
```

#### 4.2 业务日志测试（自定义字段演示）
```bash
# 测试业务日志（包含自定义字段：businessType, department, project, customField1, customField2）
curl -X POST "http://localhost:8080/api/custom-entity/business" \
  -d "businessType=订单处理&department=销售部&project=电商平台&customField1=VIP客户&customField2=紧急订单"

# 测试异步业务日志
curl -X POST "http://localhost:8080/api/custom-entity/async-business" \
  -d "businessType=库存管理&department=仓储部&project=WMS系统&customField1=自动补货&customField2=预警阈值"

# 测试指定Kafka发送器的业务日志
curl -X POST "http://localhost:8080/api/custom-entity/kafka-business" \
  -d "businessType=支付处理&department=财务部&project=支付系统"
```

#### 4.3 自定义字段说明

**CustomLogEntity 包含的自定义字段**：
- `userRole`: 用户角色
- `operationLevel`: 操作级别（1-低，2-中，3-高）
- `duration`: 操作耗时（毫秒）
- `affectedRows`: 影响的数据量
- `amount`: 业务金额
- `remarks`: 备注信息
- `beforeData`: 操作前的数据
- `afterData`: 操作后的数据
- `riskLevel`: 风险等级
- `approvalStatus`: 审批状态
- `deviceInfo`: 设备信息
- `location`: 地理位置
- `extensionData`: 扩展数据（JSON格式）

**BusinessLogEntity 包含的自定义字段**：
- `businessType`: 业务类型
- `department`: 部门
- `project`: 项目
- `customField1`: 自定义字段1
- `customField2`: 自定义字段2
- `businessData`: 业务数据
- `operationResult`: 操作结果
- `impactScope`: 影响范围

#### 5.1 数据库 Sender 测试
```bash
# 启动数据库配置
cd example
mvn spring-boot:run -Dspring-boot.run.profiles=database

# 测试操作日志实体自动建表
curl -X GET "http://localhost:8080/api/preset-test/operation-log"

# 测试用户访问日志实体自动建表
curl -X GET "http://localhost:8080/api/preset-test/user-access-log"

# 测试批量预设实体
curl -X GET "http://localhost:8080/api/preset-test/batch-test"
```

#### 5.2 Kafka Sender 测试
```bash
# 启动Kafka配置
cd example
mvn spring-boot:run -Dspring-boot.run.profiles=kafka

# 测试操作日志实体
curl -X GET "http://localhost:8080/api/preset-test/operation-log"

# 测试用户访问日志实体
curl -X GET "http://localhost:8080/api/preset-test/user-access-log"

# 测试批量预设实体
curl -X GET "http://localhost:8080/api/preset-test/batch-test"

# 预期输出示例：
# ✅ 用户访问日志测试成功 - 存储方式: kafka, ID: 07b98f9a-39a6-4976-ba64-3f5eb9b6239d, Topic: log_user_access_log_entity
# ✅ 操作日志测试成功 - 存储方式: kafka, ID: d612f934-4a0f-4ae0-b569-0cc369b7fc30, Topic: log_operation_log_entity
# ✅ 批量测试成功 - 存储方式: kafka, 发送了3条预设实体日志
```

#### 5.3 Elasticsearch Sender 测试
```bash
# 启动Elasticsearch配置
cd example
mvn spring-boot:run -Dspring-boot.run.profiles=elasticsearch

# 测试操作日志实体
curl -X GET "http://localhost:8080/api/preset-test/operation-log"

# 测试用户访问日志实体
curl -X GET "http://localhost:8080/api/preset-test/user-access-log"

# 测试批量预设实体
curl -X GET "http://localhost:8080/api/preset-test/batch-test"
```

#### 5.4 HTTP Sender 测试
```bash
# 启动HTTP配置
cd example
mvn spring-boot:run -Dspring-boot.run.profiles=http

# 测试操作日志实体
curl -X GET "http://localhost:8080/api/preset-test/operation-log"

# 测试用户访问日志实体
curl -X GET "http://localhost:8080/api/preset-test/user-access-log"

# 测试批量预设实体
curl -X GET "http://localhost:8080/api/preset-test/batch-test"
```

### 6. 查看中间件中的日志

#### 数据库查询
```bash
# 查看创建的表
docker exec postgres psql -U postgres -d log_test -c "\dt"

# 查看操作日志数据
docker exec postgres psql -U postgres -d log_test -c "SELECT * FROM log_operation LIMIT 5;"

# 查看用户访问日志数据
docker exec postgres psql -U postgres -d log_test -c "SELECT * FROM log_user_access LIMIT 5;"
```

#### Kafka 查询
```bash
# 查看所有主题
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092

# 查看日志相关主题
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092 | grep log

# 消费用户访问日志消息
docker exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic log_user_access_log_entity \
  --from-beginning \
  --max-messages 1

# 消费操作日志消息
docker exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic log_operation_log_entity \
  --from-beginning \
  --max-messages 1

# 实时消费所有日志消息
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic log_user_access_log_entity \
  --from-beginning

# 消费自定义日志消息（CustomLogEntity）
docker exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic log_custom_log_entity \
  --from-beginning \
  --max-messages 1

# 消费业务日志消息（BusinessLogEntity）
docker exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic log_business_log_entity \
  --from-beginning \
  --max-messages 1
```

#### Elasticsearch 查询
```bash
# 查看日志索引
curl -s "localhost:9200/_cat/indices?v" | grep logs

# 查询操作日志数据
curl -X GET "localhost:9200/logs-operation/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": {"match_all": {}},
  "size": 5
}'

# 打开 Kibana 界面
open http://localhost:5601
```

#### HTTP 查询
```bash
# 启动HTTP接收端点进行测试
python3 -c "
from http.server import HTTPServer, BaseHTTPRequestHandler
import json
import datetime

class LogHandler(BaseHTTPRequestHandler):
    def do_POST(self):
        content_length = int(self.headers['Content-Length'])
        post_data = self.rfile.read(content_length)
        print(f'[{datetime.datetime.now()}] 收到日志: {post_data.decode(\"utf-8\")}')
        self.send_response(200)
        self.send_header('Content-Type', 'application/json')
        self.end_headers()
        self.wfile.write(json.dumps({'status': 'success'}).encode())

print('HTTP日志接收服务器启动在 http://localhost:8081')
HTTPServer(('localhost', 8081), LogHandler).serve_forever()
"
```

### 7. 快速测试脚本

使用项目提供的快速测试脚本：
```bash
./quick-test.sh
```

### 8. 清理环境
```bash
# 停止应用
# Ctrl+C 或者 kill -9 <pid>

# 停止中间件
docker-compose down

# 清理数据
docker-compose down -v
```

---

**注意**: 
- 每种sender都有对应的配置文件，使用 `-Dspring-boot.run.profiles=<profile>` 启动
- 预设实体自动建表功能需要数据库发送器支持
- 测试前确保相关中间件正在运行

## 🔧 故障排除

### 常见问题

#### 1. "❌ 数据库发送器未配置，请检查配置"
**原因**: 控制器直接注入了数据库发送器，但当前配置使用的是其他存储方式。

**解决方案**: 已修复！现在 `PresetEntityTestController` 会自动根据当前配置选择相应的发送器。

#### 2. Kafka命令找不到
**错误**: `kafka-topics.sh: executable file not found`

**解决方案**: 使用正确的命令格式：
```bash
# 错误命令
docker exec kafka kafka-topics.sh --list --bootstrap-server localhost:9092

# 正确命令
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092
```

#### 3. 应用启动失败
**检查步骤**:
1. 确保在正确的目录：`cd example`
2. 确保Maven插件可用：`mvn clean compile`
3. 检查端口占用：`lsof -i :8080`

#### 4. Kafka连接失败
**检查步骤**:
1. 确保Kafka容器运行：`docker ps | grep kafka`
2. 检查Kafka端口：`docker port kafka`
3. 测试连接：`docker exec kafka kafka-topics --list --bootstrap-server localhost:9092`

### 验证步骤

#### 验证Kafka配置
```bash
# 1. 检查Kafka容器状态
docker ps | grep kafka

# 2. 列出所有topics
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092

# 3. 测试预设实体API
curl -X GET "http://localhost:8080/api/preset-test/user-access-log"

# 4. 测试自定义日志API
curl -X POST "http://localhost:8080/api/log-test/custom-log" \
  -d "transactionType=转账&amount=1000.50&currency=CNY&riskLevel=中等"

# 5. 测试业务日志API
curl -X POST "http://localhost:8080/api/custom-entity/business" \
  -d "businessType=订单处理&department=销售部&project=电商平台&customField1=VIP客户&customField2=紧急订单"

# 6. 验证消息发送
docker exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic log_user_access_log_entity \
  --from-beginning \
  --max-messages 1

# 7. 验证自定义日志消息
docker exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic log_custom_log_entity \
  --from-beginning \
  --max-messages 1

# 8. 验证业务日志消息
docker exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic log_business_log_entity \
  --from-beginning \
  --max-messages 1
```

#### 预期输出示例
```bash
# 预设实体测试输出
✅ 用户访问日志测试成功 - 存储方式: kafka, ID: 07b98f9a-39a6-4976-ba64-3f5eb9b6239d, Topic: log_user_access_log_entity

# 自定义日志API输出
{"transactionType":"转账","amount":1000.50,"riskLevel":"中等","currency":"CNY","message":"交易处理成功","transactionId":"94793d79-c9f8-4823-a5b2-0a7745f05772","status":"SUCCESS"}

# 业务日志API输出
{"customField1":"VIP客户","customField2":"紧急订单","project":"电商平台","businessType":"订单处理","department":"销售部","message":"业务操作测试成功","timestamp":1756888645064}

# Kafka消息示例（包含自定义字段结构）
{"id":"7e5d2a16293b43c1bc972d088322cfd1","timestamp":[2025,9,3,16,37,20,478846000],"content":"金融交易：转账 - 金额：1000.50","level":"INFO","userRole":null,"operationLevel":null,"duration":null,"affectedRows":null,"amount":null,"remarks":null,"beforeData":null,"afterData":null,"riskLevel":null,"approvalStatus":null,"deviceInfo":null,"location":null,"extensionData":null}
```