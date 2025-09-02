package com.diit.common.log.annotation;

import com.diit.common.log.entity.BaseLogEntity;
import com.diit.common.log.entity.DefaultLogEntity;

import java.lang.annotation.*;

/**
 * 通用日志注解
 * 支持自定义实体类和发送器类型，提供最大的灵活性
 * 
 * 使用示例：
 * <pre>
 * // 使用默认实体类
 * &#64;GenericLog("用户登录")
 * public String login() { ... }
 * 
 * // 使用自定义实体类
 * &#64;GenericLog(value = "数据导出", entityClass = MyCustomLogEntity.class)
 * public void exportData() { ... }
 * 
 * // 指定发送器类型
 * &#64;GenericLog(value = "重要操作", senderType = "kafka")
 * public void importantAction() { ... }
 * </pre>
 * 
 * @author diit
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GenericLog {
    
    /**
     * 操作描述
     * 支持SpEL表达式，可以获取方法参数和返回值
     * 
     * 示例：
     * - "用户登录" - 固定描述
     * - "删除用户：#{#username}" - 使用参数
     * - "操作结果：#{#result}" - 使用返回值
     */
    String value() default "";
    
    /**
     * 操作模块
     * 用于分类不同的业务模块
     */
    String module() default "";
    
    /**
     * 操作对象
     * 描述操作的目标对象
     */
    String target() default "";
    
    /**
     * 日志实体类
     * 必须继承自BaseLogEntity
     * 默认使用OperationLogEntity
     */
    Class<? extends BaseLogEntity> entityClass() default DefaultLogEntity.class;
    
    /**
     * 发送器类型
     * 支持的类型：database、kafka、elasticsearch、http、file
     * 如果为空，则使用配置文件中的默认发送器
     */
    String senderType() default "";
    
    /**
     * 是否记录方法参数
     */
    boolean logArgs() default false;
    
    /**
     * 是否记录返回值
     */
    boolean logResult() default false;
    
    /**
     * 是否记录异常信息
     */
    boolean logException() default true;
    
    /**
     * 操作类型
     * 如：CREATE、UPDATE、DELETE、QUERY等
     */
    String operationType() default "";
    
    /**
     * 是否异步发送日志
     */
    boolean async() default true;
    
    /**
     * 日志优先级
     * 用于在高并发时决定是否跳过某些低优先级日志
     */
    int priority() default 5;
}