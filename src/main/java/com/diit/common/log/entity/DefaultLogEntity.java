package com.diit.common.log.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 默认日志实体类
 * 当@GenericLog注解没有指定entityClass时使用
 * 
 * @author zzx
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DefaultLogEntity extends BaseLogEntity {
    
    /** 操作模块 */
    private String module;
    
    /** 操作对象 */
    private String target;
    
    /** 操作类型 */
    private String operationType;
    
    /** 异常信息 */
    private String exceptionMessage;
}