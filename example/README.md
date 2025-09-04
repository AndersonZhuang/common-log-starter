# Common Log Starter 示例项目

这是一个展示Common Log Starter功能的示例Spring Boot应用。

## 项目结构

```
example/
├── src/main/java/com/diit/example/
│   ├── controller/          # 控制器层
│   │   ├── LogTestController.java      # 日志测试控制器
│   │   └── UserController.java         # 用户管理控制器
│   ├── entity/             # 实体类
│   │   ├── BusinessLogEntity.java      # 业务日志实体
│   │   ├── OrderLogEntity.java         # 订单日志实体
│   │   └── UserActivityLogEntity.java  # 用户活动日志实体
│   ├── service/            # 服务层
│   │   └── DirectKafkaService.java     # 直接Kafka服务
│   ├── exception/          # 异常处理
│   │   └── GlobalExceptionHandler.java # 全局异常处理器
│   └── ExampleApplication.java         # 启动类
└── src/main/resources/
    ├── application.yml              # 主配置文件
    ├── application-kafka.yml        # Kafka配置
    ├── application-elasticsearch.yml # Elasticsearch配置
    ├── application-database.yml     # 数据库配置
    └── application-http.yml         # HTTP配置
```

## 快速开始

### 1. 环境准备

#### 启动依赖服务
```bash
# 启动Docker服务（Kafka、Elasticsearch、PostgreSQL等）
docker compose up -d

# 启动HTTP日志服务器
python3 http-log-server.py
```

#### 编译安装Starter
```bash
# 在项目根目录
mvn clean install -DskipTests
```

### 2. 运行示例

#### Kafka模式
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=kafka
```

#### Elasticsearch模式
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=elasticsearch
```

#### Database模式
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=database
```

#### HTTP模式
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=http
```

### 3. 测试API

#### 用户管理API
```bash
# 创建用户（操作日志）
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"张三","email":"zhangsan@example.com"}'

# 获取用户列表（用户访问日志）
curl -X GET http://localhost:8080/api/users

# 删除用户（操作日志）
curl -X DELETE http://localhost:8080/api/users/1
```

#### 日志测试API
```bash
# 测试业务日志（通用日志）
curl -X POST http://localhost:8080/api/logs/test/business \
  -H "Content-Type: application/json" \
  -d '{"businessType":"订单处理","department":"技术部","project":"电商系统"}'

# 测试订单日志（通用日志）
curl -X POST http://localhost:8080/api/logs/test/order \
  -H "Content-Type: application/json" \
  -d '{"orderId":"ORD001","amount":99.99,"status":"已支付"}'

# 测试用户活动日志（通用日志）
curl -X POST http://localhost:8080/api/logs/test/user-activity \
  -H "Content-Type: application/json" \
  -d '{"userId":"123","activity":"登录","ip":"192.168.1.100"}'
```

## 配置说明

### 1. 主配置文件 (application.yml)
```yaml
# 基础配置
server:
  port: 8080

spring:
  application:
    name: common-log-starter-example

# 日志Starter配置
diit:
  log:
    enabled: true
    storage:
      type: kafka  # 默认使用Kafka
      async: true
      batchSize: 100
      batchInterval: 1000
```

### 2. Kafka配置 (application-kafka.yml)
```yaml
diit:
  log:
    storage:
      type: kafka
    kafka:
      enabled: true
      bootstrapServers: localhost:9092
      accessLogTopic: access-log
      operationLogTopic: operation-log
    http:
      enabled: false  # 禁用HTTP发送器
```

### 3. Elasticsearch配置 (application-elasticsearch.yml)
```yaml
diit:
  log:
    storage:
      type: elasticsearch
    elasticsearch:
      enabled: true
      hosts: localhost:9200
      indexPrefix: common-log
    http:
      enabled: false
```

### 4. Database配置 (application-database.yml)
```yaml
diit:
  log:
    storage:
      type: database
    database:
      enabled: true
      tableName: log  # 自定义表名
      autoCreateTable: false  # 手动建表
    http:
      enabled: false

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/postgres
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
```

### 5. HTTP配置 (application-http.yml)
```yaml
diit:
  log:
    storage:
      type: http
    http:
      enabled: true
      genericEndpoint: http://localhost:8080/api/logs/generic
    kafka:
      enabled: false
    elasticsearch:
      enabled: false
```

## 实体类说明

### 1. BusinessLogEntity
```java
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessLogEntity extends BaseLogEntity {
    private String businessType;  // 业务类型
    private String department;    // 部门
    private String project;       // 项目
}
```

### 2. OrderLogEntity
```java
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderLogEntity extends BaseLogEntity {
    private String orderId;       // 订单ID
    private Double amount;        // 金额
    private String status;        // 状态
}
```

### 3. UserActivityLogEntity
```java
@Data
@EqualsAndHashCode(callSuper = true)
public class UserActivityLogEntity extends BaseLogEntity {
    private String userId;        // 用户ID
    private String activity;      // 活动类型
    private String ip;           // IP地址
}
```

## 日志注解使用

### 1. @OperationLog - 操作日志
```java
@OperationLog(module = "用户管理", operation = "创建用户", description = "创建用户: #{#user.name}")
public User createUser(@RequestBody User user) {
    // 业务逻辑
}
```

### 2. @UserAccessLog - 用户访问日志
```java
@UserAccessLog(description = "访问用户列表页面")
public List<User> getUserList() {
    // 业务逻辑
}
```

### 3. @GenericLog - 通用日志
```java
@GenericLog(description = "业务操作: #{#businessType}")
public void processBusiness(@RequestBody BusinessLogEntity businessLog) {
    // 业务逻辑
}
```

## 查看日志数据

### 1. Kafka日志
```bash
# 查看操作日志
docker exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic operation-log --from-beginning

# 查看用户访问日志
docker exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic access-log --from-beginning

# 查看通用日志
docker exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic log_business_log_entity --from-beginning
```

### 2. Elasticsearch日志
```bash
# 查看所有日志
curl -X GET "localhost:9200/logs-*/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": {"match_all": {}},
  "size": 10
}'
```

### 3. Database日志
```bash
# 连接PostgreSQL
docker exec -it postgres psql -U postgres -d postgres

# 查看日志数据
SELECT * FROM log ORDER BY timestamp DESC LIMIT 10;
```

### 4. HTTP日志
查看HTTP日志服务器控制台输出。

## 故障排除

### 1. 端口冲突
```bash
# 查看端口占用
lsof -i :8080

# 杀死占用进程
lsof -ti :8080 | xargs kill -9
```

### 2. 依赖服务未启动
```bash
# 检查Docker服务状态
docker ps

# 重启所有服务
docker compose restart
```

### 3. 数据库连接失败
```bash
# 检查PostgreSQL状态
docker exec -it postgres psql -U postgres -c "SELECT version();"
```

## 开发说明

### 1. 添加新的实体类
1. 继承`BaseLogEntity`
2. 添加自定义字段
3. 使用`@GenericLog`注解记录日志

### 2. 添加新的API
1. 在Controller中添加方法
2. 使用相应的日志注解
3. 测试日志记录功能

### 3. 自定义配置
1. 修改对应的`application-*.yml`文件
2. 重启应用使配置生效
3. 验证配置是否正确
