package com.diit.common.log.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 用于标记需要记录操作日志的方法
 * 
 * @author zzx
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    
    /**
     * 操作类型（如：新增、编辑、删除、查询等）
     */
    String type();
    
    /**
     * 操作描述（如：新增多媒体课程《生物科学》）
     */
    String description() default "";
    
    /**
     * 是否记录方法参数
     */
    boolean recordParams() default true;
    
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
    
    /**
     * 是否记录数据变更（前后对比）
     */
    boolean recordDataChange() default false;
}
