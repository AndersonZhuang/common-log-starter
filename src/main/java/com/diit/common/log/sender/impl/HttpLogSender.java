package com.diit.common.log.sender.impl;

import com.diit.common.log.entity.UserAccessLogEntity;
import com.diit.common.log.entity.OperationLogEntity;
import com.diit.common.log.properties.LogProperties;
import com.diit.common.log.sender.LogSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

/**
 * HTTP日志发送器实现
 * 
 * @author diit
 */
@Slf4j
@Component
public class HttpLogSender implements LogSender {
    
    @Autowired
    private LogProperties logProperties;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Override
    public void sendAccessLog(UserAccessLogEntity accessLog) {
        try {
            String message = objectMapper.writeValueAsString(accessLog);
            String endpoint = logProperties.getHttp().getAccessLogEndpoint();
            
            CompletableFuture.runAsync(() -> {
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    
                    HttpEntity<String> request = new HttpEntity<>(message, headers);
                    ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);
                    
                    if (response.getStatusCode().is2xxSuccessful()) {
                        log.debug("成功发送访问日志到HTTP端点: {}", endpoint);
                    } else {
                        log.warn("发送访问日志到HTTP端点失败 - Status:{}, Endpoint:{}", 
                                response.getStatusCode(), endpoint);
                    }
                } catch (Exception e) {
                    log.error("发送访问日志到HTTP端点异常: {}, Endpoint:{}", e.getMessage(), endpoint);
                }
            });
        } catch (Exception e) {
            log.error("序列化访问日志失败: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void sendOperationLog(OperationLogEntity operationLog) {
        try {
            String message = objectMapper.writeValueAsString(operationLog);
            String endpoint = logProperties.getHttp().getOperationLogEndpoint();
            
            CompletableFuture.runAsync(() -> {
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    
                    HttpEntity<String> request = new HttpEntity<>(message, headers);
                    ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);
                    
                    if (response.getStatusCode().is2xxSuccessful()) {
                        log.debug("成功发送操作日志到HTTP端点: {}", endpoint);
                    } else {
                        log.warn("发送操作日志到HTTP端点失败 - Status:{}, Endpoint:{}", 
                                response.getStatusCode(), endpoint);
                    }
                } catch (Exception e) {
                    log.error("发送操作日志到HTTP端点异常: {}, Endpoint:{}", e.getMessage(), endpoint);
                }
            });
        } catch (Exception e) {
            log.error("序列化操作日志失败: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public boolean supports(String logType) {
        return "http".equals(logType) && logProperties.getHttp().isEnabled();
    }
    
    @Override
    public String getName() {
        return "HttpLogSender";
    }
}
