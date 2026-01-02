package com.smartinterview.ai.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置（AI 模块）
 * 声明面试评估队列，确保 AI 服务独立启动时队列已存在
 */
@Configuration
public class RabbitMQConfig {

    public static final String INTERVIEW_EXCHANGE = "interview.exchange";
    public static final String EVALUATE_QUEUE = "interview.evaluate.queue";
    public static final String EVALUATE_ROUTING_KEY = "interview.evaluate";

    @Bean
    public DirectExchange interviewExchange() {
        return new DirectExchange(INTERVIEW_EXCHANGE, true, false);
    }

    @Bean
    public Queue evaluateQueue() {
        return QueueBuilder.durable(EVALUATE_QUEUE).build();
    }

    @Bean
    public Binding evaluateBinding() {
        return BindingBuilder.bind(evaluateQueue())
                .to(interviewExchange())
                .with(EVALUATE_ROUTING_KEY);
    }
}
