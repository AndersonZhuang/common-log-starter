package com.diit.example.entity;

import com.diit.common.log.entity.BaseLogEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务日志实体类
 * 用于测试@GenericLog注解的自定义实体功能
 * 只保留四个核心业务字段，避免冗余
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
}
