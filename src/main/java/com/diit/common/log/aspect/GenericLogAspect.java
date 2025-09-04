package com.diit.common.log.aspect;

import com.diit.common.log.annotation.GenericLog;
import com.diit.common.log.entity.BaseLogEntity;
import com.diit.common.log.factory.LogEntityFactory;

import com.diit.common.log.service.LogSenderService;
import com.diit.common.log.utils.SpelUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * 通用日志切面
 * 支持任何继承自BaseLogEntity的自定义实体类
 * 
 * @author diit
 */
@Slf4j
@Aspect
@Component
public class GenericLogAspect {
    
    @Autowired
    private LogEntityFactory logEntityFactory;
    
    @Autowired
    private LogSenderService logSenderService;
    
    @Autowired
    private SpelUtils spelUtils;
    
    /**
     * 切点：所有标注了@GenericLog的方法
     */
    @Pointcut("@annotation(com.diit.common.log.annotation.GenericLog)")
    public void genericLogPointcut() {}
    
    /**
     * 环绕通知：记录操作日志
     */
    @Around("genericLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Throwable exception = null;
        
        try {
            // 执行原方法
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            exception = e;
            throw e;
        } finally {
            // 记录日志
            try {
                recordLog(joinPoint, result, exception, startTime);
            } catch (Exception e) {
                log.error("记录通用日志失败", e);
            }
        }
    }
    
    /**
     * 异常通知：记录异常日志
     */
    @AfterThrowing(pointcut = "genericLogPointcut()", throwing = "exception")
    public void afterThrowing(JoinPoint joinPoint, Throwable exception) {
        // 异常情况已在around方法中处理，这里不重复处理
    }
    
    /**
     * 记录日志
     */
    private void recordLog(ProceedingJoinPoint joinPoint, Object result, 
                          Throwable exception, long startTime) {
        
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        GenericLog annotation = method.getAnnotation(GenericLog.class);
        
        if (annotation == null) {
            return;
        }
        
        try {
            // 计算响应时间
            long responseTime = System.currentTimeMillis() - startTime;
            
            // 解析操作描述（支持SpEL表达式）
            String description = parseDescription(annotation.value(), joinPoint, result, exception);
            
            // 创建日志实体
            BaseLogEntity logEntity = logEntityFactory.createLogEntity(
                annotation.entityClass(), description);
            
            // 填充扩展字段
            fillExtendedFields(logEntity, annotation, joinPoint, result, exception, responseTime);
            
            // 发送日志
            sendLog(logEntity, annotation);
            
        } catch (Exception e) {
            log.error("处理通用日志记录失败: method={}", method.getName(), e);
        }
    }
    
    /**
     * 解析操作描述（支持SpEL表达式）
     */
    private String parseDescription(String template, ProceedingJoinPoint joinPoint, 
                                   Object result, Throwable exception) {
        if (!StringUtils.hasText(template)) {
            return "操作记录";
        }
        
        try {
            return spelUtils.parseExpression(template, joinPoint, result, exception);
        } catch (Exception e) {
            log.debug("SpEL表达式解析失败，使用原始模板: {}", template, e);
            return template;
        }
    }
    
    /**
     * 填充扩展字段
     */
    private void fillExtendedFields(BaseLogEntity logEntity, GenericLog annotation,
                                   ProceedingJoinPoint joinPoint, Object result, 
                                   Throwable exception, long responseTime) {
        
        // 通过反射填充可能存在的扩展字段
        try {
            Class<?> entityClass = logEntity.getClass();
            
            // 确保基础字段被正确设置（只在为null时设置）
            if (logEntity.getTimestamp() == null) {
                logEntity.setTimestamp(java.time.LocalDateTime.now());
            }
            if (logEntity.getContent() == null) {
                logEntity.setContent("操作记录");
            }
            if (logEntity.getLevel() == null) {
                logEntity.setLevel(org.springframework.boot.logging.LogLevel.INFO);
            }
            
            // 设置模块
            if (StringUtils.hasText(annotation.module())) {
                setFieldIfExists(entityClass, logEntity, "module", annotation.module());
            }
            
            // 设置目标
            if (StringUtils.hasText(annotation.target())) {
                setFieldIfExists(entityClass, logEntity, "target", annotation.target());
            }
            
            // 设置操作类型
            if (StringUtils.hasText(annotation.operationType())) {
                setFieldIfExists(entityClass, logEntity, "operationType", annotation.operationType());
            }
            
            // 设置响应时间
            setFieldIfExists(entityClass, logEntity, "responseTime", responseTime);
            
            // 设置状态
            String status = exception == null ? "SUCCESS" : "FAILED";
            setFieldIfExists(entityClass, logEntity, "status", status);
            
            // 记录异常信息
            if (exception != null && annotation.logException()) {
                setFieldIfExists(entityClass, logEntity, "exceptionMessage", 
                               exception.getMessage());
            }
            
            // 记录方法参数
            if (annotation.logArgs()) {
                String argsJson = spelUtils.convertToJson(joinPoint.getArgs());
                setFieldIfExists(entityClass, logEntity, "requestArgs", argsJson);
                
                // 尝试从方法参数中提取字段值
                extractAndSetParameterFields(logEntity, joinPoint);
            }
            
            // 记录返回值
            if (annotation.logResult() && result != null) {
                String resultJson = spelUtils.convertToJson(result);
                setFieldIfExists(entityClass, logEntity, "responseData", resultJson);
            }
            
        } catch (Exception e) {
            log.debug("填充扩展字段失败", e);
        }
    }
    
    /**
     * 如果字段存在则设置值
     */
    private void setFieldIfExists(Class<?> clazz, Object obj, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (NoSuchFieldException e) {
            // 字段不存在，忽略
        } catch (Exception e) {
            log.debug("设置字段失败: {}={}", fieldName, value, e);
        }
    }
    
    /**
     * 发送日志
     */
    private void sendLog(BaseLogEntity logEntity, GenericLog annotation) {
        try {
            if (annotation.async()) {
                logSenderService.sendAsync(logEntity, annotation.senderType());
            } else {
                logSenderService.send(logEntity, annotation.senderType());
            }
        } catch (Exception e) {
            log.error("发送日志失败: entityClass={}, senderType={}", 
                     logEntity.getClass().getSimpleName(), annotation.senderType(), e);
        }
    }
    
    /**
     * 从方法参数中提取字段值并设置到日志实体
     */
    private void extractAndSetParameterFields(BaseLogEntity logEntity, ProceedingJoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args == null || args.length == 0) {
                return;
            }
            
            // 获取方法参数名（如果可用）
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            
            if (paramNames == null) {
                return;
            }
            
            // 遍历参数，尝试设置到对应的字段
            for (int i = 0; i < args.length && i < paramNames.length; i++) {
                String paramName = paramNames[i];
                Object paramValue = args[i];
                
                if (paramValue == null) {
                    continue;
                }
                
                // 尝试设置到对应的字段
                setFieldIfExists(logEntity.getClass(), logEntity, paramName, paramValue);
            }
            
        } catch (Exception e) {
            log.debug("从方法参数提取字段值失败", e);
        }
    }
}