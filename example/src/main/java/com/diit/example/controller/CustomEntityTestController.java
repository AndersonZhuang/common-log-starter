package com.diit.example.controller;

import com.diit.common.log.annotation.GenericLog;
import com.diit.example.entity.BusinessLogEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 自定义实体测试控制器
 * 演示如何在具体项目中使用自定义日志实体
 * 
 * @author diit
 */
@Slf4j
@RestController
@RequestMapping("/api/custom-entity")
public class CustomEntityTestController {
    
    /**
     * 测试业务日志实体
     */
    @GenericLog(
        value = "业务操作：#{#businessType} - 部门：#{#department}",
        entityClass = BusinessLogEntity.class,
        module = "业务管理",
        target = "业务操作",
        operationType = "BUSINESS",
        logArgs = true,
        logResult = true
    )
    @PostMapping("/business")
    public Map<String, Object> testBusinessLog(
            @RequestParam String businessType,
            @RequestParam String department,
            @RequestParam String project,
            @RequestParam String customField1,
            @RequestParam String customField2) {
        
        log.info("执行业务操作测试，业务类型：{}，部门：{}", businessType, department);
        
        Map<String, Object> result = new HashMap<>();
        result.put("businessType", businessType);
        result.put("department", department);
        result.put("project", project);
        result.put("customField1", customField1);
        result.put("customField2", customField2);
        result.put("message", "业务操作测试成功");
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
    
    /**
     * 测试异步日志记录
     */
    @GenericLog(
        value = "异步业务操作：#{#businessType} - 项目：#{#project}",
        entityClass = BusinessLogEntity.class,
        module = "业务管理",
        target = "异步业务操作",
        operationType = "ASYNC_BUSINESS",
        logArgs = true,
        logResult = true,
        async = true
    )
    @PostMapping("/async-business")
    public Map<String, Object> testAsyncBusinessLog(
            @RequestParam String businessType,
            @RequestParam String department,
            @RequestParam String project,
            @RequestParam String customField1,
            @RequestParam String customField2) {
        
        log.info("执行异步业务操作测试，业务类型：{}，项目：{}", businessType, project);
        
        Map<String, Object> result = new HashMap<>();
        result.put("businessType", businessType);
        result.put("department", department);
        result.put("project", project);
        result.put("customField1", customField1);
        result.put("customField2", customField2);
        result.put("async", true);
        result.put("message", "异步业务操作测试成功");
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
    
    /**
     * 测试指定Kafka发送器
     */
    @GenericLog(
        value = "Kafka业务日志：#{#businessType} - #{#department}",
        entityClass = BusinessLogEntity.class,
        senderType = "kafka",
        module = "Kafka业务",
        target = "业务日志",
        operationType = "KAFKA_BUSINESS"
    )
    @PostMapping("/kafka-business")
    public Map<String, Object> testKafkaBusinessLog(
            @RequestParam String businessType,
            @RequestParam String department,
            @RequestParam String project) {
        
        log.info("执行Kafka业务日志测试，业务类型：{}，部门：{}", businessType, department);
        
        Map<String, Object> result = new HashMap<>();
        result.put("businessType", businessType);
        result.put("department", department);
        result.put("project", project);
        result.put("sender", "kafka");
        result.put("message", "Kafka业务日志测试成功");
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
}