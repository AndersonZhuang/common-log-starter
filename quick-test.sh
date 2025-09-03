#!/bin/bash

echo "=== CommonLog 快速测试 ==="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 启动中间件
echo -e "${BLUE}启动中间件...${NC}"
docker-compose up -d
sleep 30

# 检查服务状态
echo -e "${BLUE}检查服务状态...${NC}"
services=("Elasticsearch:9200" "Kafka:9092" "PostgreSQL:5433")

for service in "${services[@]}"; do
    IFS=':' read -r name port <<< "$service"
    if curl -s localhost:$port > /dev/null 2>&1; then
        echo -e "${GREEN}✓${NC} $name 运行正常"
    else
        echo -e "${RED}✗${NC} $name 未运行"
    fi
done

# 创建日志目录
mkdir -p logs

# 测试函数
test_sender() {
    local profile=$1
    local sender_name=$2
    local port=8080
    
    echo -e "\n${BLUE}测试${sender_name}发送器...${NC}"
    cd example
    mvn spring-boot:run -Dspring-boot.run.profiles=$profile > ../logs/${profile}.log 2>&1 &
    app_pid=$!
    cd ..
    
    sleep 20
    
    if curl -s http://localhost:$port/actuator/health > /dev/null; then
        echo -e "${GREEN}✓${NC} ${sender_name}应用启动成功"
        
        # 测试预设实体接口
        echo "测试预设实体类自动建表..."
        curl -s -X GET "http://localhost:$port/api/preset-test/operation-log"
        curl -s -X GET "http://localhost:$port/api/preset-test/user-access-log"
        curl -s -X GET "http://localhost:$port/api/preset-test/batch-test"
        
        # 根据发送器类型验证数据
        case $profile in
            "database")
                echo -e "\n验证数据库表创建:"
                docker exec postgres psql -U postgres -d log_test -c "\dt" 2>/dev/null
                docker exec postgres psql -U postgres -d log_test -c "SELECT COUNT(*) FROM log_operation;" 2>/dev/null
                docker exec postgres psql -U postgres -d log_test -c "SELECT COUNT(*) FROM log_user_access;" 2>/dev/null
                ;;
            "kafka")
                echo -e "\n验证Kafka主题创建:"
                docker exec kafka kafka-topics.sh --list --bootstrap-server localhost:9092 | grep log 2>/dev/null
                ;;
            "elasticsearch")
                echo -e "\n验证Elasticsearch索引创建:"
                curl -s "localhost:9200/_cat/indices?v" | grep logs 2>/dev/null
                ;;
            "http")
                echo -e "\nHTTP发送器测试完成（需要手动启动接收端点）"
                ;;
        esac
        
    else
        echo -e "${RED}✗${NC} ${sender_name}应用启动失败"
    fi
    
    # 停止应用
    kill $app_pid 2>/dev/null
    sleep 5
    pkill -f "spring-boot:run.*$profile" 2>/dev/null
}

# 测试数据库发送器
test_sender "database" "数据库"

# 测试Kafka发送器
test_sender "kafka" "Kafka"

# 测试Elasticsearch发送器
test_sender "elasticsearch" "Elasticsearch"

# 测试HTTP发送器
test_sender "http" "HTTP"

echo -e "\n${GREEN}测试完成！${NC}"
echo -e "${BLUE}查看日志:${NC} ls -la logs/"
echo -e "${BLUE}清理环境:${NC} docker-compose down"