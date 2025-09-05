package com.diit.example.controller;

import com.diit.common.log.annotation.GenericLog;
import com.diit.common.log.annotation.OperationLog;
import com.diit.common.log.annotation.UserAccessLog;
import com.diit.example.entity.BusinessLogEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 日志测试控制器
 * 提供三个测试接口：两个预设日志测试，一个自定义日志测试
 * 
 * @author zzx
 */
@Slf4j
@RestController
@RequestMapping("/api/log-test")
public class LogTestController {
    
    /**
     * 测试1: 用户访问日志（预设）
     */
    @PostMapping("/user-access")
    @UserAccessLog(
        type = "登录", 
        description = "用户登录系统",
        module = "用户认证",
        target = "用户登录"
    )
    public Map<String, Object> testUserAccess(@RequestBody Map<String, String> loginRequest) {
        log.info("执行用户访问日志测试，用户名：{}", loginRequest.get("username"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "用户访问日志测试成功");
        response.put("username", loginRequest.get("username"));
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
    
    /**
     * 测试2: 操作日志（预设）
     */
    @PostMapping("/operation")
    @OperationLog(
        type = "新增",
        description = "创建新用户",
        module = "用户管理",
        target = "用户信息"
    )
    public Map<String, Object> testOperation(@RequestBody Map<String, String> userRequest) {
        log.info("执行操作日志测试，用户名：{}", userRequest.get("username"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "操作日志测试成功");
        response.put("username", userRequest.get("username"));
        response.put("userId", "12345");
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
    
    /**
     * 测试3: 自定义业务日志（带额外字段）
     */
    @PostMapping("/business")
    @GenericLog(
        value = "业务日志测试：#{#businessType} - #{#description}",
        entityClass = BusinessLogEntity.class,
        module = "业务模块",
        target = "业务测试",
        logArgs = true,
        logResult = true
    )
    public Map<String, Object> testBusinessLog(
            @RequestParam String businessType,
            @RequestParam String description,
            @RequestParam String department,
            @RequestParam String project) {
        
        log.info("执行自定义业务日志测试，业务类型：{}，描述：{}", businessType, description);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "自定义业务日志测试成功");
        response.put("businessType", businessType);
        response.put("description", description);
        response.put("department", department);
        response.put("project", project);
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
}
