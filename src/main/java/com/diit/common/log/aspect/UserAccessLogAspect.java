package com.diit.common.log.aspect;

import com.diit.common.log.annotation.UserAccessLog;
import com.diit.common.log.entity.UserAccessLogEntity;
import com.diit.common.log.sender.LogSenderFactory;
import com.diit.common.log.utils.LogWebUtils;
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
 * 用户访问日志切面
 * 
 * @author zzx
 */
@Slf4j
@Aspect
@Component
public class UserAccessLogAspect {
    
    @Autowired
    private LogSenderFactory logSenderFactory;
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
    
    @Around("@annotation(userAccessLog)")
    public Object logUserAccess(ProceedingJoinPoint joinPoint, UserAccessLog userAccessLog) throws Throwable {
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        
        // 初始化访问日志信息
        UserAccessLogEntity accessLog = UserAccessLogEntity.builder()
                .id(UUID.randomUUID().toString())
                .accessTime(LocalDateTime.now().format(DATE_TIME_FORMATTER))
                .accessTimestamp(LocalDateTime.now())
                .accessType(userAccessLog.type())
                .description(userAccessLog.description())
                .module(userAccessLog.module())
                .target(userAccessLog.target())
                .createTime(LocalDateTime.now())
                .build();
        
        if (request != null) {
            // 获取IP地址
            String ipAddress = LogWebUtils.getClientIpAddress(request);
            accessLog.setClientIp(ipAddress);
            
            // 获取IP来源
            String ipLocation = LogWebUtils.getIpLocation(ipAddress);
            accessLog.setIpLocation(ipLocation);
            
            // 获取浏览器信息
            String userAgent = request.getHeader("User-Agent");
            accessLog.setUserAgent(userAgent);
            
            String browser = LogWebUtils.getBrowserInfo(userAgent);
            accessLog.setBrowser(browser);
            
            // 获取操作系统信息
            String operatingSystem = LogWebUtils.getOperatingSystem(userAgent);
            accessLog.setOperatingSystem(operatingSystem);
            
            // 获取设备类型
            String deviceType = LogWebUtils.getDeviceType(userAgent);
            accessLog.setDeviceType(deviceType);
            
            // 获取请求信息
            accessLog.setRequestUri(request.getRequestURI());
            accessLog.setRequestMethod(request.getMethod());
            accessLog.setSessionId(request.getSession().getId());
            
            // 获取用户信息（从Token中解析）
            String username = LogWebUtils.getUsernameFromToken(request);
            if (username != null) {
                accessLog.setUsername(username);
            }
        }
        
        Object result;
        String status = "成功";
        
        try {
            result = joinPoint.proceed();
            status = "成功";
            return result;
        } catch (Exception e) {
            status = "失败";
            if (userAccessLog.recordStackTrace()) {
                accessLog.setExceptionMessage(e.getMessage());
            }
            log.error("用户访问失败: {}", e.getMessage(), e);
            throw e;
        } finally {
            // 计算响应时间
            long responseTime = System.currentTimeMillis() - startTime;
            accessLog.setStatus(status);
            accessLog.setResponseTime(responseTime);
            
            log.info("用户访问日志: {}", accessLog);
            
            // 异步发送日志
            try {
                logSenderFactory.getAccessLogSender().sendAccessLog(accessLog);
            } catch (Exception e) {
                log.error("发送访问日志失败: {}", e.getMessage(), e);
            }
        }
    }
}
