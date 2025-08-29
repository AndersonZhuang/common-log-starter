#!/bin/bash

echo "🛑 停止日志服务栈..."

# 停止所有服务
docker-compose down

# 清理数据卷（可选，取消注释以清理所有数据）
# echo "🧹 清理数据卷..."
# docker-compose down -v

echo "✅ 所有服务已停止"
echo ""
echo "💡 提示："
echo "   - 数据已保存到Docker卷中"
echo "   - 如需完全清理，运行: docker-compose down -v"
echo "   - 查看容器状态: docker-compose ps"
