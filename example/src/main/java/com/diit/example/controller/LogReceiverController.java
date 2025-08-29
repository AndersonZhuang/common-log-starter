package com.diit.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日志接收控制器
 * 用于接收和显示日志，便于测试starter功能
 * 
 * @author diit
 */
@Slf4j
@RestController
@RequestMapping("/api/logs")
public class LogReceiverController {
    
    // 存储接收到的日志（仅用于测试，生产环境不建议这样做）
    private final List<Map<String, Object>> accessLogs = new ArrayList<>();
    private final List<Map<String, Object>> operationLogs = new ArrayList<>();
    
    /**
     * 接收访问日志
     */
    @PostMapping("/access")
    public Map<String, Object> receiveAccessLog(@RequestBody Map<String, Object> accessLog) {
        log.info("收到访问日志: {}", accessLog);
        accessLogs.add(accessLog);
        
        Map<String, Object> response = new ConcurrentHashMap<>();
        response.put("success", true);
        response.put("message", "访问日志接收成功");
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
    
    /**
     * 接收操作日志
     */
    @PostMapping("/operation")
    public Map<String, Object> receiveOperationLog(@RequestBody Map<String, Object> operationLog) {
        log.info("收到操作日志: {}", operationLog);
        operationLogs.add(operationLog);
        
        Map<String, Object> response = new ConcurrentHashMap<>();
        response.put("success", true);
        response.put("message", "操作日志接收成功");
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
    
    /**
     * 查看所有访问日志
     */
    @GetMapping("/access")
    public Map<String, Object> getAccessLogs() {
        Map<String, Object> response = new ConcurrentHashMap<>();
        response.put("success", true);
        response.put("count", accessLogs.size());
        response.put("logs", accessLogs);
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
    
    /**
     * 查看所有操作日志
     */
    @GetMapping("/operation")
    public Map<String, Object> getOperationLogs() {
        Map<String, Object> response = new ConcurrentHashMap<>();
        response.put("success", true);
        response.put("count", operationLogs.size());
        response.put("logs", operationLogs);
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
    
    /**
     * 清空所有日志
     */
    @DeleteMapping
    public Map<String, Object> clearLogs() {
        accessLogs.clear();
        operationLogs.clear();
        
        Map<String, Object> response = new ConcurrentHashMap<>();
        response.put("success", true);
        response.put("message", "所有日志已清空");
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
    
    /**
     * 获取日志统计信息
     */
    @GetMapping("/stats")
    public Map<String, Object> getLogStats() {
        Map<String, Object> response = new ConcurrentHashMap<>();
        response.put("success", true);
        response.put("accessLogCount", accessLogs.size());
        response.put("operationLogCount", operationLogs.size());
        response.put("totalLogCount", accessLogs.size() + operationLogs.size());
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
}
