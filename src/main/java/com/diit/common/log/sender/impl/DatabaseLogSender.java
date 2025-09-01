package com.diit.common.log.sender.impl;

import com.diit.common.log.entity.UserAccessLogEntity;
import com.diit.common.log.entity.OperationLogEntity;
import com.diit.common.log.properties.LogProperties;
import com.diit.common.log.sender.LogSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * 数据库日志发送器实现
 * 
 * @author diit
 */
@Slf4j
@Component
public class DatabaseLogSender implements LogSender {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private LogProperties logProperties;
    
    
    @Override
    public void sendAccessLog(UserAccessLogEntity accessLog) {
        try {
            String tableName = logProperties.getDatabase().getTablePrefix() + "access_log";
            String sql = "INSERT INTO " + tableName + " " +
                    "(id, username, real_name, email, access_type, " +
                    "description, access_time, access_timestamp, client_ip, ip_location, " +
                    "browser, operating_system, device_type, status, response_time, " +
                    "request_uri, request_method, user_agent, session_id, module_name, " +
                    "target, exception_message, create_time) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            jdbcTemplate.update(sql,
                    accessLog.getId(),
                    accessLog.getUsername(),
                    accessLog.getRealName(),
                    accessLog.getEmail(),
                    accessLog.getAccessType(),
                    accessLog.getDescription(),
                    accessLog.getAccessTime(),
                    Timestamp.valueOf(accessLog.getAccessTimestamp()),
                    accessLog.getClientIp(),
                    accessLog.getIpLocation(),
                    accessLog.getBrowser(),
                    accessLog.getOperatingSystem(),
                    accessLog.getDeviceType(),
                    accessLog.getStatus(),
                    accessLog.getResponseTime(),
                    accessLog.getRequestUri(),
                    accessLog.getRequestMethod(),
                    accessLog.getUserAgent(),
                    accessLog.getSessionId(),
                    accessLog.getModule(),
                    accessLog.getTarget(),
                    accessLog.getExceptionMessage(),
                    Timestamp.valueOf(accessLog.getCreateTime())
            );
            
            log.debug("成功保存访问日志到数据库表: {}", tableName);
        } catch (Exception e) {
            log.error("保存访问日志到数据库失败: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void sendOperationLog(OperationLogEntity operationLog) {
        try {
            String tableName = logProperties.getDatabase().getTablePrefix() + "operation_log";
            String sql = "INSERT INTO " + tableName + " " +
                    "(id, username, real_name, email, role_name, operation_type, " +
                    "description, operation_time, operation_timestamp, client_ip, ip_location, " +
                    "browser, operating_system, device_type, status, response_time, " +
                    "request_uri, request_method, user_agent, session_id, module_name, " +
                    "target, before_data, after_data, exception_message, create_time) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            jdbcTemplate.update(sql,
                    operationLog.getId(),
                    operationLog.getUsername(),
                    operationLog.getRealName(),
                    operationLog.getEmail(),
                    operationLog.getRoleName(),
                    operationLog.getOperationType(),
                    operationLog.getDescription(),
                    operationLog.getOperationTime(),
                    Timestamp.valueOf(operationLog.getOperationTimestamp()),
                    operationLog.getClientIp(),
                    operationLog.getIpLocation(),
                    operationLog.getBrowser(),
                    operationLog.getOperatingSystem(),
                    operationLog.getDeviceType(),
                    operationLog.getStatus(),
                    operationLog.getResponseTime(),
                    operationLog.getRequestUri(),
                    operationLog.getRequestMethod(),
                    operationLog.getUserAgent(),
                    operationLog.getSessionId(),
                    operationLog.getModule(),
                    operationLog.getTarget(),
                    operationLog.getBeforeData(),
                    operationLog.getAfterData(),
                    operationLog.getExceptionMessage(),
                    Timestamp.valueOf(operationLog.getCreateTime())
            );
            
            log.debug("成功保存操作日志到数据库表: {}", tableName);
        } catch (Exception e) {
            log.error("保存操作日志到数据库失败: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public boolean supports(String logType) {
        return "database".equals(logType) && logProperties.getDatabase().isEnabled();
    }
    
    @Override
    public String getName() {
        return "DatabaseLogSender";
    }
    
}