package com.diit.example.controller;

import com.diit.example.service.DirectKafkaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 简单Kafka测试控制器
 * 直接测试Kafka发送功能
 * 
 * @author diit
 */
@Slf4j
@RestController
@RequestMapping("/api/simple-kafka")
public class SimpleKafkaTestController {
    
    @Autowired
    private DirectKafkaService directKafkaService;
    
    /**
     * 测试Kafka发送
     */
    @PostMapping("/send")
    public Map<String, Object> testKafkaSend(@RequestParam String message) {
        log.info("开始测试直接Kafka发送，消息：{}", message);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 使用直接Kafka服务发送消息
            directKafkaService.sendCustomLog(message);
            
            result.put("success", true);
            result.put("message", "自定义日志发送成功");
            result.put("service", "DirectKafkaService");
            
        } catch (Exception e) {
            log.error("自定义日志发送失败", e);
            result.put("success", false);
            result.put("message", "自定义日志发送失败：" + e.getMessage());
        }
        
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
}
