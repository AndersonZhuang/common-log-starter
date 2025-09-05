package com.diit.common.log.factory;

import com.diit.common.log.entity.BaseLogEntity;
import com.diit.common.log.utils.LogContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 日志实体工厂
 * 负责创建和填充日志实体的基础字段
 * 
 * @author zzx
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
        
        // 设置时间戳
        logEntity.setTimestamp(LocalDateTime.now());
        
        // 设置内容（描述）
        logEntity.setContent(description);
        
        // 设置日志级别
        logEntity.setLevel(org.springframework.boot.logging.LogLevel.INFO);
        
        // 注意：BaseLogEntity只包含id、timestamp、content、level四个基础字段
        // 其他字段如username、clientIp、status等需要子类自己定义
        // 这里不再尝试设置这些字段，避免反射错误
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
        target.setTimestamp(source.getTimestamp());
        target.setContent(source.getContent());
        target.setLevel(source.getLevel());
        
        // 只复制BaseLogEntity中确实存在的字段
        // 其他字段由子类自己处理
    }
    
    /**
     * 安全地设置字段值，如果方法不存在则忽略
     */
    private void setFieldSafely(BaseLogEntity entity, String methodName, Object value) {
        if (value == null) return;
        
        try {
            Class<?> entityClass = entity.getClass();
            Method method = null;
            
            // 尝试找到对应的setter方法
            if (value instanceof String) {
                method = entityClass.getMethod(methodName, String.class);
            } else if (value instanceof java.time.LocalDateTime) {
                method = entityClass.getMethod(methodName, java.time.LocalDateTime.class);
            }
            
            if (method != null) {
                method.invoke(entity, value);
            }
        } catch (Exception e) {
            // 方法不存在或调用失败，忽略
            log.debug("无法设置字段 {}: {}", methodName, e.getMessage());
        }
    }
    
    /**
     * 安全地获取字段值，如果方法不存在则返回null
     */
    private Object getFieldSafely(BaseLogEntity entity, String methodName) {
        try {
            Class<?> entityClass = entity.getClass();
            Method method = entityClass.getMethod(methodName);
            return method.invoke(entity);
        } catch (Exception e) {
            // 方法不存在或调用失败，返回null
            log.debug("无法获取字段 {}: {}", methodName, e.getMessage());
            return null;
        }
    }
}