package com.diit.common.log.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 用户访问日志实体
 * 
 * @author diit
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserAccessLogEntity extends BaseLogEntity {
    
    /** 用户名 */
    private String username;
    
    /** 真实姓名 */
    private String realName;
    
    /** 邮箱 */
    private String email;
    
    /** 访问类型（登录、注销等） */
    private String accessType;
    
    /** 访问描述 */
    private String description;
    
    /** 操作模块 */
    private String module;
    
    /** 操作对象 */
    private String target;
    
    /** 访问时间 */
    private String accessTime;
    
    /** 访问时间戳 */
    private LocalDateTime accessTimestamp;
    
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
    
    /** 访问状态（成功、失败） */
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
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 异常信息 */
    private String exceptionMessage;
}
