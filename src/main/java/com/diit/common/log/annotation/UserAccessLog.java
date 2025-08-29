package com.diit.common.log.annotation;

import java.lang.annotation.*;

/**
 * 用户访问日志注解
 * 用于标记需要记录用户访问日志的方法
 * 
 * @author diit
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UserAccessLog {
    
    /**
     * 访问类型（如：登录、注销、访问等）
     */
    String type();
    
    /**
     * 访问描述（如：用户登录系统、用户注销系统等）
     */
    String description() default "";
    
    /**
     * 是否记录请求参数
     */
    boolean recordParams() default false;
    
    /**
     * 是否记录响应结果
     */
    boolean recordResponse() default false;
    
    /**
     * 是否记录异常堆栈
     */
    boolean recordStackTrace() default true;
    
    /**
     * 操作模块
     */
    String module() default "";
    
    /**
     * 操作对象
     */
    String target() default "";
}
