package com.diit.example.entity;

import com.diit.common.log.entity.BaseLogEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单日志实体类
 * 演示如何在具体项目中继承BaseLogEntity并添加业务相关字段
 * 
 * @author zzx
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderLogEntity extends BaseLogEntity {
    
    // ==================== 订单基础信息 ====================
    
    /** 订单ID */
    private String orderId;
    
    /** 订单号 */
    private String orderNumber;
    
    /** 订单状态 */
    private String orderStatus;
    
    /** 订单类型 */
    private String orderType;
    
    // ==================== 金额信息 ====================
    
    /** 订单总金额 */
    private BigDecimal totalAmount;
    
    /** 实付金额 */
    private BigDecimal paidAmount;
    
    /** 优惠金额 */
    private BigDecimal discountAmount;
    
    /** 运费 */
    private BigDecimal shippingFee;
    
    // ==================== 用户信息 ====================
    
    /** 用户ID */
    private String userId;
    
    /** 用户手机号 */
    private String userPhone;
    
    /** 用户邮箱 */
    private String userEmail;
    
    /** VIP等级 */
    private String vipLevel;
    
    // ==================== 商品信息 ====================
    
    /** 商品数量 */
    private Integer productCount;
    
    /** 商品列表 */
    private List<String> productIds;
    
    /** 商品名称列表 */
    private List<String> productNames;
    
    /** 商品分类 */
    private String productCategory;
    
    // ==================== 物流信息 ====================
    
    /** 收货地址 */
    private String deliveryAddress;
    
    /** 收货人 */
    private String receiverName;
    
    /** 收货人电话 */
    private String receiverPhone;
    
    /** 物流公司 */
    private String logisticsCompany;
    
    /** 物流单号 */
    private String trackingNumber;
    
    // ==================== 支付信息 ====================
    
    /** 支付方式 */
    private String paymentMethod;
    
    /** 支付渠道 */
    private String paymentChannel;
    
    /** 支付流水号 */
    private String paymentTransactionId;
    
    /** 支付时间 */
    private String paymentTime;
    
    // ==================== 业务扩展字段 ====================
    
    /** 促销活动ID */
    private String promotionId;
    
    /** 优惠券ID */
    private String couponId;
    
    /** 营销渠道 */
    private String marketingChannel;
    
    /** 来源平台 */
    private String sourcePlatform;
    
    /** 设备信息 */
    private String deviceInfo;
    
    /** 浏览器信息 */
    private String browserInfo;
    
    /** 扩展属性 */
    private Map<String, Object> extendedProperties;
    
    /** 备注信息 */
    private String remarks;
    
    /** 操作来源 */
    private String operationSource;
    
    /** 风险等级 */
    private String riskLevel;
    
    /** 审核状态 */
    private String auditStatus;
    
    /** 审核人 */
    private String auditor;
    
    /** 审核时间 */
    private String auditTime;
}
