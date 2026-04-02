package com.smartinterview.ai.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String DLX_EXCHANGE = "interview.dlx.exchange";
    public static final String DLX_QUEUE = "interview.dlx.queue";

    @Bean public Queue deadLetterQueue() { return QueueBuilder.durable(DLX_QUEUE).build(); }
    @Bean public DirectExchange deadLetterExchange() { return new DirectExchange(DLX_EXCHANGE); }
    @Bean public Binding deadLetterBinding() { return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with("interview.evaluate.dlx"); }
}
