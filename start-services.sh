#!/bin/bash

echo "🚀 启动日志服务栈..."

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker未运行，请先启动Docker Desktop"
    exit 1
fi

# 停止可能存在的旧容器
echo "🛑 停止旧容器..."
docker-compose down

# 启动服务
echo "📦 启动服务..."
docker-compose up -d

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 30

# 检查服务状态
echo "🔍 检查服务状态..."

# 检查Kafka
echo "📊 检查Kafka..."
if curl -s http://localhost:9092 > /dev/null 2>&1; then
    echo "✅ Kafka 运行正常 (端口: 9092)"
else
    echo "❌ Kafka 启动失败"
fi

# 检查Elasticsearch
echo "🔍 检查Elasticsearch..."
if curl -s http://localhost:9200 > /dev/null 2>&1; then
    echo "✅ Elasticsearch 运行正常 (端口: 9200)"
    echo "   - 健康状态: $(curl -s http://localhost:9200/_cluster/health | jq -r '.status' 2>/dev/null || echo 'unknown')"
else
    echo "❌ Elasticsearch 启动失败"
fi

# 检查Kibana
echo "📈 检查Kibana..."
if curl -s http://localhost:5601 > /dev/null 2>&1; then
    echo "✅ Kibana 运行正常 (端口: 5601)"
    echo "   - 访问地址: http://localhost:5601"
else
    echo "❌ Kibana 启动失败"
fi

# 检查Logstash
echo "📝 检查Logstash..."
if curl -s http://localhost:9600 > /dev/null 2>&1; then
    echo "✅ Logstash 运行正常 (端口: 9600)"
else
    echo "❌ Logstash 启动失败"
fi

echo ""
echo "🎉 服务启动完成！"
echo ""
echo "📋 服务访问地址："
echo "   - Elasticsearch: http://localhost:9200"
echo "   - Kibana: http://localhost:5601"
echo "   - Kafka: localhost:9092"
echo "   - Logstash: localhost:9600"
echo ""
echo "🔧 下一步："
echo "   1. 修改example项目的配置文件，启用Kafka日志发送"
echo "   2. 启动example项目"
echo "   3. 测试日志记录功能"
echo "   4. 在Kibana中查看日志数据"
echo ""
echo "📚 查看日志："
echo "   docker-compose logs -f [service_name]"
echo "   例如: docker-compose logs -f kafka"
