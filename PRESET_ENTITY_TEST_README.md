# 预设实体类自动建表功能测试指南

## 概述

本指南介绍如何测试 `OperationLogEntity` 和 `UserAccessLogEntity` 这两个预设日志实体类的自动建表功能。

## 快速开始

### 一键测试（推荐）

```bash
# 运行一键测试脚本
./quick-test.sh
```

这个脚本会自动完成所有测试步骤，包括：
- 启动中间件
- 编译项目
- 测试各种发送器
- 验证数据存储

### 手动测试步骤

#### 1. 启动环境

```bash
# 启动所有中间件
docker-compose up -d

# 等待服务启动
sleep 30
```

#### 2. 测试数据库发送器（自动建表）

```bash
# 启动应用（数据库配置）
cd example
mvn spring-boot:run -Dspring-boot.run.profiles=database

# 测试预设实体自动建表
curl -X GET "http://localhost:8088/api/preset-test/operation-log"
curl -X GET "http://localhost:8088/api/preset-test/user-access-log"
curl -X GET "http://localhost:8088/api/preset-test/batch-test"
```

#### 3. 验证数据库表创建

```bash
# 查看创建的表
docker exec postgres psql -U postgres -d log_test -c "\dt"

# 查看表结构
docker exec postgres psql -U postgres -d log_test -c "\d log_operation"
docker exec postgres psql -U postgres -d log_test -c "\d log_user_access"

# 查看数据
docker exec postgres psql -U postgres -d log_test -c "SELECT * FROM log_operation LIMIT 5;"
docker exec postgres psql -U postgres -d log_test -c "SELECT * FROM log_user_access LIMIT 5;"
```

## 测试接口说明

### 预设实体测试接口

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/preset-test/operation-log` | GET | 测试操作日志实体自动建表 |
| `/api/preset-test/user-access-log` | GET | 测试用户访问日志实体自动建表 |
| `/api/preset-test/batch-test` | GET | 测试批量预设实体 |

### 预期结果

- **操作日志实体**：自动创建 `log_operation` 表，包含所有 `OperationLogEntity` 字段
- **用户访问日志实体**：自动创建 `log_user_access` 表，包含所有 `UserAccessLogEntity` 字段
- **表结构完全匹配**：数据库表结构与实体类字段完全一致
- **自动索引**：为常用查询字段创建索引

## 中间件验证

### 数据库验证

```bash
# 查看所有日志表
docker exec postgres psql -U postgres -d log_test -c "
SELECT tablename, schemaname 
FROM pg_tables 
WHERE tablename LIKE 'log_%'
ORDER BY tablename;
"

# 统计各表记录数
docker exec postgres psql -U postgres -d log_test -c "
SELECT 
    'log_operation' as table_name, 
    COUNT(*) as record_count 
FROM log_operation
UNION ALL
SELECT 
    'log_user_access' as table_name, 
    COUNT(*) as record_count 
FROM log_user_access;
"
```

### Kafka 验证

```bash
# 查看日志主题
docker exec kafka kafka-topics.sh --list --bootstrap-server localhost:9092 | grep log

# 消费消息
docker exec -it kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic log_operation \
  --from-beginning
```

### Elasticsearch 验证

```bash
# 查看日志索引
curl -s "localhost:9200/_cat/indices?v" | grep logs

# 查询数据
curl -X GET "localhost:9200/logs-operation/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": {"match_all": {}},
  "size": 5
}'
```

## 表结构说明

### log_operation 表

包含 `OperationLogEntity` 的所有字段：
- 基础字段：`id`, `username`, `real_name`, `email`, `role_name`
- 操作字段：`operation_type`, `description`, `operation_time`, `operation_timestamp`
- 环境字段：`client_ip`, `ip_location`, `browser`, `operating_system`, `device_type`
- 状态字段：`status`, `response_time`, `request_uri`, `request_method`
- 业务字段：`module`, `target`, `before_data`, `after_data`
- 审计字段：`created_at`, `updated_at`

### log_user_access 表

包含 `UserAccessLogEntity` 的所有字段：
- 基础字段：`id`, `username`, `real_name`, `email`
- 访问字段：`access_type`, `description`, `access_time`, `access_timestamp`
- 环境字段：`client_ip`, `ip_location`, `browser`, `operating_system`, `device_type`
- 状态字段：`status`, `response_time`, `request_uri`, `request_method`
- 业务字段：`module`, `target`, `session_id`
- 审计字段：`created_at`, `updated_at`

## 故障排查

### 常见问题

1. **应用启动失败**
   - 检查端口 8088 是否被占用
   - 确认中间件服务正常运行
   - 查看应用日志：`tail -f logs/database-test.log`

2. **数据库连接失败**
   - 确认 PostgreSQL 容器运行正常
   - 检查数据库配置是否正确
   - 验证数据库权限

3. **表创建失败**
   - 检查数据库用户权限
   - 确认表名不冲突
   - 查看应用日志中的错误信息

### 日志查看

```bash
# 查看应用日志
tail -f logs/database-test.log
tail -f logs/kafka-test.log
tail -f logs/elasticsearch-test.log

# 查看 Docker 容器日志
docker-compose logs postgres
docker-compose logs kafka
docker-compose logs elasticsearch
```

## 清理

```bash
# 停止应用
pkill -f "spring-boot:run"

# 停止中间件
docker-compose down

# 清理数据
docker-compose down -v

# 清理日志
rm -rf logs/
```

## 更多信息

- 详细测试文档：[TEST.md](TEST.md)
- 预设实体功能文档：[PRESET_ENTITY_AUTO_TABLE.md](PRESET_ENTITY_AUTO_TABLE.md)
- 项目 README：[README.md](README.md)
