package com.diit.example.entity;

import com.diit.common.log.entity.BaseLogEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务日志实体类
 * 用于测试@GenericLog注解的自定义实体功能
 * 
 * @author diit
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessLogEntity extends BaseLogEntity {
    
    /** 业务类型 */
    private String businessType;
    
    /** 部门 */
    private String department;
    
    /** 项目 */
    private String project;
    
    /** 自定义字段1 */
    private String customField1;
    
    /** 自定义字段2 */
    private String customField2;
    
    /** 业务数据 */
    private String businessData;
    
    /** 操作结果 */
    private String operationResult;
    
    /** 影响范围 */
    private String impactScope;
}
