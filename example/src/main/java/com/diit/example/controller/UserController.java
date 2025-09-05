package com.diit.example.controller;

import com.diit.common.log.annotation.OperationLog;
import com.diit.common.log.annotation.UserAccessLog;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器示例
 * 演示日志注解的使用
 * 
 * @author zzx
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    /**
     * 用户登录 - 访问日志
     */
    @PostMapping("/login")
    @UserAccessLog(
        type = "登录", 
        description = "用户登录系统",
        module = "用户认证",
        target = "用户登录"
    )
    public Map<String, Object> login(@RequestBody Map<String, String> loginRequest) {
        // 模拟登录逻辑
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "登录成功");
        response.put("token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...");
        
        return response;
    }
    
    /**
     * 用户登出 - 访问日志
     */
    @PostMapping("/logout")
    @UserAccessLog(
        type = "登出", 
        description = "用户登出系统",
        module = "用户认证",
        target = "用户登出"
    )
    public Map<String, Object> logout() {
        // 模拟登出逻辑
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "登出成功");
        
        return response;
    }
    
    /**
     * 创建用户 - 操作日志
     */
    @PostMapping
    @OperationLog(
        type = "新增", 
        description = "创建新用户",
        module = "用户管理",
        target = "用户信息",
        recordDataChange = true
    )
    public Map<String, Object> createUser(@RequestBody Map<String, String> userRequest) {
        // 模拟创建用户逻辑
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "用户创建成功");
        response.put("userId", "12345");
        
        return response;
    }
    
    /**
     * 更新用户 - 操作日志
     */
    @PutMapping("/{id}")
    @OperationLog(
        type = "编辑", 
        description = "更新用户信息",
        module = "用户管理",
        target = "用户信息",
        recordDataChange = true
    )
    public Map<String, Object> updateUser(@PathVariable String id, @RequestBody Map<String, String> userRequest) {
        // 模拟更新用户逻辑
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "用户更新成功");
        response.put("userId", id);
        
        return response;
    }
    
    /**
     * 删除用户 - 操作日志
     */
    @DeleteMapping("/{id}")
    @OperationLog(
        type = "删除", 
        description = "删除用户",
        module = "用户管理",
        target = "用户信息"
    )
    public Map<String, Object> deleteUser(@PathVariable String id) {
        // 模拟删除用户逻辑
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "用户删除成功");
        response.put("userId", id);
        
        return response;
    }
    
    /**
     * 查询用户 - 操作日志
     */
    @GetMapping("/{id}")
    @OperationLog(
        type = "查询", 
        description = "查询用户信息",
        module = "用户管理",
        target = "用户信息"
    )
    public Map<String, Object> getUser(@PathVariable String id) {
        // 模拟查询用户逻辑
        Map<String, Object> user = new HashMap<>();
        user.put("id", id);
        user.put("username", "example_user");
        user.put("email", "user@example.com");
        user.put("nickname", "示例用户");
        
        return user;
    }
}
