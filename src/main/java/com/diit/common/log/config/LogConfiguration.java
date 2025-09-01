package com.diit.common.log.config;

import com.diit.common.log.properties.LogProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 日志配置类
 * 
 * @author diit
 */
@Slf4j
@Configuration
public class LogConfiguration {
    
    /**
     * 配置Kafka生产者工厂
     */
    @Bean
    @ConditionalOnProperty(prefix = "diit.log.kafka", name = "enabled", havingValue = "true")
    public ProducerFactory<String, Object> kafkaProducerFactory(LogProperties logProperties) {
        Map<String, Object> configProps = new HashMap<>();
        
        // 基础配置
        configProps.put("bootstrap.servers", logProperties.getKafka().getBootstrapServers());
        configProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        configProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        
        // 生产者配置
        LogProperties.Kafka.Producer producer = logProperties.getKafka().getProducer();
        configProps.put("retries", producer.getRetries());
        configProps.put("batch.size", producer.getBatchSize());
        configProps.put("linger.ms", producer.getLingerMs());
        configProps.put("buffer.memory", producer.getBufferMemory());
        
        // 可靠性配置
        configProps.put("acks", "all");
        configProps.put("enable.idempotence", "true");
        
        log.info("配置Kafka生产者工厂: {}", configProps);
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    
    /**
     * 配置Kafka模板
     */
    @Bean
    @ConditionalOnProperty(prefix = "diit.log.kafka", name = "enabled", havingValue = "true")
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        log.info("初始化KafkaTemplate");
        return new KafkaTemplate<>(producerFactory);
    }
}
