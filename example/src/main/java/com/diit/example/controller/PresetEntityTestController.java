package com.diit.example.controller;

import com.diit.common.log.entity.BaseLogEntity;
import com.diit.common.log.entity.OperationLogEntity;
import com.diit.common.log.entity.UserAccessLogEntity;
import com.diit.common.log.sender.GenericLogSender;
import com.diit.common.log.sender.LogSenderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 预设实体类测试控制器
 * 用于测试OperationLogEntity和UserAccessLogEntity的自动建表功能
 * 支持多种存储方式：数据库、Kafka、Elasticsearch、HTTP
 * 
 * @author diit
 */
@Slf4j
@RestController
@RequestMapping("/api/preset-test")
public class PresetEntityTestController {
    
    @Autowired
    private LogSenderFactory logSenderFactory;
    
    /**
     * 测试操作日志实体
     * 支持多种存储方式：数据库、Kafka、Elasticsearch、HTTP
     */
    @GetMapping("/operation-log")
    public String testOperationLog() {
        try {
            // 获取当前配置的日志发送器
            GenericLogSender<? extends BaseLogEntity> sender = logSenderFactory.getGenericLogSender(null);
            String senderType = sender.getSenderType();
            
            // 创建操作日志实体
            OperationLogEntity operationLog = OperationLogEntity.builder()
                .id(UUID.randomUUID().toString())
                .username("testUser")
                .realName("测试用户")
                .email("test@example.com")
                .roleName("管理员")
                .operationType("新增")
                .description("测试操作日志功能")
                .operationTime("2024-01-15 10:30:00")
                .operationTimestamp(LocalDateTime.now())
                .clientIp("192.168.1.100")
                .ipLocation("北京市")
                .browser("Chrome")
                .operatingSystem("Windows 10")
                .deviceType("PC")
                .status("成功")
                .responseTime(150L)
                .requestUri("/api/test")
                .requestMethod("GET")
                .userAgent("Mozilla/5.0...")
                .sessionId("session123")
                .module("用户管理")
                .target("用户信息")
                .beforeData("{}")
                .afterData("{\"name\":\"test\"}")
                .exceptionMessage(null)
                .createTime(LocalDateTime.now())
                .build();
            
            // 发送日志 - 使用类型转换
            @SuppressWarnings("unchecked")
            GenericLogSender<BaseLogEntity> baseSender = (GenericLogSender<BaseLogEntity>) sender;
            baseSender.send(operationLog);
            
            String result = "✅ 操作日志测试成功 - 存储方式: " + senderType + ", ID: " + operationLog.getId();
            if ("database".equals(senderType)) {
                result += ", 表名: log_operation";
            } else if ("kafka".equals(senderType)) {
                result += ", Topic: log_operation_log_entity";
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("操作日志测试失败", e);
            return "❌ 操作日志测试失败: " + e.getMessage();
        }
    }
    
    /**
     * 测试用户访问日志实体
     * 支持多种存储方式：数据库、Kafka、Elasticsearch、HTTP
     */
    @GetMapping("/user-access-log")
    public String testUserAccessLog() {
        try {
            // 获取当前配置的日志发送器
            GenericLogSender<? extends BaseLogEntity> sender = logSenderFactory.getGenericLogSender(null);
            String senderType = sender.getSenderType();
            
            // 创建用户访问日志实体
            UserAccessLogEntity accessLog = UserAccessLogEntity.builder()
                .id(UUID.randomUUID().toString())
                .username("testUser")
                .realName("测试用户")
                .email("test@example.com")
                .accessType("登录")
                .description("测试用户访问日志功能")
                .module("认证模块")
                .target("用户登录")
                .accessTime("2024-01-15 10:30:00")
                .accessTimestamp(LocalDateTime.now())
                .clientIp("192.168.1.100")
                .ipLocation("北京市")
                .browser("Chrome")
                .operatingSystem("Windows 10")
                .deviceType("PC")
                .status("成功")
                .responseTime(200L)
                .requestUri("/api/login")
                .requestMethod("POST")
                .userAgent("Mozilla/5.0...")
                .sessionId("session456")
                .createTime(LocalDateTime.now())
                .exceptionMessage(null)
                .build();
            
            // 发送日志 - 使用类型转换
            @SuppressWarnings("unchecked")
            GenericLogSender<BaseLogEntity> baseSender = (GenericLogSender<BaseLogEntity>) sender;
            baseSender.send(accessLog);
            
            String result = "✅ 用户访问日志测试成功 - 存储方式: " + senderType + ", ID: " + accessLog.getId();
            if ("database".equals(senderType)) {
                result += ", 表名: log_user_access";
            } else if ("kafka".equals(senderType)) {
                result += ", Topic: log_user_access_log_entity";
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("用户访问日志测试失败", e);
            return "❌ 用户访问日志测试失败: " + e.getMessage();
        }
    }
    
    /**
     * 测试批量发送预设实体
     * 支持多种存储方式：数据库、Kafka、Elasticsearch、HTTP
     */
    @GetMapping("/batch-test")
    public String testBatchPresetEntities() {
        try {
            // 获取当前配置的日志发送器
            GenericLogSender<? extends BaseLogEntity> sender = logSenderFactory.getGenericLogSender(null);
            String senderType = sender.getSenderType();
            
            // 创建多个操作日志
            OperationLogEntity operationLog1 = OperationLogEntity.builder()
                .id(UUID.randomUUID().toString())
                .username("user1")
                .operationType("查询")
                .description("批量测试1")
                .operationTimestamp(LocalDateTime.now())
                .status("成功")
                .createTime(LocalDateTime.now())
                .build();
            
            OperationLogEntity operationLog2 = OperationLogEntity.builder()
                .id(UUID.randomUUID().toString())
                .username("user2")
                .operationType("更新")
                .description("批量测试2")
                .operationTimestamp(LocalDateTime.now())
                .status("成功")
                .createTime(LocalDateTime.now())
                .build();
            
            // 创建用户访问日志
            UserAccessLogEntity accessLog = UserAccessLogEntity.builder()
                .id(UUID.randomUUID().toString())
                .username("user3")
                .accessType("注销")
                .description("批量测试访问日志")
                .accessTimestamp(LocalDateTime.now())
                .status("成功")
                .createTime(LocalDateTime.now())
                .build();
            
            // 批量发送 - 使用类型转换
            @SuppressWarnings("unchecked")
            GenericLogSender<BaseLogEntity> baseSender = (GenericLogSender<BaseLogEntity>) sender;
            baseSender.send(operationLog1);
            baseSender.send(operationLog2);
            baseSender.send(accessLog);
            
            return "✅ 批量测试成功 - 存储方式: " + senderType + ", 发送了3条预设实体日志";
            
        } catch (Exception e) {
            log.error("批量测试失败", e);
            return "❌ 批量测试失败: " + e.getMessage();
        }
    }
}
