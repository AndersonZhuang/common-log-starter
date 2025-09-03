package com.diit.example.controller;

import com.diit.common.log.annotation.OperationLog;
import com.diit.common.log.annotation.UserAccessLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 简单测试控制器 - 用于测试日志功能
 * 
 * @author diit
 */
@Slf4j
@RestController
@RequestMapping("/api/simple")
public class SimpleTestController {

    /**
     * 简单测试接口 - 无参数
     */
    @GetMapping("/test1")
    @OperationLog(
        type = "测试",
        description = "简单测试接口1",
        module = "测试模块",
        target = "接口测试"
    )
    public Map<String, Object> test1() {
        log.info("执行简单测试接口1");
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "简单测试接口1执行成功");
        result.put("timestamp", System.currentTimeMillis());
        result.put("data", "这是测试数据1");
        
        return result;
    }

    /**
     * 简单测试接口 - 带一个参数
     */
    @GetMapping("/test2")
    @OperationLog(
        type = "测试",
        description = "简单测试接口2",
        module = "测试模块",
        target = "参数测试"
    )
    public Map<String, Object> test2(@RequestParam(defaultValue = "默认值") String name) {
        log.info("执行简单测试接口2，参数: {}", name);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "简单测试接口2执行成功");
        result.put("timestamp", System.currentTimeMillis());
        result.put("receivedName", name);
        result.put("data", "这是测试数据2");
        
        return result;
    }

    /**
     * 简单测试接口 - 返回当前时间
     */
    @GetMapping("/time")
    @OperationLog(
        type = "查询",
        description = "获取当前时间",
        module = "时间模块",
        target = "时间查询"
    )
    public Map<String, Object> getCurrentTime() {
        log.info("执行时间查询接口");
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "时间查询成功");
        result.put("timestamp", System.currentTimeMillis());
        result.put("currentTime", java.time.LocalDateTime.now().toString());
        result.put("data", "这是时间数据");
        
        return result;
    }

    /**
     * 简单测试接口 - 返回系统信息
     */
    @GetMapping("/system")
    @OperationLog(
        type = "查询",
        description = "获取系统信息",
        module = "系统模块",
        target = "系统信息"
    )
    public Map<String, Object> getSystemInfo() {
        log.info("执行系统信息查询接口");
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "系统信息查询成功");
        result.put("timestamp", System.currentTimeMillis());
        result.put("javaVersion", System.getProperty("java.version"));
        result.put("osName", System.getProperty("os.name"));
        result.put("userHome", System.getProperty("user.home"));
        result.put("data", "这是系统信息数据");
        
        return result;
    }

    /**
     * 简单测试接口 - 模拟计算
     */
    @GetMapping("/calculate")
    @OperationLog(
        type = "计算",
        description = "简单数学计算",
        module = "计算模块",
        target = "数学运算"
    )
    public Map<String, Object> calculate(@RequestParam(defaultValue = "10") int a, 
                                       @RequestParam(defaultValue = "5") int b) {
        log.info("执行计算接口，参数: a={}, b={}", a, b);
        
        int sum = a + b;
        int diff = a - b;
        int product = a * b;
        double quotient = (double) a / b;
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "计算成功");
        result.put("timestamp", System.currentTimeMillis());
        result.put("input", Map.of("a", a, "b", b));
        result.put("results", Map.of(
            "sum", sum,
            "difference", diff,
            "product", product,
            "quotient", quotient
        ));
        result.put("data", "这是计算结果数据");
        
        return result;
    }

    /**
     * 访问日志测试接口 - 模拟用户登录
     */
    @PostMapping("/login")
    @UserAccessLog(
        type = "登录",
        description = "用户登录系统",
        module = "认证模块",
        target = "用户登录",
        recordParams = true,
        recordResponse = false
    )
    public Map<String, Object> login(@RequestBody Map<String, Object> loginInfo) {
        log.info("用户登录接口被调用");
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "登录成功");
        result.put("timestamp", System.currentTimeMillis());
        result.put("token", "mock-jwt-token-12345");
        result.put("userId", "user-001");
        
        return result;
    }
    
    /**
     * 访问日志测试接口 - 模拟用户注销
     */
    @PostMapping("/logout")
    @UserAccessLog(
        type = "注销",
        description = "用户注销系统",
        module = "认证模块",
        target = "用户注销"
    )
    public Map<String, Object> logout() {
        log.info("用户注销接口被调用");
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "注销成功");
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("timestamp", System.currentTimeMillis());
        result.put("message", "服务运行正常");
        return result;
    }
}
