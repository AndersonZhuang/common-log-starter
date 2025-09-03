package com.diit.example.controller;

import com.diit.common.log.annotation.GenericLog;
import com.diit.common.log.annotation.OperationLog;
import com.diit.common.log.annotation.UserAccessLog;
import com.diit.example.entity.CustomLogEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * 日志测试控制器
 * 演示操作日志、访问日志和自定义日志的使用
 * 
 * @author diit
 */
@Slf4j
@RestController
@RequestMapping("/api/log-test")
public class LogTestController {
    
    /**
     * 测试操作日志
     */
    @OperationLog(
        type = "CREATE",
        description = "新增用户：#{#userName}",
        module = "用户管理",
        target = "用户信息",
        recordParams = true,
        recordResponse = true
    )
    @PostMapping("/operation-log")
    public Map<String, Object> testOperationLog(
            @RequestParam String userName,
            @RequestParam String email,
            @RequestParam String role) {
        
        log.info("开始创建用户：{}", userName);
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", UUID.randomUUID().toString());
        result.put("userName", userName);
        result.put("email", email);
        result.put("role", role);
        result.put("success", true);
        result.put("message", "用户创建成功");
        
        return result;
    }
    
    /**
     * 测试访问日志
     */
    @UserAccessLog(
        type = "LOGIN",
        description = "用户登录：#{#username}",
        module = "用户认证",
        target = "登录系统",
        recordParams = false,
        recordResponse = false
    )
    @PostMapping("/access-log")
    public Map<String, Object> testAccessLog(
            @RequestParam String username,
            @RequestParam String password) {
        
        log.info("用户登录：{}", username);
        
        Map<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("loginTime", System.currentTimeMillis());
        result.put("token", "jwt-token-" + UUID.randomUUID().toString());
        result.put("success", true);
        result.put("message", "登录成功");
        
        return result;
    }
    
    /**
     * 测试自定义日志实体
     */
    @GenericLog(
        value = "金融交易：#{#transactionType} - 金额：#{#amount}",
        entityClass = CustomLogEntity.class,
        module = "金融交易",
        target = "交易处理",
        operationType = "FINANCIAL_TRANSACTION",
        logArgs = true,
        logResult = true
    )
    @PostMapping("/custom-log")
    public Map<String, Object> testCustomLog(
            @RequestParam String transactionType,
            @RequestParam BigDecimal amount,
            @RequestParam String currency,
            @RequestParam String riskLevel) {
        
        log.info("执行金融交易：{}，金额：{} {}", transactionType, amount, currency);
        
        Map<String, Object> result = new HashMap<>();
        result.put("transactionId", UUID.randomUUID().toString());
        result.put("transactionType", transactionType);
        result.put("amount", amount);
        result.put("currency", currency);
        result.put("riskLevel", riskLevel);
        result.put("status", "SUCCESS");
        result.put("message", "交易处理成功");
        
        return result;
    }
}