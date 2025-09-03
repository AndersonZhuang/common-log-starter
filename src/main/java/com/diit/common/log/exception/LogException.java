package com.diit.common.log.exception;

/**
 * 日志组件基础异常类
 * 
 * @author diit
 */
public class LogException extends RuntimeException {
    
    /** 错误码 */
    private final LogResultCode resultCode;
    
    /** 详细错误信息 */
    private final String detailMessage;
    
    /**
     * 构造函数
     * 
     * @param resultCode 错误码
     */
    public LogException(LogResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
        this.detailMessage = null;
    }
    
    /**
     * 构造函数
     * 
     * @param resultCode 错误码
     * @param detailMessage 详细错误信息
     */
    public LogException(LogResultCode resultCode, String detailMessage) {
        super(resultCode.getMessage() + (detailMessage != null ? ": " + detailMessage : ""));
        this.resultCode = resultCode;
        this.detailMessage = detailMessage;
    }
    
    /**
     * 构造函数
     * 
     * @param resultCode 错误码
     * @param cause 原始异常
     */
    public LogException(LogResultCode resultCode, Throwable cause) {
        super(resultCode.getMessage(), cause);
        this.resultCode = resultCode;
        this.detailMessage = cause != null ? cause.getMessage() : null;
    }
    
    /**
     * 构造函数
     * 
     * @param resultCode 错误码
     * @param detailMessage 详细错误信息
     * @param cause 原始异常
     */
    public LogException(LogResultCode resultCode, String detailMessage, Throwable cause) {
        super(resultCode.getMessage() + (detailMessage != null ? ": " + detailMessage : ""), cause);
        this.resultCode = resultCode;
        this.detailMessage = detailMessage;
    }
    
    /**
     * 获取错误码
     * 
     * @return 错误码
     */
    public LogResultCode getResultCode() {
        return resultCode;
    }
    
    /**
     * 获取错误码值
     * 
     * @return 错误码值
     */
    public int getCode() {
        return resultCode.getCode();
    }
    
    /**
     * 获取错误消息
     * 
     * @return 错误消息
     */
    public String getResultMessage() {
        return resultCode.getMessage();
    }
    
    /**
     * 获取详细错误信息
     * 
     * @return 详细错误信息
     */
    public String getDetailMessage() {
        return detailMessage;
    }
    
    @Override
    public String toString() {
        return String.format("LogException{code=%d, message='%s', detail='%s'}", 
                getCode(), getResultMessage(), detailMessage);
    }
}