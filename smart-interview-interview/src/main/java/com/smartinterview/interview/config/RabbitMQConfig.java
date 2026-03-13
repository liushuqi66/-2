package com.smartinterview.interview.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE = "interview.exchange";
    public static final String EVALUATE_QUEUE = "interview.evaluate.queue";
    public static final String EVALUATE_RK = "interview.evaluate";

    @Bean public DirectExchange interviewExchange() { return new DirectExchange(EXCHANGE); }
    @Bean public Queue evaluateQueue() { return QueueBuilder.durable(EVALUATE_QUEUE).build(); }
    @Bean public Binding evaluateBinding() { return BindingBuilder.bind(evaluateQueue()).to(interviewExchange()).with(EVALUATE_RK); }
}
