package com.diit.common.log.config;

import com.diit.common.log.properties.LogProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
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
    @ConditionalOnClass(KafkaTemplate.class)
    @ConditionalOnProperty(prefix = "diit.log.kafka", name = "enabled", havingValue = "true")
    public ProducerFactory<String, String> kafkaProducerFactory(LogProperties logProperties) {
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
    @ConditionalOnClass(KafkaTemplate.class)
    @ConditionalOnProperty(prefix = "diit.log.kafka", name = "enabled", havingValue = "true")
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        log.info("初始化KafkaTemplate");
        return new KafkaTemplate<>(producerFactory);
    }
    
    /**
     * 配置RestTemplate（用于HTTP和Elasticsearch发送器）
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "diit.log", name = "enabled", havingValue = "true")
    public RestTemplate restTemplate() {
        log.info("初始化RestTemplate");
        return new RestTemplate();
    }
    
    /**
     * 配置JdbcTemplate（用于数据库发送器）
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(DataSource.class)
    @ConditionalOnBean(DataSource.class)
    @ConditionalOnProperty(prefix = "diit.log.database", name = "enabled", havingValue = "true")
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        log.info("初始化JdbcTemplate");
        return new JdbcTemplate(dataSource);
    }
}
