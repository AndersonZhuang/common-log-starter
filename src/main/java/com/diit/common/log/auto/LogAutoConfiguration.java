package com.diit.common.log.auto;

import com.diit.common.log.aspect.OperationLogAspect;
import com.diit.common.log.aspect.UserAccessLogAspect;
import com.diit.common.log.config.LogConfiguration;
import com.diit.common.log.properties.LogProperties;
import com.diit.common.log.sender.LogSenderFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * 日志自动配置类
 * 
 * @author diit
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(LogProperties.class)
@ConditionalOnProperty(prefix = "diit.log", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import({LogConfiguration.class})
public class LogAutoConfiguration {
    
    /**
     * 配置ObjectMapper
     */
    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
    
    /**
     * 配置访问日志切面
     */
    @Bean
    @ConditionalOnMissingBean
    public UserAccessLogAspect userAccessLogAspect() {
        log.info("初始化用户访问日志切面");
        return new UserAccessLogAspect();
    }
    
    /**
     * 配置操作日志切面
     */
    @Bean
    @ConditionalOnMissingBean
    public OperationLogAspect operationLogAspect() {
        log.info("初始化操作日志切面");
        return new OperationLogAspect();
    }
    
    /**
     * 配置日志发送器工厂
     */
    @Bean
    @ConditionalOnMissingBean
    public LogSenderFactory logSenderFactory() {
        log.info("初始化日志发送器工厂");
        return new LogSenderFactory();
    }
    
    /**
     * 配置Kafka相关Bean（条件化）
     */
    @Configuration
    @ConditionalOnClass(KafkaTemplate.class)
    @ConditionalOnProperty(prefix = "diit.log.kafka", name = "enabled", havingValue = "true")
    static class KafkaConfiguration {
        
        @Bean
        @ConditionalOnMissingBean
        public KafkaTemplate<String, Object> kafkaTemplate() {
            log.info("初始化KafkaTemplate");
            // 这里需要具体的Kafka配置
            // 实际使用时应该由使用者提供配置
            return null;
        }
    }
}
