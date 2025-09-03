package com.diit.example.entity;

import com.diit.common.log.entity.BaseLogEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 自定义日志实体类
 * 演示包含额外字段的自定义日志实体
 * 
 * @author diit
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CustomLogEntity extends BaseLogEntity {
    
    /** 用户角色 */
    private String userRole;
    
    /** 操作级别（1-低，2-中，3-高） */
    private Integer operationLevel;
    
    /** 操作耗时（毫秒） */
    private Long duration;
    
    /** 影响的数据量 */
    private Integer affectedRows;
    
    /** 业务金额 */
    private BigDecimal amount;
    
    /** 备注信息 */
    private String remarks;
    
    /** 操作前的数据 */
    private String beforeData;
    
    /** 操作后的数据 */
    private String afterData;
    
    /** 风险等级 */
    private String riskLevel;
    
    /** 审批状态 */
    private String approvalStatus;
    
    /** 设备信息 */
    private String deviceInfo;
    
    /** 地理位置 */
    private String location;
    
    /** 扩展数据（JSON格式） */
    private String extensionData;
}