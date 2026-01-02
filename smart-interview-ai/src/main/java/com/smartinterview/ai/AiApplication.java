package com.smartinterview.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * AI 评估服务启动类
 */
@SpringBootApplication(scanBasePackages = "com.smartinterview")
@EnableDiscoveryClient
@EnableFeignClients
public class AiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiApplication.class, args);
    }
}
