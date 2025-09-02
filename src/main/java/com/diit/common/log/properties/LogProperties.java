package com.diit.common.log.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 日志配置属性
 * 
 * @author diit
 */
@Data
@ConfigurationProperties(prefix = "diit.log")
public class LogProperties {
    
    /**
     * 是否启用日志功能
     */
    private boolean enabled = true;
    
    /**
     * 日志存储配置
     */
    private Storage storage = new Storage();
    
    /**
     * Kafka配置
     */
    private Kafka kafka = new Kafka();
    
    /**
     * Elasticsearch配置
     */
    private Elasticsearch elasticsearch = new Elasticsearch();
    
    /**
     * 数据库配置
     */
    private Database database = new Database();
    
    /**
     * HTTP配置
     */
    private Http http = new Http();
    
    /**
     * 日志记录配置
     */
    private Record record = new Record();
    
    @Data
    public static class Storage {
        /**
         * 存储类型：kafka, elasticsearch, database, http
         */
        private String type = "kafka";
        
        /**
         * 是否异步发送
         */
        private boolean async = true;
        
        /**
         * 批量发送大小
         */
        private int batchSize = 100;
        
        /**
         * 批量发送间隔（毫秒）
         */
        private long batchInterval = 1000;
    }
    
    @Data
    public static class Kafka {
        /**
         * 是否启用Kafka
         */
        private boolean enabled = true;
        
        /**
         * 访问日志Topic
         */
        private String accessLogTopic = "access-log";
        
        /**
         * 操作日志Topic
         */
        private String operationLogTopic = "operation-log";
        
        /**
         * 服务器地址
         */
        private String bootstrapServers = "localhost:9092";
        
        /**
         * 生产者配置
         */
        private Producer producer = new Producer();
        
        @Data
        public static class Producer {
            /**
             * 重试次数
             */
            private int retries = 3;
            
            /**
             * 批量大小
             */
            private int batchSize = 16384;
            
            /**
             * 延迟时间
             */
            private int lingerMs = 1;
            
            /**
             * 缓冲区大小
             */
            private int bufferMemory = 33554432;
        }
    }
    
    @Data
    public static class Elasticsearch {
        /**
         * 是否启用Elasticsearch
         */
        private boolean enabled = false;
        
        /**
         * 服务器地址
         */
        private String hosts = "localhost:9200";
        
        /**
         * 索引前缀
         */
        private String indexPrefix = "log";
        
        /**
         * 用户名
         */
        private String username;
        
        /**
         * 密码
         */
        private String password;
        
        /**
         * 连接超时时间
         */
        private int connectTimeout = 5000;
        
        /**
         * 读取超时时间
         */
        private int readTimeout = 30000;
    }
    
    @Data
    public static class Database {
        /**
         * 是否启用数据库存储
         */
        private boolean enabled = false;
        
        /**
         * 表前缀
         */
        private String tablePrefix = "log_";
        
        /**
         * 是否自动创建表
         */
        private boolean autoCreateTable = true;
    }
    
    @Data
    public static class Http {
        /**
         * 是否启用HTTP发送
         */
        private boolean enabled = false;
        
        /**
         * 访问日志端点
         */
        private String accessLogEndpoint = "http://localhost:8080/api/logs/access";
        
        /**
         * 操作日志端点
         */
        private String operationLogEndpoint = "http://localhost:8080/api/logs/operation";
        
        /**
         * 通用日志端点（用于自定义实体类）
         * 自定义实体类会发送到: {genericEndpoint}/{entityClassName}
         */
        private String genericEndpoint = "http://localhost:8080/api/logs/generic";
        
        /**
         * 连接超时时间
         */
        private int connectTimeout = 5000;
        
        /**
         * 读取超时时间
         */
        private int readTimeout = 30000;
    }
    
    @Data
    public static class Record {
        /**
         * 是否记录请求参数
         */
        private boolean recordParams = true;
        
        /**
         * 是否记录响应结果
         */
        private boolean recordResponse = false;
        
        /**
         * 是否记录异常堆栈
         */
        private boolean recordStackTrace = true;
        
        /**
         * 是否记录IP地理位置
         */
        private boolean recordIpLocation = true;
        
        /**
         * 是否记录用户代理信息
         */
        private boolean recordUserAgent = true;
        
        /**
         * 敏感字段（不记录）
         */
        private String[] sensitiveFields = {"password", "token", "secret"};
    }
}
