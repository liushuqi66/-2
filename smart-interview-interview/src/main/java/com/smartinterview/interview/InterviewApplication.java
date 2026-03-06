package com.smartinterview.interview;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.smartinterview.interview.mapper")
public class InterviewApplication {
    public static void main(String[] args) { SpringApplication.run(InterviewApplication.class, args); }
}
