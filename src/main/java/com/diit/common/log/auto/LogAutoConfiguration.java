package com.diit.common.log.auto;

import com.diit.common.log.aspect.GenericLogAspect;
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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
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
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Import({LogConfiguration.class})
@ComponentScan(basePackages = "com.diit.common.log")
public class LogAutoConfiguration {
    

    
    /**
     * 配置ObjectMapper
     */
    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        return mapper;
    }
    
    /**
     * 配置通用日志切面
     */
    @Bean
    @ConditionalOnMissingBean
    public GenericLogAspect genericLogAspect() {
        return new GenericLogAspect();
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
     * 注意：这个配置类已经被LogConfiguration替代，这里保留是为了向后兼容
     */
    @Configuration
    @ConditionalOnClass(KafkaTemplate.class)
    @ConditionalOnProperty(prefix = "common.log.sender.kafka", name = "enabled", havingValue = "true")
    static class KafkaConfiguration {
        
        // 这个Bean现在由LogConfiguration提供，这里不再重复定义
        // 避免与LogConfiguration中的配置冲突
    }
}
