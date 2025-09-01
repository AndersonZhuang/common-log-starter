package com.diit.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 示例应用主类
 * 
 * @author diit
 */
@SpringBootApplication
@EnableAspectJAutoProxy
public class ExampleApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
        System.out.println("=== Common Log Starter 示例应用启动成功 ===");
        System.out.println("访问地址: http://localhost:8080");
        System.out.println("API文档: http://localhost:8080/api/users");
        System.out.println("==========================================");
    }
}
