package com.diit.common.log.factory;

import com.diit.common.log.entity.BaseLogEntity;
import com.diit.common.log.utils.LogContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 日志实体工厂
 * 负责创建和填充日志实体的基础字段
 * 
 * @author diit
 */
@Slf4j
@Component
public class LogEntityFactory {
    
    /**
     * 创建并填充日志实体
     * 
     * @param entityClass 实体类型
     * @param description 操作描述
     * @param <T> 实体类型
     * @return 填充了基础字段的日志实体
     */
    public <T extends BaseLogEntity> T createLogEntity(Class<T> entityClass, String description) {
        try {
            // 使用无参构造器创建实例
            Constructor<T> constructor = entityClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            T logEntity = constructor.newInstance();
            
            // 填充基础字段
            fillBaseFields(logEntity, description);
            
            return logEntity;
        } catch (Exception e) {
            log.error("创建日志实体失败: entityClass={}, description={}", 
                     entityClass.getName(), description, e);
            throw new RuntimeException("Failed to create log entity", e);
        }
    }
    
    /**
     * 填充BaseLogEntity的基础字段
     * 
     * @param logEntity 日志实体
     * @param description 操作描述
     */
    private void fillBaseFields(BaseLogEntity logEntity, String description) {
        // 生成ID
        logEntity.setId(generateLogId());
        
        // 设置描述
        logEntity.setDescription(description);
        
        // 设置创建时间
        logEntity.setCreateTime(LocalDateTime.now());
        
        // 从上下文获取用户信息
        try {
            String username = LogContextUtils.getCurrentUsername();
            logEntity.setUsername(username);
        } catch (Exception e) {
            log.debug("获取用户名失败，将设置为匿名用户", e);
            logEntity.setUsername("anonymous");
        }
        
        // 从请求中获取IP地址
        try {
            HttpServletRequest request = LogContextUtils.getCurrentRequest();
            if (request != null) {
                String clientIp = LogContextUtils.getClientIpAddress(request);
                logEntity.setClientIp(clientIp);
            }
        } catch (Exception e) {
            log.debug("获取客户端IP失败", e);
            logEntity.setClientIp("unknown");
        }
        
        // 默认设置为成功状态，后续可能会被覆盖
        logEntity.setStatus("SUCCESS");
    }
    
    /**
     * 生成日志ID
     * 
     * @return 日志ID
     */
    private String generateLogId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 复制基础字段到目标实体
     * 用于在不同实体类型之间转换时保持基础字段
     * 
     * @param source 源实体
     * @param target 目标实体
     */
    public void copyBaseFields(BaseLogEntity source, BaseLogEntity target) {
        target.setId(source.getId());
        target.setUsername(source.getUsername());
        target.setDescription(source.getDescription());
        target.setClientIp(source.getClientIp());
        target.setStatus(source.getStatus());
        target.setCreateTime(source.getCreateTime());
    }
}