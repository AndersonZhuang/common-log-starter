package com.diit.common.log.aspect;

import com.diit.common.log.annotation.OperationLog;
import com.diit.common.log.entity.OperationLogEntity;

import com.diit.common.log.sender.LogSenderFactory;
import com.diit.common.log.utils.LogWebUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 操作日志切面
 * 
 * @author diit
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {
    
    @Autowired
    private LogSenderFactory logSenderFactory;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
    
    @Around("@annotation(operationLog)")
    public Object logOperation(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        
        // 初始化操作日志信息
        OperationLogEntity opLog = OperationLogEntity.builder()
                .id(UUID.randomUUID().toString())
                .operationTime(LocalDateTime.now().format(DATE_TIME_FORMATTER))
                .operationTimestamp(LocalDateTime.now())
                .operationType(operationLog.type())
                .description(operationLog.description())
                .module(operationLog.module())
                .target(operationLog.target())
                .createTime(LocalDateTime.now())
                .build();
        
        if (request != null) {
            // 获取IP地址
            String ipAddress = LogWebUtils.getClientIpAddress(request);
            opLog.setClientIp(ipAddress);
            
            // 获取IP来源
            String ipLocation = LogWebUtils.getIpLocation(ipAddress);
            opLog.setIpLocation(ipLocation);
            
            // 获取浏览器信息
            String userAgent = request.getHeader("User-Agent");
            opLog.setUserAgent(userAgent);
            
            String browser = LogWebUtils.getBrowserInfo(userAgent);
            opLog.setBrowser(browser);
            
            // 获取操作系统信息
            String operatingSystem = LogWebUtils.getOperatingSystem(userAgent);
            opLog.setOperatingSystem(operatingSystem);
            
            // 获取设备类型
            String deviceType = LogWebUtils.getDeviceType(userAgent);
            opLog.setDeviceType(deviceType);
            
            // 获取请求信息
            opLog.setRequestUri(request.getRequestURI());
            opLog.setRequestMethod(request.getMethod());
            opLog.setSessionId(request.getSession().getId());
            
            // 获取用户信息（从Token中解析）
            String username = LogWebUtils.getUsernameFromToken(request);
            if (username != null) {
                opLog.setUsername(username);
            }
        }
        
        // 记录操作前数据（如果需要）
        Object beforeData = null;
        if (operationLog.recordDataChange()) {
            try {
                beforeData = joinPoint.getArgs();
                if (beforeData != null) {
                    opLog.setBeforeData(objectMapper.writeValueAsString(beforeData));
                }
            } catch (Exception e) {
                log.warn("记录操作前数据失败: {}", e.getMessage());
            }
        }
        
        Object result;
        String status = "成功";
        
        try {
            result = joinPoint.proceed();
            status = "成功";
            
            // 记录操作后数据（如果需要）
            if (operationLog.recordDataChange() && result != null) {
                try {
                    opLog.setAfterData(objectMapper.writeValueAsString(result));
                } catch (Exception e) {
                    log.warn("记录操作后数据失败: {}", e.getMessage());
                }
            }
            
            return result;
        } catch (Exception e) {
            status = "失败";
            if (operationLog.recordStackTrace()) {
                opLog.setExceptionMessage(e.getMessage());
            }
            log.error("用户操作失败: {}", e.getMessage(), e);
            throw e;
        } finally {
            // 计算响应时间
            long responseTime = System.currentTimeMillis() - startTime;
            opLog.setStatus(status);
            opLog.setResponseTime(responseTime);
            
            log.info("用户操作日志: {}", opLog);
            
            // 异步发送日志
            try {
                logSenderFactory.getOperationLogSender().sendOperationLog(opLog);
            } catch (Exception e) {
                log.error("发送操作日志失败: {}", e.getMessage(), e);
            }
        }
    }
}
