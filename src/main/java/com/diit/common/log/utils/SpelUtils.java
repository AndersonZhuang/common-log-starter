package com.diit.common.log.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * SpEL表达式工具类
 * 用于解析注解中的SpEL表达式，支持获取方法参数和返回值
 * 
 * @author diit
 */
@Slf4j
@Component
public class SpelUtils {
    
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 解析SpEL表达式
     * 
     * @param template SpEL表达式模板
     * @param joinPoint 切点信息
     * @param result 方法返回值
     * @param exception 异常信息
     * @return 解析后的字符串
     */
    public String parseExpression(String template, ProceedingJoinPoint joinPoint, 
                                 Object result, Throwable exception) {
        
        if (template == null || !template.contains("#{")) {
            return template;
        }
        
        try {
            // 创建表达式上下文
            EvaluationContext context = createEvaluationContext(joinPoint, result, exception);
            
            // 解析并执行表达式
            Expression expression = parser.parseExpression(template, new org.springframework.expression.common.TemplateParserContext());
            Object value = expression.getValue(context);
            
            return value != null ? value.toString() : template;
        } catch (Exception e) {
            log.debug("SpEL表达式解析失败: {}", template, e);
            return template;
        }
    }
    
    /**
     * 创建SpEL表达式求值上下文
     */
    private EvaluationContext createEvaluationContext(ProceedingJoinPoint joinPoint, 
                                                     Object result, Throwable exception) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        
        try {
            // 获取方法信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Object[] args = joinPoint.getArgs();
            Parameter[] parameters = method.getParameters();
            
            // 设置方法参数到上下文
            if (parameters != null && args != null) {
                for (int i = 0; i < parameters.length && i < args.length; i++) {
                    String paramName = parameters[i].getName();
                    context.setVariable(paramName, args[i]);
                }
            }
            
            // 设置参数数组
            context.setVariable("args", args);
            
            // 设置返回值
            if (result != null) {
                context.setVariable("result", result);
            }
            
            // 设置异常信息
            if (exception != null) {
                context.setVariable("exception", exception);
                context.setVariable("exceptionMessage", exception.getMessage());
            }
            
            // 设置方法信息
            context.setVariable("methodName", method.getName());
            context.setVariable("className", joinPoint.getTarget().getClass().getSimpleName());
            
            // 设置一些常用的工具方法
            context.setVariable("currentTime", System.currentTimeMillis());
            context.setVariable("currentTimeStr", java.time.LocalDateTime.now().toString());
            
        } catch (Exception e) {
            log.debug("创建SpEL上下文失败", e);
        }
        
        return context;
    }
    
    /**
     * 将对象转换为JSON字符串
     * 
     * @param obj 对象
     * @return JSON字符串
     */
    public String convertToJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        
        try {
            // 对于简单类型，直接toString
            if (isSimpleType(obj.getClass())) {
                return obj.toString();
            }
            
            // 对于复杂对象，转换为JSON
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.debug("对象转JSON失败: {}", obj.getClass(), e);
            return obj.toString();
        }
    }
    
    /**
     * 判断是否为简单类型
     */
    private boolean isSimpleType(Class<?> clazz) {
        return clazz.isPrimitive() 
            || String.class.equals(clazz)
            || Number.class.isAssignableFrom(clazz)
            || Boolean.class.equals(clazz)
            || Character.class.equals(clazz)
            || java.util.Date.class.isAssignableFrom(clazz)
            || java.time.temporal.Temporal.class.isAssignableFrom(clazz);
    }
    
    /**
     * 安全地获取对象的字符串表示
     * 
     * @param obj 对象
     * @param maxLength 最大长度
     * @return 字符串表示
     */
    public String safeToString(Object obj, int maxLength) {
        if (obj == null) {
            return "null";
        }
        
        try {
            String str = convertToJson(obj);
            if (str.length() > maxLength) {
                return str.substring(0, maxLength) + "...";
            }
            return str;
        } catch (Exception e) {
            return obj.getClass().getSimpleName() + "@" + Integer.toHexString(obj.hashCode());
        }
    }
    
    /**
     * 测试SpEL表达式是否有效
     * 
     * @param expression SpEL表达式
     * @return 是否有效
     */
    public boolean isValidExpression(String expression) {
        if (expression == null || !expression.contains("#{")) {
            return true; // 非SpEL表达式认为有效
        }
        
        try {
            parser.parseExpression(expression, new org.springframework.expression.common.TemplateParserContext());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}