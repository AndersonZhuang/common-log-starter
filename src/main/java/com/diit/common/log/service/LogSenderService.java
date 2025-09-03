package com.diit.common.log.service;

import com.diit.common.log.entity.BaseLogEntity;
import com.diit.common.log.exception.LogResultCode;
import com.diit.common.log.exception.LogSenderException;
import com.diit.common.log.sender.GenericLogSender;
import com.diit.common.log.sender.LogSenderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日志发送服务
 * 管理所有的日志发送器，根据配置和注解选择合适的发送器
 * 
 * @author diit
 */
@Slf4j
@Service
public class LogSenderService {
    
    /**
     * 发送器缓存
     * key: senderType, value: GenericLogSender实例
     */
    private final Map<String, GenericLogSender<? extends BaseLogEntity>> senderCache = new ConcurrentHashMap<>();
    
    // 移除硬编码的默认发送器类型，改为从配置中获取
    
    @Autowired
    private LogSenderFactory logSenderFactory;
    
    /**
     * 初始化发送器缓存
     */
    @jakarta.annotation.PostConstruct
    public void initSenderCache() {
        List<GenericLogSender<? extends BaseLogEntity>> allSenders = logSenderFactory.getAllGenericSenders();
        if (allSenders != null) {
            for (GenericLogSender<? extends BaseLogEntity> sender : allSenders) {
                String senderType = sender.getSenderType();
                if (StringUtils.hasText(senderType)) {
                    senderCache.put(senderType, sender);
                    log.info("注册日志发送器: {} -> {}", senderType, sender.getClass().getSimpleName());
                }
            }
        }
        log.info("日志发送器初始化完成，共注册{}个发送器", senderCache.size());
    }
    
    /**
     * 发送日志
     * 
     * @param logEntity 日志实体
     * @param senderType 发送器类型
     */
    public void send(Object logEntity, String senderType) {
        // 将任意对象转换为BaseLogEntity
        BaseLogEntity baseLogEntity = convertToBaseLogEntity(logEntity);
        if (baseLogEntity == null) {
            throw new LogSenderException(LogResultCode.ENTITY_SERIALIZATION_FAILED, 
                String.format("无法转换日志实体: %s", logEntity.getClass().getSimpleName()));
        }
        
        GenericLogSender<BaseLogEntity> sender = findSender(baseLogEntity, senderType);
        if (sender == null) {
            throw new LogSenderException(LogResultCode.SENDER_NOT_CONFIGURED, 
                String.format("未找到合适的日志发送器: senderType=%s, entityClass=%s", 
                    senderType, logEntity.getClass().getSimpleName()));
        }
        
        try {
            sender.send(baseLogEntity);
            log.debug("日志发送成功: senderType={}, entityClass={}", 
                     senderType, logEntity.getClass().getSimpleName());
        } catch (Exception e) {
            log.error("日志发送失败: senderType={}, entityClass={}", 
                     senderType, logEntity.getClass().getSimpleName(), e);
            throw e; // 重新抛出异常，不要静默处理
        }
    }
    
    /**
     * 异步发送日志
     * 
     * @param logEntity 日志实体
     * @param senderType 发送器类型
     */
    public void sendAsync(Object logEntity, String senderType) {
        // 将任意对象转换为BaseLogEntity
        BaseLogEntity baseLogEntity = convertToBaseLogEntity(logEntity);
        if (baseLogEntity == null) {
            throw new LogSenderException(LogResultCode.ENTITY_SERIALIZATION_FAILED, 
                String.format("无法转换日志实体: %s", logEntity.getClass().getSimpleName()));
        }
        
        GenericLogSender<BaseLogEntity> sender = findSender(baseLogEntity, senderType);
        if (sender == null) {
            throw new LogSenderException(LogResultCode.SENDER_NOT_CONFIGURED, 
                String.format("未找到合适的日志发送器: senderType=%s, entityClass=%s", 
                    senderType, logEntity.getClass().getSimpleName()));
        }
        
        try {
            sender.sendAsync(baseLogEntity);
            log.debug("异步日志发送成功: senderType={}, entityClass={}", 
                     senderType, logEntity.getClass().getSimpleName());
        } catch (Exception e) {
            log.error("异步日志发送失败: senderType={}, entityClass={}", 
                     senderType, logEntity.getClass().getSimpleName(), e);
            throw e; // 重新抛出异常，不要静默处理
        }
    }
    
    /**
     * 批量发送日志
     * 
     * @param logEntities 日志实体列表
     * @param senderType 发送器类型
     */
    @SuppressWarnings("unchecked")
    public void sendBatch(List<? extends BaseLogEntity> logEntities, String senderType) {
        if (logEntities == null || logEntities.isEmpty()) {
            return;
        }
        
        BaseLogEntity firstEntity = logEntities.get(0);
        GenericLogSender<BaseLogEntity> sender = findSender(firstEntity, senderType);
        if (sender == null) {
            throw new LogSenderException(LogResultCode.SENDER_NOT_CONFIGURED, 
                String.format("未找到合适的日志发送器: senderType=%s, entityClass=%s", 
                    senderType, firstEntity.getClass().getSimpleName()));
        }
        
        try {
            sender.sendBatch((List<BaseLogEntity>) logEntities);
            log.debug("批量日志发送成功: senderType={}, count={}", senderType, logEntities.size());
        } catch (Exception e) {
            log.error("批量日志发送失败: senderType={}, count={}", senderType, logEntities.size(), e);
            throw e; // 重新抛出异常，不要静默处理
        }
    }
    
    /**
     * 查找合适的发送器
     * 
     * @param logEntity 日志实体
     * @param senderType 指定的发送器类型
     * @return 发送器实例
     */
    @SuppressWarnings("unchecked")
    private GenericLogSender<BaseLogEntity> findSender(BaseLogEntity logEntity, String senderType) {
        
        // 1. 如果指定了发送器类型，优先使用指定的
        if (StringUtils.hasText(senderType)) {
            GenericLogSender<? extends BaseLogEntity> sender = logSenderFactory.getGenericLogSender(senderType);
            if (sender != null && sender.supports(logEntity.getClass())) {
                return (GenericLogSender<BaseLogEntity>) sender;
            }
        }
        
        // 2. 使用配置中的默认发送器
        GenericLogSender<? extends BaseLogEntity> defaultSender = logSenderFactory.getGenericLogSender(null);
        if (defaultSender != null && defaultSender.supports(logEntity.getClass())) {
            return (GenericLogSender<BaseLogEntity>) defaultSender;
        }
        
        // 3. 寻找第一个支持该实体类型的发送器
        List<GenericLogSender<? extends BaseLogEntity>> allSenders = logSenderFactory.getAllGenericSenders();
        for (GenericLogSender<? extends BaseLogEntity> sender : allSenders) {
            if (sender.supports(logEntity.getClass())) {
                return (GenericLogSender<BaseLogEntity>) sender;
            }
        }
        
        return null;
    }
    
    /**
     * 检查是否存在指定类型的发送器
     * 
     * @param senderType 发送器类型
     * @return 是否存在
     */
    public boolean hasSender(String senderType) {
        return logSenderFactory.hasSender(senderType);
    }
    
    /**
     * 获取所有已注册的发送器类型
     * 
     * @return 发送器类型列表
     */
    public java.util.Set<String> getRegisteredSenderTypes() {
        return logSenderFactory.getAllGenericSenders().stream()
                .map(GenericLogSender::getSenderType)
                .collect(java.util.stream.Collectors.toSet());
    }
    
    /**
     * 将任意日志实体转换为BaseLogEntity
     * 
     * @param logEntity 原始日志实体
     * @return BaseLogEntity实例
     */
    private BaseLogEntity convertToBaseLogEntity(Object logEntity) {
        if (logEntity instanceof BaseLogEntity) {
            return (BaseLogEntity) logEntity;
        }
        
        // 如果不是BaseLogEntity，创建一个新的BaseLogEntity并复制基础字段
        try {
            BaseLogEntity baseLogEntity = new com.diit.common.log.entity.DefaultLogEntity();
            
            // 使用反射复制字段
            java.lang.reflect.Field[] fields = logEntity.getClass().getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(logEntity);
                    
                    // 复制BaseLogEntity中的字段
                    switch (field.getName()) {
                        case "id":
                            baseLogEntity.setId((String) value);
                            break;
                        case "timestamp":
                            baseLogEntity.setTimestamp((java.time.LocalDateTime) value);
                            break;
                        case "content":
                            baseLogEntity.setContent((String) value);
                            break;
                        case "level":
                            baseLogEntity.setLevel((org.springframework.boot.logging.LogLevel) value);
                            break;
                    }
                } catch (Exception e) {
                    log.debug("复制字段失败: {}", field.getName(), e);
                }
            }
            
            return baseLogEntity;
        } catch (Exception e) {
            log.error("转换日志实体失败", e);
            return null;
        }
    }
}