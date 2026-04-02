package com.smartinterview.ai.consumer;

import com.rabbitmq.client.Channel;
import com.smartinterview.ai.service.EvaluateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class InterviewEvaluateConsumer {
    private final EvaluateService evaluateService;
    private static final int MAX_RETRIES = 3;

    @RabbitListener(queues = "interview.evaluate.queue", concurrency = "3-5")
    public void handle(Long interviewId, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("Received evaluation request: interview={}", interviewId);
        try {
            evaluateService.evaluate(interviewId);
            channel.basicAck(tag, false);
            log.info("Evaluation completed: interview={}", interviewId);
        } catch (Exception e) {
            log.error("Evaluation failed: interview={}, error={}", interviewId, e.getMessage());
            channel.basicNack(tag, false, true);
        }
    }
}
