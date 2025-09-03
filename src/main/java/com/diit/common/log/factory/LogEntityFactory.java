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
            
            // 尝试从当前请求中提取自定义字段
            extractCustomFieldsFromRequest(logEntity);
            
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
        
        // 设置时间戳
        logEntity.setTimestamp(LocalDateTime.now());
        
        // 设置内容
        logEntity.setContent(description);
        
        // 设置日志级别
        logEntity.setLevel(org.springframework.boot.logging.LogLevel.INFO);
        
        // 从上下文获取用户信息并设置到自定义字段
        try {
            String username = LogContextUtils.getCurrentUsername();
            setFieldIfExists(logEntity.getClass(), logEntity, "username", username);
        } catch (Exception e) {
            log.debug("获取用户名失败，将设置为匿名用户", e);
            setFieldIfExists(logEntity.getClass(), logEntity, "username", "anonymous");
        }
        
        // 从请求中获取IP地址并设置到自定义字段
        try {
            HttpServletRequest request = LogContextUtils.getCurrentRequest();
            if (request != null) {
                String clientIp = LogContextUtils.getClientIpAddress(request);
                setFieldIfExists(logEntity.getClass(), logEntity, "clientIp", clientIp);
            }
        } catch (Exception e) {
            log.debug("获取客户端IP失败", e);
            setFieldIfExists(logEntity.getClass(), logEntity, "clientIp", "unknown");
        }
        
        // 默认设置为成功状态，后续可能会被覆盖
        setFieldIfExists(logEntity.getClass(), logEntity, "status", "SUCCESS");
        
        // 设置创建时间到自定义字段
        setFieldIfExists(logEntity.getClass(), logEntity, "createTime", LocalDateTime.now());
        
        // 设置描述到自定义字段
        setFieldIfExists(logEntity.getClass(), logEntity, "description", description);
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
        // 复制基础字段
        target.setId(source.getId());
        target.setTimestamp(source.getTimestamp());
        target.setContent(source.getContent());
        target.setLevel(source.getLevel());
        
        // 复制扩展字段（如果存在）
        copyFieldIfExists(source, target, "username");
        copyFieldIfExists(source, target, "description");
        copyFieldIfExists(source, target, "clientIp");
        copyFieldIfExists(source, target, "status");
        copyFieldIfExists(source, target, "createTime");
    }
    
    /**
     * 如果字段存在则复制
     */
    private void copyFieldIfExists(BaseLogEntity source, BaseLogEntity target, String fieldName) {
        try {
            java.lang.reflect.Field sourceField = source.getClass().getDeclaredField(fieldName);
            java.lang.reflect.Field targetField = target.getClass().getDeclaredField(fieldName);
            
            sourceField.setAccessible(true);
            targetField.setAccessible(true);
            
            Object value = sourceField.get(source);
            if (value != null) {
                targetField.set(target, value);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // 字段不存在或无法访问，忽略
            log.debug("复制字段 {} 失败: {}", fieldName, e.getMessage());
        }
    }
    
    /**
     * 从当前请求中提取自定义字段
     */
    private void extractCustomFieldsFromRequest(BaseLogEntity logEntity) {
        try {
            HttpServletRequest request = LogContextUtils.getCurrentRequest();
            if (request == null) {
                return;
            }
            
            // 获取请求参数
            java.util.Map<String, String[]> parameterMap = request.getParameterMap();
            Class<?> entityClass = logEntity.getClass();
            
            // 遍历请求参数，尝试设置对应的实体字段
            for (java.util.Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                String paramName = entry.getKey();
                String[] paramValues = entry.getValue();
                
                if (paramValues != null && paramValues.length > 0) {
                    String paramValue = paramValues[0]; // 取第一个值
                    
                    // 尝试设置字段值
                    setFieldIfExists(entityClass, logEntity, paramName, paramValue);
                }
            }
            
        } catch (Exception e) {
            log.debug("从请求中提取自定义字段失败", e);
        }
    }
    

    
    /**
     * 如果字段存在则设置值
     */
    private boolean setFieldIfExists(Class<?> entityClass, Object entity, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = entityClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(entity, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}