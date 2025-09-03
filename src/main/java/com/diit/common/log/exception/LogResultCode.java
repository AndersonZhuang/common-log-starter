package com.diit.common.log.exception;

/**
 * 日志组件统一响应码枚举
 * 
 * @author diit
 */
public enum LogResultCode {
    
    // ==================== 通用响应码 ====================
    /** 操作成功 */
    SUCCESS(200, "操作成功"),
    
    /** 系统内部错误 */
    INTERNAL_ERROR(500, "系统内部错误"),
    
    /** 参数错误 */
    INVALID_PARAMETER(400, "参数错误"),
    
    /** 配置错误 */
    CONFIG_ERROR(501, "配置错误"),
    
    // ==================== 日志发送相关错误码 ====================
    /** 日志发送失败 */
    LOG_SEND_FAILED(1001, "日志发送失败"),
    
    /** 日志发送器未配置 */
    SENDER_NOT_CONFIGURED(1002, "日志发送器未配置"),
    
    /** 日志发送器初始化失败 */
    SENDER_INIT_FAILED(1003, "日志发送器初始化失败"),
    
    /** 不支持的发送器类型 */
    UNSUPPORTED_SENDER_TYPE(1004, "不支持的发送器类型"),
    
    // ==================== HTTP 发送器错误码 ====================
    /** HTTP发送器配置错误 */
    HTTP_SENDER_CONFIG_ERROR(2001, "HTTP发送器配置错误"),
    
    /** HTTP请求失败 */
    HTTP_REQUEST_FAILED(2002, "HTTP请求失败"),
    
    /** HTTP连接超时 */
    HTTP_CONNECTION_TIMEOUT(2003, "HTTP连接超时"),
    
    /** HTTP响应异常 */
    HTTP_RESPONSE_ERROR(2004, "HTTP响应异常"),
    
    // ==================== Kafka 发送器错误码 ====================
    /** Kafka发送器配置错误 */
    KAFKA_SENDER_CONFIG_ERROR(3001, "Kafka发送器配置错误"),
    
    /** Kafka连接失败 */
    KAFKA_CONNECTION_FAILED(3002, "Kafka连接失败"),
    
    /** Kafka消息发送失败 */
    KAFKA_MESSAGE_SEND_FAILED(3003, "Kafka消息发送失败"),
    
    /** Kafka序列化错误 */
    KAFKA_SERIALIZATION_ERROR(3004, "Kafka序列化错误"),
    
    // ==================== Elasticsearch 发送器错误码 ====================
    /** Elasticsearch发送器配置错误 */
    ES_SENDER_CONFIG_ERROR(4001, "Elasticsearch发送器配置错误"),
    
    /** Elasticsearch连接失败 */
    ES_CONNECTION_FAILED(4002, "Elasticsearch连接失败"),
    
    /** Elasticsearch索引创建失败 */
    ES_INDEX_CREATE_FAILED(4003, "Elasticsearch索引创建失败"),
    
    /** Elasticsearch文档插入失败 */
    ES_DOCUMENT_INSERT_FAILED(4004, "Elasticsearch文档插入失败"),
    
    // ==================== Database 发送器错误码 ====================
    /** 数据库发送器配置错误 */
    DB_SENDER_CONFIG_ERROR(5001, "数据库发送器配置错误"),
    
    /** 数据库连接失败 */
    DB_CONNECTION_FAILED(5002, "数据库连接失败"),
    
    /** 数据库表不存在 */
    DB_TABLE_NOT_EXISTS(5003, "数据库表不存在"),
    
    /** 数据库插入失败 */
    DB_INSERT_FAILED(5004, "数据库插入失败"),
    
    // ==================== 日志实体相关错误码 ====================
    /** 日志实体序列化失败 */
    ENTITY_SERIALIZATION_FAILED(6001, "日志实体序列化失败"),
    
    /** 不支持的日志实体类型 */
    UNSUPPORTED_ENTITY_TYPE(6002, "不支持的日志实体类型"),
    
    /** 日志实体字段映射错误 */
    ENTITY_FIELD_MAPPING_ERROR(6003, "日志实体字段映射错误"),
    
    // ==================== 异步处理相关错误码 ====================
    /** 异步任务执行失败 */
    ASYNC_TASK_FAILED(7001, "异步任务执行失败"),
    
    /** 线程池已满 */
    THREAD_POOL_FULL(7002, "线程池已满"),
    
    /** 批处理失败 */
    BATCH_PROCESS_FAILED(7003, "批处理失败");
    
    /** 响应码 */
    private final int code;
    
    /** 响应消息 */
    private final String message;
    
    LogResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    @Override
    public String toString() {
        return String.format("LogResultCode{code=%d, message='%s'}", code, message);
    }
}