package com.diit.example.entity;

import com.diit.common.log.entity.BaseLogEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 用户活动日志实体类
 * 演示用户行为追踪的自定义字段
 * 
 * @author zzx
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserActivityLogEntity extends BaseLogEntity {
    
    // ==================== 用户信息 ====================
    
    /** 用户ID */
    private String userId;
    
    /** 用户类型 */
    private String userType;
    
    /** 用户等级 */
    private String userLevel;
    
    /** 注册时间 */
    private String registerTime;
    
    /** 最后登录时间 */
    private String lastLoginTime;
    
    // ==================== 活动信息 ====================
    
    /** 活动类型 */
    private String activityType;
    
    /** 活动子类型 */
    private String activitySubType;
    
    /** 活动描述 */
    private String activityDescription;
    
    /** 活动持续时间（秒） */
    private Long duration;
    
    /** 活动结果 */
    private String activityResult;
    
    // ==================== 页面信息 ====================
    
    /** 页面URL */
    private String pageUrl;
    
    /** 页面标题 */
    private String pageTitle;
    
    /** 页面分类 */
    private String pageCategory;
    
    /** 页面停留时间（秒） */
    private Long pageStayTime;
    
    /** 页面滚动深度 */
    private Integer scrollDepth;
    
    // ==================== 设备信息 ====================
    
    /** 设备类型 */
    private String deviceType;
    
    /** 操作系统 */
    private String operatingSystem;
    
    /** 浏览器类型 */
    private String browserType;
    
    /** 浏览器版本 */
    private String browserVersion;
    
    /** 屏幕分辨率 */
    private String screenResolution;
    
    /** 设备ID */
    private String deviceId;
    
    // ==================== 地理位置信息 ====================
    
    /** 国家 */
    private String country;
    
    /** 省份 */
    private String province;
    
    /** 城市 */
    private String city;
    
    /** 经度 */
    private Double longitude;
    
    /** 纬度 */
    private Double latitude;
    
    /** IP地址 */
    private String ipAddress;
    
    // ==================== 业务相关字段 ====================
    
    /** 商品ID */
    private String productId;
    
    /** 商品名称 */
    private String productName;
    
    /** 商品价格 */
    private String productPrice;
    
    /** 商品分类 */
    private String productCategory;
    
    /** 搜索关键词 */
    private String searchKeyword;
    
    /** 推荐算法ID */
    private String recommendationAlgorithmId;
    
    /** 推荐结果 */
    private String recommendationResult;
    
    // ==================== 性能相关字段 ====================
    
    /** 页面加载时间（毫秒） */
    private Long pageLoadTime;
    
    /** API响应时间（毫秒） */
    private Long apiResponseTime;
    
    /** 网络类型 */
    private String networkType;
    
    /** 网络速度 */
    private String networkSpeed;
    
    // ==================== 扩展字段 ====================
    
    /** 自定义属性 */
    private Map<String, Object> customAttributes;
    
    /** 标签 */
    private String tags;
    
    /** 优先级 */
    private Integer priority;
    
    /** 是否重要事件 */
    private Boolean isImportant;
    
    /** 关联事件ID */
    private String relatedEventId;
    
    /** 会话ID */
    private String sessionId;
    
    /** 用户代理 */
    private String userAgent;
    
    /** 引用页面 */
    private String referrer;
    
    /** 语言设置 */
    private String language;
    
    /** 时区 */
    private String timezone;
}
