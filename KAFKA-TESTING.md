# Kafka环境测试指南

本文档详细说明如何在Kafka环境下测试 `common-log-starter` 的完整功能。

## 🚀 环境准备

### 1. 启动日志服务栈

```bash
# 启动所有服务（Kafka、Elasticsearch、Kibana、Logstash）
./start-services.sh

# 等待服务启动完成（约30秒）
```

### 2. 验证服务状态

```bash
# 检查Kafka
curl -s http://localhost:9092

# 检查Elasticsearch
curl -s http://localhost:9200/_cluster/health

# 检查Kibana
curl -s http://localhost:5601

# 检查Logstash
curl -s http://localhost:9600
```

## 🧪 测试步骤

### 步骤1：启动Example项目（Kafka模式）

```bash
cd example

# 使用Kafka配置文件启动
mvn spring-boot:run -Dspring-boot.run.profiles=kafka
```

### 步骤2：测试日志记录

#### 基础测试
```bash
# 测试访问日志
curl -X GET "http://localhost:8080/api/test/normal"

# 测试操作日志
curl -X POST "http://localhost:8080/api/users" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com"}'

# 测试异常日志
curl -X GET "http://localhost:8080/api/test/exception"
```

#### 批量测试
```bash
# 连续发送多个请求
for i in {1..10}; do
  curl -X GET "http://localhost:8080/api/test/normal?count=$i"
  sleep 0.5
done
```

### 步骤3：验证日志流转

#### 检查Kafka Topic
```bash
# 查看Kafka Topic列表
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092

# 查看访问日志Topic
docker exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic access-log \
  --from-beginning \
  --max-messages 5

# 查看操作日志Topic
docker exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic operation-log \
  --from-beginning \
  --max-messages 5
```

#### 检查Elasticsearch索引
```bash
# 查看索引列表
curl -s "http://localhost:9200/_cat/indices?v"

# 查看访问日志数据
curl -s "http://localhost:9200/logs-access-*/_search?pretty" | jq '.hits.total.value'

# 查看操作日志数据
curl -s "http://localhost:9200/logs-operation-*/_search?pretty" | jq '.hits.total.value'
```

#### 检查Logstash状态
```bash
# 查看Logstash管道状态
curl -s "http://localhost:9600/_node/stats/pipeline?pretty"

# 查看Logstash日志
docker logs logstash --tail 20
```

### 步骤4：在Kibana中查看日志

1. 打开浏览器访问：http://localhost:5601
2. 进入 **Discover** 页面
3. 创建索引模式：
   - 模式名称：`logs-*`
   - 时间字段：`@timestamp`
4. 查看日志数据：
   - 筛选 `log_type: access` 查看访问日志
   - 筛选 `log_type: operation` 查看操作日志

## 📊 测试成功标准

### ✅ 成功标志

1. **项目启动成功**
   - 控制台显示 "Started ExampleApplication"
   - 没有Kafka连接错误

2. **日志发送成功**
   - 控制台显示日志发送信息
   - Kafka Topic中有数据

3. **日志处理成功**
   - Logstash正常处理日志
   - Elasticsearch中有索引和数据

4. **日志查看成功**
   - Kibana能正常访问
   - 能看到记录的日志数据

### ❌ 失败标志

1. **Kafka连接失败**
   - 控制台显示连接错误
   - 日志无法发送

2. **日志处理失败**
   - Logstash报错
   - Elasticsearch无数据

3. **服务异常**
   - 某个服务无法启动
   - 端口被占用

## 🔧 故障排除

### 问题1：Kafka连接失败

**症状**：控制台显示 "Failed to connect to Kafka"

**解决方案**：
```bash
# 检查Kafka状态
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092

# 重启Kafka服务
docker restart kafka

# 检查网络连接
telnet localhost 9092
```

### 问题2：Elasticsearch启动失败

**症状**：Elasticsearch容器状态为Exited

**解决方案**：
```bash
# 检查日志
docker logs elasticsearch

# 增加内存限制
# 在docker-compose.yml中修改ES_JAVA_OPTS
# - "ES_JAVA_OPTS=-Xms1g -Xmx1g"

# 重启服务
docker-compose restart elasticsearch
```

### 问题3：Logstash无法处理日志

**症状**：Elasticsearch中没有数据

**解决方案**：
```bash
# 检查Logstash配置
docker exec logstash cat /usr/share/logstash/pipeline/logstash.conf

# 查看Logstash日志
docker logs logstash

# 重启Logstash
docker restart logstash
```

## 📈 性能测试

### 压力测试
```bash
# 使用ab进行压力测试
ab -n 1000 -c 10 "http://localhost:8080/api/test/normal"

# 检查日志处理性能
docker exec kafka kafka-run-class kafka.tools.ConsumerPerformance \
  --bootstrap-server localhost:9092 \
  --topic access-log \
  --messages 1000
```

### 监控指标
- Kafka消息吞吐量
- Elasticsearch索引速度
- Logstash处理延迟
- 系统资源使用情况

## 🧹 清理环境

### 停止服务
```bash
# 停止所有服务
./stop-services.sh

# 完全清理（包括数据）
docker-compose down -v
```

### 清理数据
```bash
# 清理Docker卷
docker volume prune

# 清理Docker镜像
docker image prune
```

## 📚 参考资源

- [Kafka官方文档](https://kafka.apache.org/documentation/)
- [Elasticsearch官方文档](https://www.elastic.co/guide/index.html)
- [Logstash官方文档](https://www.elastic.co/guide/en/logstash/current/index.html)
- [Docker Compose文档](https://docs.docker.com/compose/)

## 🆘 获取帮助

如果遇到问题：
1. 检查服务状态和日志
2. 参考故障排除部分
3. 查看Docker容器状态
4. 提交GitHub Issue
