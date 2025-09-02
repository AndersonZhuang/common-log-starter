package com.diit.example.controller;

import com.diit.common.log.annotation.GenericLog;
import com.diit.example.entity.BusinessLogEntity;
import com.diit.example.entity.OrderLogEntity;
import com.diit.example.entity.UserActivityLogEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
     * 测试订单日志实体
     */
    @GenericLog(
        value = "订单操作：#{#operation} - 订单号：#{#orderNumber}",
        entityClass = OrderLogEntity.class,
        module = "订单管理",
        target = "订单信息",
        operationType = "ORDER",
        logArgs = true,
        logResult = true
    )
    @PostMapping("/order")
    public Map<String, Object> testOrderLog(
            @RequestParam String operation,
            @RequestParam String orderNumber,
            @RequestParam String orderStatus,
            @RequestParam String userId,
            @RequestParam String totalAmount) {
        
        log.info("执行订单操作测试，操作：{}，订单号：{}", operation, orderNumber);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", operation);
        result.put("orderNumber", orderNumber);
        result.put("orderStatus", orderStatus);
        result.put("userId", userId);
        result.put("totalAmount", totalAmount);
        result.put("message", "订单操作测试成功");
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
    
    /**
     * 测试用户活动日志实体
     */
    @GenericLog(
        value = "用户活动：#{#activityType} - 用户：#{#userId}",
        entityClass = UserActivityLogEntity.class,
        module = "用户行为",
        target = "用户活动",
        operationType = "USER_ACTIVITY",
        logArgs = true,
        logResult = true
    )
    @PostMapping("/user-activity")
    public Map<String, Object> testUserActivityLog(
            @RequestParam String activityType,
            @RequestParam String userId,
            @RequestParam String pageUrl,
            @RequestParam String deviceType) {
        
        log.info("执行用户活动测试，活动类型：{}，用户：{}", activityType, userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("activityType", activityType);
        result.put("userId", userId);
        result.put("pageUrl", pageUrl);
        result.put("deviceType", deviceType);
        result.put("message", "用户活动测试成功");
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
    
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
     * 测试复杂订单场景
     */
    @GenericLog(
        value = "复杂订单操作：#{#operation} - 订单：#{#orderNumber}，金额：#{#totalAmount}",
        entityClass = OrderLogEntity.class,
        module = "订单管理",
        target = "复杂订单",
        operationType = "COMPLEX_ORDER",
        logArgs = true,
        logResult = true,
        async = true
    )
    @PostMapping("/complex-order")
    public Map<String, Object> testComplexOrderLog(
            @RequestParam String operation,
            @RequestParam String orderNumber,
            @RequestParam String orderStatus,
            @RequestParam String userId,
            @RequestParam String totalAmount,
            @RequestParam String paymentMethod,
            @RequestParam String deliveryAddress,
            @RequestParam String productCategory) {
        
        log.info("执行复杂订单测试，操作：{}，订单：{}，金额：{}", operation, orderNumber, totalAmount);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", operation);
        result.put("orderNumber", orderNumber);
        result.put("orderStatus", orderStatus);
        result.put("userId", userId);
        result.put("totalAmount", totalAmount);
        result.put("paymentMethod", paymentMethod);
        result.put("deliveryAddress", deliveryAddress);
        result.put("productCategory", productCategory);
        result.put("message", "复杂订单测试成功");
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
    
    /**
     * 测试用户行为追踪
     */
    @GenericLog(
        value = "用户行为：#{#activityType} - 页面：#{#pageUrl}，设备：#{#deviceType}",
        entityClass = UserActivityLogEntity.class,
        module = "用户行为分析",
        target = "行为追踪",
        operationType = "BEHAVIOR_TRACKING",
        logArgs = true,
        logResult = true,
        async = true
    )
    @PostMapping("/behavior-tracking")
    public Map<String, Object> testBehaviorTracking(
            @RequestParam String activityType,
            @RequestParam String userId,
            @RequestParam String pageUrl,
            @RequestParam String deviceType,
            @RequestParam String browserType,
            @RequestParam String country,
            @RequestParam String city,
            @RequestParam String searchKeyword) {
        
        log.info("执行用户行为追踪测试，活动：{}，用户：{}，页面：{}", activityType, userId, pageUrl);
        
        Map<String, Object> result = new HashMap<>();
        result.put("activityType", activityType);
        result.put("userId", userId);
        result.put("pageUrl", pageUrl);
        result.put("deviceType", deviceType);
        result.put("browserType", browserType);
        result.put("country", country);
        result.put("city", city);
        result.put("searchKeyword", searchKeyword);
        result.put("message", "用户行为追踪测试成功");
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
    
    /**
     * 测试指定发送器的自定义实体
     */
    @GenericLog(
        value = "Kafka订单日志：#{#operation} - #{#orderNumber}",
        entityClass = OrderLogEntity.class,
        senderType = "kafka",
        module = "Kafka订单",
        target = "订单日志",
        operationType = "KAFKA_ORDER"
    )
    @PostMapping("/kafka-order")
    public Map<String, Object> testKafkaOrderLog(
            @RequestParam String operation,
            @RequestParam String orderNumber,
            @RequestParam String userId) {
        
        log.info("执行Kafka订单日志测试，操作：{}，订单：{}", operation, orderNumber);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", operation);
        result.put("orderNumber", orderNumber);
        result.put("userId", userId);
        result.put("sender", "kafka");
        result.put("message", "Kafka订单日志测试成功");
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
}
