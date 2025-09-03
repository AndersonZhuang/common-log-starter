package com.diit.example.controller;

import com.diit.common.log.annotation.GenericLog;
import com.diit.example.entity.BusinessLogEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP发送器测试控制器
 * 专门测试HTTP发送器功能
 * 
 * @author diit
 */
@Slf4j
@RestController
@RequestMapping("/api/http-sender")
public class HttpSenderTestController {
    
    /**
     * 测试HTTP发送器
     */
    @GenericLog(
        value = "HTTP发送器测试：#{#message}",
        entityClass = BusinessLogEntity.class,
        senderType = "http",
        module = "HTTP模块",
        target = "HTTP测试"
    )
    @PostMapping("/test")
    public Map<String, Object> testHttpSender(@RequestParam String message) {
        log.info("执行HTTP发送器测试，消息：{}", message);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", message);
        result.put("sender", "http");
        result.put("timestamp", System.currentTimeMillis());
        result.put("status", "success");
        
        return result;
    }
    
    /**
     * 测试HTTP发送器 - 自定义实体
     */
    @GenericLog(
        value = "HTTP业务日志：#{#businessType} - #{#description}",
        entityClass = BusinessLogEntity.class,
        senderType = "http",
        module = "HTTP业务模块",
        target = "HTTP业务测试",
        logArgs = true,
        logResult = true
    )
    @PostMapping("/business")
    public Map<String, Object> testHttpBusinessLog(
            @RequestParam String businessType,
            @RequestParam String description,
            @RequestParam String department,
            @RequestParam String project) {
        
        log.info("执行HTTP业务日志测试，业务类型：{}，描述：{}", businessType, description);
        
        Map<String, Object> result = new HashMap<>();
        result.put("businessType", businessType);
        result.put("description", description);
        result.put("department", department);
        result.put("project", project);
        result.put("sender", "http");
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
    
    /**
     * 测试HTTP发送器 - 异步发送
     */
    @GenericLog(
        value = "HTTP异步日志：#{#message}",
        entityClass = BusinessLogEntity.class,
        senderType = "http",
        module = "HTTP异步模块",
        target = "HTTP异步测试",
        async = true
    )
    @PostMapping("/async")
    public Map<String, Object> testHttpAsyncSender(@RequestParam String message) {
        log.info("执行HTTP异步发送器测试，消息：{}", message);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", message);
        result.put("sender", "http");
        result.put("async", true);
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
}
