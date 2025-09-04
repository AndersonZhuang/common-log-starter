package com.diit.example.service;

import com.diit.example.entity.BusinessLogEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;

/**
 * 直接Kafka服务
 * 不依赖Spring的KafkaTemplate，直接使用Kafka客户端
 * 
 * @author diit
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "diit.log.kafka", name = "enabled", havingValue = "true")
public class DirectKafkaService {
    
    private KafkaProducer<String, String> producer;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    
    @PostConstruct
    public void init() {
        try {
            // 创建Kafka生产者配置
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.ACKS_CONFIG, "all");
            props.put(ProducerConfig.RETRIES_CONFIG, 3);
            props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
            props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
            props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
            props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
            
            // 创建生产者
            producer = new KafkaProducer<>(props);
            log.info("✅ 直接Kafka生产者初始化成功");
            
        } catch (Exception e) {
            log.error("❌ 直接Kafka生产者初始化失败", e);
        }
    }
    
    @PreDestroy
    public void cleanup() {
        if (producer != null) {
            producer.close();
            log.info("🔄 Kafka生产者已关闭");
        }
    }
    
    /**
     * 发送自定义日志到Kafka
     */
    public void sendCustomLog(String message) {
        try {
            if (producer == null) {
                log.warn("⚠️ Kafka生产者未初始化");
                return;
            }
            
            // 创建业务日志实体（包含所有自定义字段）
            BusinessLogEntity businessLog = new BusinessLogEntity();
            
            // 设置基础字段（继承自BaseLogEntity）
            businessLog.setId(UUID.randomUUID().toString());
            businessLog.setTimestamp(LocalDateTime.now());
            businessLog.setContent("自定义日志：" + message);
            businessLog.setLevel(org.springframework.boot.logging.LogLevel.INFO);
            
            // 设置业务特定字段（BusinessLogEntity的核心字段）
            businessLog.setBusinessType("订单处理");
            businessLog.setDepartment("技术部");
            businessLog.setProject("电商系统");
            
            // 序列化为JSON
            String jsonMessage = objectMapper.writeValueAsString(businessLog);
            
            // 确定Topic名称
            String topic = "log_custom_messages";
            String key = "custom-" + System.currentTimeMillis();
            
            // 发送到Kafka
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, jsonMessage);
            
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    log.error("❌ 自定义日志发送失败 - Topic: {}, Key: {}, Error: {}", 
                             topic, key, exception.getMessage());
                } else {
                    log.info("✅ 自定义日志发送成功 - Topic: {}, Key: {}, Partition: {}, Offset: {}", 
                            topic, key, metadata.partition(), metadata.offset());
                }
            });
            
            log.info("🚀 自定义日志已发送到Kafka:");
            log.info("   Topic: {}", topic);
            log.info("   Key: {}", key);
            log.info("   Message: {}", jsonMessage);
            
        } catch (Exception e) {
            log.error("发送自定义日志失败", e);
            throw new RuntimeException("Failed to send custom log to Kafka", e);
        }
    }
}
