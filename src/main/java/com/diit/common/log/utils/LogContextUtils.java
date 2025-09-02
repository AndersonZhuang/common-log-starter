package com.diit.common.log.utils;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 日志上下文工具类
 * 用于获取当前请求的上下文信息
 * 
 * @author diit
 */
public class LogContextUtils {
    
    /**
     * 获取当前用户名
     * 这里提供一个默认实现，实际项目中可以从Security上下文中获取
     * 
     * @return 用户名
     */
    public static String getCurrentUsername() {
        // TODO: 从Spring Security或其他认证框架中获取用户名
        // 示例：
        // SecurityContext context = SecurityContextHolder.getContext();
        // Authentication authentication = context.getAuthentication();
        // return authentication != null ? authentication.getName() : "anonymous";
        
        return "system"; // 默认返回系统用户
    }
    
    /**
     * 获取当前HTTP请求
     * 
     * @return HttpServletRequest
     */
    public static HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取客户端IP地址
     * 
     * @param request HTTP请求
     * @return IP地址
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 处理多个IP的情况
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
    
    /**
     * 获取用户代理字符串
     * 
     * @param request HTTP请求
     * @return 用户代理
     */
    public static String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        return request.getHeader("User-Agent");
    }
    
    /**
     * 获取请求URI
     * 
     * @param request HTTP请求
     * @return 请求URI
     */
    public static String getRequestUri(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        return request.getRequestURI();
    }
    
    /**
     * 获取请求方法
     * 
     * @param request HTTP请求
     * @return 请求方法
     */
    public static String getRequestMethod(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        return request.getMethod();
    }
}