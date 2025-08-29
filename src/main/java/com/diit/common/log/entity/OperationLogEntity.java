package com.diit.common.log.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 * 
 * @author diit
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogEntity {
    
    /** 主键ID */
    private String id;
    
    /** 用户名 */
    private String username;
    
    /** 真实姓名 */
    private String realName;
    
    /** 邮箱 */
    private String email;
    
    /** 角色名称 */
    private String roleName;
    
    /** 操作类型（新增、编辑、删除、查询等） */
    private String operationType;
    
    /** 操作描述 */
    private String description;
    
    /** 操作时间 */
    private String operationTime;
    
    /** 操作时间戳 */
    private LocalDateTime operationTimestamp;
    
    /** 客户端IP地址 */
    private String clientIp;
    
    /** IP地理位置 */
    private String ipLocation;
    
    /** 浏览器信息 */
    private String browser;
    
    /** 操作系统 */
    private String operatingSystem;
    
    /** 设备类型 */
    private String deviceType;
    
    /** 操作状态（成功、失败） */
    private String status;
    
    /** 响应时间（毫秒） */
    private Long responseTime;
    
    /** 请求URI */
    private String requestUri;
    
    /** 请求方法 */
    private String requestMethod;
    
    /** 用户代理 */
    private String userAgent;
    
    /** 会话ID */
    private String sessionId;
    
    /** 操作模块 */
    private String module;
    
    /** 操作对象 */
    private String target;
    
    /** 操作前数据 */
    private String beforeData;
    
    /** 操作后数据 */
    private String afterData;
    
    /** 异常信息 */
    private String exceptionMessage;
    
    /** 创建时间 */
    private LocalDateTime createTime;
}
