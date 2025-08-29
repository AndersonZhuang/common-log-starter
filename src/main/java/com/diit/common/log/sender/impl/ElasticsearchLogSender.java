package com.diit.common.log.sender.impl;

import com.diit.common.log.entity.UserAccessLogEntity;
import com.diit.common.log.entity.OperationLogEntity;
import com.diit.common.log.properties.LogProperties;
import com.diit.common.log.sender.LogSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Elasticsearch日志发送器实现
 * 
 * @author diit
 */
@Slf4j
@Component
public class ElasticsearchLogSender implements LogSender {
    
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;
    
    @Autowired
    private LogProperties logProperties;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    
    @Override
    public void sendAccessLog(UserAccessLogEntity accessLog) {
        try {
            String indexName = buildIndexName("access-log");
            IndexCoordinates indexCoordinates = IndexCoordinates.of(indexName);
            
            elasticsearchOperations.save(accessLog, indexCoordinates);
            log.debug("成功保存访问日志到Elasticsearch - Index:{}", indexName);
        } catch (Exception e) {
            log.error("保存访问日志到Elasticsearch失败: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void sendOperationLog(OperationLogEntity operationLog) {
        try {
            String indexName = buildIndexName("operation-log");
            IndexCoordinates indexCoordinates = IndexCoordinates.of(indexName);
            
            elasticsearchOperations.save(operationLog, indexCoordinates);
            log.debug("成功保存操作日志到Elasticsearch - Index:{}", indexName);
        } catch (Exception e) {
            log.error("保存操作日志到Elasticsearch失败: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public boolean supports(String logType) {
        return "elasticsearch".equals(logType) && logProperties.getElasticsearch().isEnabled();
    }
    
    @Override
    public String getName() {
        return "ElasticsearchLogSender";
    }
    
    /**
     * 构建索引名称
     * 
     * @param logType 日志类型
     * @return 索引名称
     */
    private String buildIndexName(String logType) {
        String prefix = logProperties.getElasticsearch().getIndexPrefix();
        String date = LocalDateTime.now().format(DATE_FORMATTER);
        return String.format("%s-%s-%s", prefix, logType, date);
    }
}
