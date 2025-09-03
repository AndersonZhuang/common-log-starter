package com.diit.example.controller;

import com.diit.common.log.annotation.GenericLog;
import com.diit.example.entity.BusinessLogEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @GenericLog 注解测试控制器
 * 用于测试通用日志记录功能的各种场景
 * 
 * @author diit
 */
@Slf4j
@RestController
@RequestMapping("/api/generic")
public class GenericLogTestController {
    
    private final Random random = new Random();
    
    /**
     * 测试1：基础@GenericLog
     */
    @GenericLog("基础通用日志测试")
    @GetMapping("/basic")
    public Map<String, Object> testBasic() {
        log.info("执行基础通用日志测试");
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "基础通用日志测试成功");
        result.put("timestamp", System.currentTimeMillis());
        result.put("testType", "basic");
        
        return result;
    }
    
    /**
     * 测试2：带SpEL表达式的@GenericLog
     */
    @GenericLog("用户操作：#{#username} 执行了 #{#action}")
    @PostMapping("/spel")
    public Map<String, Object> testSpel(
            @RequestParam String username,
            @RequestParam String action) {
        log.info("执行SpEL表达式测试，用户：{}，操作：{}", username, action);
        
        Map<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("action", action);
        result.put("message", "SpEL表达式测试成功");
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
    
    /**
     * 测试3：使用自定义实体类
     */
    @GenericLog(
        value = "业务操作：#{#businessType}",
        entityClass = BusinessLogEntity.class,
        module = "业务模块",
        target = "业务对象",
        operationType = "BUSINESS",
        logArgs = true,
        logResult = true
    )
    @PostMapping("/custom-entity")
    public Map<String, Object> testCustomEntity(
            @RequestParam String businessType,
            @RequestParam String department,
            @RequestParam String project) {
        log.info("执行自定义实体测试，业务类型：{}，部门：{}，项目：{}", businessType, department, project);
        
        Map<String, Object> result = new HashMap<>();
        result.put("businessType", businessType);
        result.put("department", department);
        result.put("project", project);
        result.put("message", "自定义实体测试成功");
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
    
    /**
     * 测试4：指定Kafka发送器
     */
    @GenericLog(
        value = "Kafka专用日志：#{#message}",
        senderType = "kafka",
        module = "Kafka模块",
        target = "Kafka测试"
    )
    @PostMapping("/kafka-sender")
    public Map<String, Object> testKafkaSender(@RequestParam String message) {
        log.info("执行Kafka发送器测试，消息：{}", message);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", message);
        result.put("sender", "kafka");
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
    
    /**
     * 测试5：指定数据库发送器
     */
    @GenericLog(
        value = "数据库专用日志：#{#message}",
        senderType = "database",
        module = "数据库模块",
        target = "数据库测试"
    )
    @PostMapping("/database-sender")
    public Map<String, Object> testDatabaseSender(@RequestParam String message) {
        log.info("执行数据库发送器测试，消息：{}", message);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", message);
        result.put("sender", "database");
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
    
    /**
     * 测试6：指定Elasticsearch发送器
     */
    @GenericLog(
        value = "Elasticsearch专用日志：#{#message}",
        senderType = "elasticsearch",
        module = "ES模块",
        target = "ES测试"
    )
    @PostMapping("/elasticsearch-sender")
    public Map<String, Object> testElasticsearchSender(@RequestParam String message) {
        log.info("执行Elasticsearch发送器测试，消息：{}", message);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", message);
        result.put("sender", "elasticsearch");
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
    
    /**
     * 测试7：异步发送
     */
    @GenericLog(
        value = "异步日志测试：#{#message}",
        async = true,
        module = "异步模块",
        target = "异步测试"
    )
    @PostMapping("/async")
    public Map<String, Object> testAsync(@RequestParam String message) {
        log.info("执行异步发送测试，消息：{}", message);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", message);
        result.put("async", true);
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
    
    /**
     * 测试8：同步发送
     */
    @GenericLog(
        value = "同步日志测试：#{#message}",
        async = false,
        module = "同步模块",
        target = "同步测试"
    )
    @PostMapping("/sync")
    public Map<String, Object> testSync(@RequestParam String message) {
        log.info("执行同步发送测试，消息：{}", message);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", message);
        result.put("async", false);
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
    
    /**
     * 测试9：高优先级
     */
    @GenericLog(
        value = "高优先级日志：#{#message}",
        priority = 1,
        module = "优先级模块",
        target = "高优先级测试"
    )
    @PostMapping("/high-priority")
    public Map<String, Object> testHighPriority(@RequestParam String message) {
        log.info("执行高优先级测试，消息：{}", message);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", message);
        result.put("priority", 1);
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
    
    /**
     * 测试10：低优先级
     */
    @GenericLog(
        value = "低优先级日志：#{#message}",
        priority = 9,
        module = "优先级模块",
        target = "低优先级测试"
    )
    @PostMapping("/low-priority")
    public Map<String, Object> testLowPriority(@RequestParam String message) {
        log.info("执行低优先级测试，消息：{}", message);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", message);
        result.put("priority", 9);
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
    
    /**
     * 测试11：异常处理
     */
    @GenericLog(
        value = "异常测试：#{#message}",
        logException = true,
        module = "异常模块",
        target = "异常测试"
    )
    @PostMapping("/exception")
    public Map<String, Object> testException(@RequestParam String message) {
        log.info("执行异常处理测试，消息：{}", message);
        
        if ("error".equals(message)) {
            throw new RuntimeException("这是一个测试异常，用于验证异常日志记录功能");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", message);
        result.put("status", "success");
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
    
    /**
     * 测试12：复杂SpEL表达式
     */
    @GenericLog("复杂操作：用户 #{#user.username} 在 #{#user.department} 部门执行了 #{#action} 操作，结果：#{#result}")
    @PostMapping("/complex-spel")
    public Map<String, Object> testComplexSpel(@RequestBody Map<String, Object> request) {
        log.info("执行复杂SpEL表达式测试，请求：{}", request);
        
        Map<String, Object> result = new HashMap<>();
        result.put("processed", true);
        result.put("timestamp", System.currentTimeMillis());
        result.put("requestData", request);
        
        return result;
    }
    
    /**
     * 测试13：批量操作
     */
    @GenericLog(
        value = "批量操作：处理 #{#count} 条记录",
        module = "批量模块",
        target = "批量测试",
        logArgs = true,
        logResult = true
    )
    @PostMapping("/batch")
    public Map<String, Object> testBatch(@RequestParam int count) {
        log.info("执行批量操作测试，数量：{}", count);
        
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("processed", count);
        result.put("message", "批量操作完成");
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
    
    /**
     * 测试14：性能测试
     */
    @GenericLog(
        value = "性能测试：处理时间 #{#processingTime}ms",
        module = "性能模块",
        target = "性能测试"
    )
    @PostMapping("/performance")
    public Map<String, Object> testPerformance() throws InterruptedException {
        log.info("执行性能测试");
        
        long startTime = System.currentTimeMillis();
        
        // 模拟处理时间
        Thread.sleep(random.nextInt(1000) + 100); // 100-1100ms
        
        long endTime = System.currentTimeMillis();
        long processingTime = endTime - startTime;
        
        Map<String, Object> result = new HashMap<>();
        result.put("processingTime", processingTime);
        result.put("message", "性能测试完成");
        result.put("timestamp", endTime);
        
        return result;
    }
    
    /**
     * 测试15：综合测试
     */
    @GenericLog(
        value = "综合测试：#{#testType} - #{#description}",
        entityClass = BusinessLogEntity.class,
        module = "综合模块",
        target = "综合测试",
        operationType = "COMPREHENSIVE",
        logArgs = true,
        logResult = true,
        async = true,
        priority = 5
    )
    @PostMapping("/comprehensive")
    public Map<String, Object> testComprehensive(
            @RequestParam String testType,
            @RequestParam String description,
            @RequestParam(required = false) String businessType) {
        log.info("执行综合测试，类型：{}，描述：{}，业务类型：{}", testType, description, businessType);
        
        Map<String, Object> result = new HashMap<>();
        result.put("testType", testType);
        result.put("description", description);
        result.put("businessType", businessType);
        result.put("message", "综合测试完成");
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
}
