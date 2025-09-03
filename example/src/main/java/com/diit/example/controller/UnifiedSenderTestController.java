package com.diit.example.controller;

import com.diit.common.log.annotation.GenericLog;
import com.diit.example.entity.BusinessLogEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一发送器测试控制器
 * 测试新的统一sender是否能够正确处理自定义字段
 * 
 * @author diit
 */
@Slf4j
@RestController
@RequestMapping("/api/unified-sender")
public class UnifiedSenderTestController {
    
    /**
     * 测试统一Kafka发送器
     */
    @PostMapping("/test-kafka")
    @GenericLog(
        value = "测试统一Kafka发送器",
        entityClass = BusinessLogEntity.class,
        senderType = "kafka"
    )
    public Map<String, Object> testUnifiedKafkaSender(@RequestParam String message) {
        log.info("测试统一Kafka发送器，消息：{}", message);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "统一Kafka发送器测试成功");
        result.put("testMessage", message);
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
    
    /**
     * 测试统一数据库发送器
     */
    @PostMapping("/test-database")
    @GenericLog(
        value = "测试统一数据库发送器",
        entityClass = BusinessLogEntity.class,
        senderType = "database"
    )
    public Map<String, Object> testUnifiedDatabaseSender(@RequestParam String message) {
        log.info("测试统一数据库发送器，消息：{}", message);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "统一数据库发送器测试成功");
        result.put("testMessage", message);
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
    
    /**
     * 测试默认发送器
     */
    @PostMapping("/test-default")
    @GenericLog(
        value = "测试默认发送器",
        entityClass = BusinessLogEntity.class
    )
    public Map<String, Object> testDefaultSender(@RequestParam String message) {
        log.info("测试默认发送器，消息：{}", message);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "默认发送器测试成功");
        result.put("testMessage", message);
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
}
