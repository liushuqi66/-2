package com.smartinterview.interview.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置
 * 用于面试完成后异步触发 AI 评估
 */
@Configuration
public class RabbitMQConfig {

    public static final String INTERVIEW_EXCHANGE = "interview.exchange";
    public static final String EVALUATE_QUEUE = "interview.evaluate.queue";
    public static final String EVALUATE_ROUTING_KEY = "interview.evaluate";

    /**
     * 声明交换机
     */
    @Bean
    public DirectExchange interviewExchange() {
        return new DirectExchange(INTERVIEW_EXCHANGE, true, false);
    }

    /**
     * 声明 AI 评估队列
     */
    @Bean
    public Queue evaluateQueue() {
        return QueueBuilder.durable(EVALUATE_QUEUE).build();
    }

    /**
     * 绑定队列到交换机
     */
    @Bean
    public Binding evaluateBinding() {
        return BindingBuilder.bind(evaluateQueue())
                .to(interviewExchange())
                .with(EVALUATE_ROUTING_KEY);
    }
}
