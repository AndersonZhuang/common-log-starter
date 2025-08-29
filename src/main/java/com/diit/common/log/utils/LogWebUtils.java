package com.diit.common.log.utils;

import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 日志Web工具类
 * 
 * @author diit
 */
@Slf4j
public class LogWebUtils {
    
    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST = "127.0.0.1";
    private static final String SEPARATOR = ",";
    
    private static final String HEADER_X_FORWARDED_FOR = "x-forwarded-for";
    private static final String HEADER_X_REAL_IP = "x-real-ip";
    private static final String HEADER_X_FORWARDED = "x-forwarded";
    private static final String HEADER_PROXY_CLIENT_IP = "proxy-client-ip";
    private static final String HEADER_WL_PROXY_CLIENT_IP = "wl-proxy-client-ip";
    private static final String HEADER_HTTP_CLIENT_IP = "http-client-ip";
    private static final String HEADER_HTTP_X_FORWARDED_FOR = "http-x-forwarded-for";
    
    /**
     * 获取客户端IP地址
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        
        String ip = request.getHeader(HEADER_X_FORWARDED_FOR);
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader(HEADER_X_REAL_IP);
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader(HEADER_X_FORWARDED);
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader(HEADER_PROXY_CLIENT_IP);
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader(HEADER_WL_PROXY_CLIENT_IP);
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader(HEADER_HTTP_CLIENT_IP);
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader(HEADER_HTTP_X_FORWARDED_FOR);
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 多个IP时取第一个
        if (ip != null && ip.contains(SEPARATOR)) {
            ip = ip.split(SEPARATOR)[0];
        }
        
        return LOCALHOST.equals(ip) ? LOCALHOST : ip;
    }
    
    /**
     * 获取IP地理位置
     */
    public static String getIpLocation(String ip) {
        if (ip == null || LOCALHOST.equals(ip)) {
            return "本地访问";
        }
        
        try {
            // 这里可以集成第三方IP地理位置服务
            // 目前返回IP地址作为地理位置
            return ip;
        } catch (Exception e) {
            log.warn("获取IP地理位置失败: {}", e.getMessage());
            return "未知";
        }
    }
    
    /**
     * 获取浏览器信息
     */
    public static String getBrowserInfo(String userAgentString) {
        if (userAgentString == null || userAgentString.isEmpty()) {
            return "未知";
        }
        
        try {
            UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);
            return userAgent.getBrowser().getName();
        } catch (Exception e) {
            log.warn("解析浏览器信息失败: {}", e.getMessage());
            return "未知";
        }
    }
    
    /**
     * 获取操作系统信息
     */
    public static String getOperatingSystem(String userAgentString) {
        if (userAgentString == null || userAgentString.isEmpty()) {
            return "未知";
        }
        
        try {
            UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);
            return userAgent.getOperatingSystem().getName();
        } catch (Exception e) {
            log.warn("解析操作系统信息失败: {}", e.getMessage());
            return "未知";
        }
    }
    
    /**
     * 获取设备类型
     */
    public static String getDeviceType(String userAgentString) {
        if (userAgentString == null || userAgentString.isEmpty()) {
            return "未知";
        }
        
        try {
            UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);
            return userAgent.getOperatingSystem().getDeviceType().getName();
        } catch (Exception e) {
            log.warn("解析设备类型信息失败: {}", e.getMessage());
            return "未知";
        }
    }
    
    /**
     * 从Token中获取用户名
     */
    public static String getUsernameFromToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                // 这里可以集成JWT解析逻辑
                // 目前返回null，由具体实现类处理
                return null;
            }
        } catch (Exception e) {
            log.debug("从Token获取用户名失败: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 检查是否为内网IP
     */
    public static boolean isInternalIp(String ip) {
        if (ip == null || LOCALHOST.equals(ip)) {
            return true;
        }
        
        try {
            InetAddress addr = InetAddress.getByName(ip);
            return addr.isSiteLocalAddress() || addr.isLoopbackAddress();
        } catch (UnknownHostException e) {
            log.warn("检查IP地址失败: {}", e.getMessage());
            return false;
        }
    }
}
