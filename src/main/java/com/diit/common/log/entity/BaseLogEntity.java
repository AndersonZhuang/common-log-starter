package com.diit.common.log.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.logging.LogLevel;

import java.time.LocalDateTime;

/**
 * 日志实体基类
 * 包含最核心的通用字段，开发者可以继承此类并添加自定义字段
 * 
 * 使用示例：
 * <pre>
 * &#64;Data
 * &#64;EqualsAndHashCode(callSuper = true)
 * public class CustomLogEntity extends BaseLogEntity {
 *     private String customField1;
 *     private Integer customField2;
 * }
 * </pre>
 * 
 * @author diit
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseLogEntity {
    
    /** 主键ID */
    private String id;

    /** 时间戳 yyyy-mm-dd hh:mm:ss*/
    private LocalDateTime timestamp;

    /** 日志内容*/
    private String content;

    /** 日志级别*/
    private LogLevel level;

}
