package com.diit.example.controller;

import com.diit.common.log.annotation.OperationLog;
import com.diit.common.log.annotation.UserAccessLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 测试控制器
 * 用于测试各种日志记录场景
 * 
 * @author diit
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
public class TestController {
    
    private final Random random = new Random();
    
    /**
     * 测试正常访问日志
     */
    @GetMapping("/normal")
    @UserAccessLog(
        type = "测试", 
        description = "正常访问测试",
        module = "测试模块",
        target = "正常访问"
    )
    public Map<String, Object> testNormalAccess() {
        log.info("执行正常访问测试");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "正常访问测试成功");
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
    
    /**
     * 测试带参数的访问日志
     */
    @PostMapping("/with-params")
    @UserAccessLog(
        type = "测试", 
        description = "带参数访问测试",
        module = "测试模块",
        target = "参数测试"
    )
    public Map<String, Object> testAccessWithParams(
            @RequestBody(required = false) Map<String, Object> params,
            @RequestParam Map<String, String> queryParams) {
        
        // 合并请求体参数和查询参数
        Map<String, Object> allParams = new HashMap<>();
        if (params != null) {
            allParams.putAll(params);
        }
        allParams.putAll(queryParams);
        
        log.info("执行带参数访问测试，参数: {}", allParams);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "带参数访问测试成功");
        response.put("receivedParams", allParams);
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
    
    /**
     * 测试操作日志
     */
    @PostMapping("/operation")
    @OperationLog(
        type = "测试", 
        description = "操作日志测试",
        module = "测试模块",
        target = "操作测试",
        recordDataChange = true
    )
    public Map<String, Object> testOperationLog(
            @RequestBody(required = false) Map<String, Object> data,
            @RequestParam Map<String, String> queryParams) {
        
        // 合并请求体参数和查询参数
        Map<String, Object> allData = new HashMap<>();
        if (data != null) {
            allData.putAll(data);
        }
        allData.putAll(queryParams);
        
        log.info("执行操作日志测试，数据: {}", allData);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "操作日志测试成功");
        response.put("processedData", allData);
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
    
    /**
     * 测试异常情况
     */
    @GetMapping("/exception")
    @UserAccessLog(
        type = "测试", 
        description = "异常测试",
        module = "测试模块",
        target = "异常测试"
    )
    public Map<String, Object> testException() {
        log.info("执行异常测试");
        
        // 随机抛出异常
        if (random.nextBoolean()) {
            throw new RuntimeException("这是一个测试异常");
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "异常测试成功（没有异常）");
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
    
    /**
     * 测试批量操作
     */
    @PostMapping("/batch")
    @OperationLog(
        type = "批量", 
        description = "批量操作测试",
        module = "测试模块",
        target = "批量测试",
        recordDataChange = true
    )
    public Map<String, Object> testBatchOperation(
            @RequestBody(required = false) Map<String, Object> batchData,
            @RequestParam Map<String, String> queryParams) {
        
        // 合并请求体参数和查询参数
        Map<String, Object> allData = new HashMap<>();
        if (batchData != null) {
            allData.putAll(batchData);
        }
        allData.putAll(queryParams);
        
        log.info("执行批量操作测试，数据: {}", allData);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "批量操作测试成功");
        response.put("processedData", allData);
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
    
    /**
     * 测试敏感信息过滤
     */
    @PostMapping("/sensitive")
    @UserAccessLog(
        type = "测试", 
        description = "敏感信息测试",
        module = "测试模块",
        target = "敏感信息"
    )
    public Map<String, Object> testSensitiveInfo(@RequestBody Map<String, Object> sensitiveData) {
        log.info("执行敏感信息测试");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "敏感信息测试成功");
        response.put("note", "密码和token等敏感信息不会被记录");
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
    
    /**
     * 测试性能监控
     */
    @GetMapping("/performance")
    @UserAccessLog(
        type = "测试", 
        description = "性能测试",
        module = "测试模块",
        target = "性能监控"
    )
    public Map<String, Object> testPerformance() throws InterruptedException {
        log.info("执行性能测试");
        
        // 模拟处理时间
        long startTime = System.currentTimeMillis();
        Thread.sleep(random.nextInt(1000) + 100); // 100-1100ms
        long endTime = System.currentTimeMillis();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "性能测试完成");
        response.put("processingTime", endTime - startTime);
        response.put("timestamp", endTime);
        
        return response;
    }
    
    /**
     * 通用测试接口 - 支持多种Content-Type
     */
    @PostMapping("/universal")
    @UserAccessLog(
        type = "测试", 
        description = "通用测试接口",
        module = "测试模块",
        target = "通用测试"
    )
    public Map<String, Object> testUniversal(
            @RequestBody(required = false) Map<String, Object> bodyParams,
            @RequestParam Map<String, String> queryParams,
            @RequestHeader Map<String, String> headers) {
        
        log.info("执行通用测试，请求头: {}", headers);
        
        // 合并所有参数
        Map<String, Object> allParams = new HashMap<>();
        if (bodyParams != null) {
            allParams.put("bodyParams", bodyParams);
        }
        allParams.put("queryParams", queryParams);
        allParams.put("headers", headers);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "通用测试成功");
        response.put("receivedData", allParams);
        response.put("contentType", headers.get("content-type"));
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
    
    /**
     * GET方式测试接口 - 只支持查询参数
     */
    @GetMapping("/get-test")
    @UserAccessLog(
        type = "测试", 
        description = "GET方式测试",
        module = "测试模块",
        target = "GET测试"
    )
    public Map<String, Object> testGetMethod(@RequestParam Map<String, String> queryParams) {
        log.info("执行GET方式测试，查询参数: {}", queryParams);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "GET方式测试成功");
        response.put("queryParams", queryParams);
        response.put("method", "GET");
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
}
