package com.diit.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 日志接收控制器
 * 用于接收HTTP发送器发送的日志
 * 
 * @author zzx
 */
@Slf4j
@RestController
@RequestMapping("/api/logs")
public class LogReceiverController {
    
    /**
     * 接收日志数据
     */
    @PostMapping("/receive")
    public Map<String, Object> receiveLog(@RequestBody Map<String, Object> logData,
                                         @RequestHeader(value = "X-Log-Source", required = false) String logSource) {
        
        log.info("📥 接收到HTTP日志数据:");
        log.info("   来源: {}", logSource);
        log.info("   数据: {}", logData);
        
        // 这里可以处理接收到的日志数据
        // 比如保存到数据库、发送到其他系统等
        
        return Map.of(
            "success", true,
            "message", "日志接收成功",
            "timestamp", System.currentTimeMillis(),
            "receivedData", logData
        );
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
            "status", "UP",
            "service", "LogReceiver",
            "timestamp", System.currentTimeMillis()
        );
    }
}