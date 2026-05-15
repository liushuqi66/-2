package com.smartinterview.interview.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://localhost:6379").setConnectionPoolSize(16).setRetryAttempts(3).setConnectTimeout(5000).setTimeout(3000);
        return Redisson.create(config);
    }
}
