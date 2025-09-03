package com.diit.common.log.exception;

/**
 * 日志发送器异常
 * 
 * @author diit
 */
public class LogSenderException extends LogException {
    
    public LogSenderException(LogResultCode resultCode, String detailMessage) {
        super(resultCode, detailMessage);
    }
    
    public LogSenderException(LogResultCode resultCode, String detailMessage, Throwable cause) {
        super(resultCode, detailMessage, cause);
    }
}