#!/usr/bin/env python3
"""
独立的HTTP日志接收服务器
用于测试HTTP Sender的真实发送能力
"""

import json
import socketserver
import http.server
from datetime import datetime
import threading
import time

class LogHandler(http.server.BaseHTTPRequestHandler):
    def do_POST(self):
        if self.path == '/api/logs/receive':
            # 读取请求体
            content_length = int(self.headers.get('Content-Length', 0))
            post_data = self.rfile.read(content_length)
            
            try:
                # 解析JSON数据
                log_data = json.loads(post_data.decode('utf-8'))
                
                # 打印接收到的日志
                print(f"\n{'='*60}")
                print(f"📥 接收到HTTP日志 - {datetime.now().strftime('%H:%M:%S')}")
                print(f"   来源: {self.headers.get('X-Log-Source', 'Unknown')}")
                print(f"   路径: {self.path}")
                print(f"   内容: {json.dumps(log_data, indent=2, ensure_ascii=False)}")
                print(f"{'='*60}")
                
                # 返回成功响应
                self.send_response(200)
                self.send_header('Content-Type', 'application/json')
                self.end_headers()
                
                response = {
                    "success": True,
                    "message": "日志接收成功",
                    "timestamp": int(time.time() * 1000),
                    "receivedData": log_data
                }
                
                self.wfile.write(json.dumps(response, ensure_ascii=False).encode('utf-8'))
                
            except Exception as e:
                print(f"❌ 解析日志数据失败: {e}")
                self.send_response(400)
                self.send_header('Content-Type', 'application/json')
                self.end_headers()
                
                error_response = {
                    "success": False,
                    "message": f"解析失败: {str(e)}",
                    "timestamp": int(time.time() * 1000)
                }
                
                self.wfile.write(json.dumps(error_response, ensure_ascii=False).encode('utf-8'))
        else:
            self.send_response(404)
            self.end_headers()
            self.wfile.write(b'Not Found')
    
    def do_GET(self):
        if self.path == '/health':
            self.send_response(200)
            self.send_header('Content-Type', 'application/json')
            self.end_headers()
            
            health_response = {
                "status": "UP",
                "service": "HTTP Log Server",
                "timestamp": int(time.time() * 1000)
            }
            
            self.wfile.write(json.dumps(health_response, ensure_ascii=False).encode('utf-8'))
        else:
            self.send_response(404)
            self.end_headers()
            self.wfile.write(b'Not Found')
    
    def log_message(self, format, *args):
        # 禁用默认的访问日志
        pass

def start_server(port=8081):
    """启动HTTP日志服务器"""
    try:
        with socketserver.TCPServer(("", port), LogHandler) as httpd:
            print(f"🚀 HTTP日志服务器启动成功")
            print(f"📡 端口: {port}")
            print(f"🌐 访问地址: http://localhost:{port}")
            print(f"📝 接收端点: http://localhost:{port}/api/logs/receive")
            print(f"💚 健康检查: http://localhost:{port}/health")
            print(f"⏹️  按 Ctrl+C 停止服务器")
            print(f"{'='*60}")
            
            httpd.serve_forever()
            
    except KeyboardInterrupt:
        print(f"\n⏹️  服务器已停止")
    except OSError as e:
        if e.errno == 48:  # Address already in use
            print(f"❌ 端口{port}已被占用，请先停止占用该端口的进程")
            print(f"💡 可以使用以下命令清理端口：")
            print(f"   lsof -ti :{port} | xargs kill -9")
        else:
            print(f"❌ 服务器启动失败: {e}")

if __name__ == "__main__":
    start_server()
