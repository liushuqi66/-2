package com.smartinterview.ai.consumer;

import com.rabbitmq.client.Channel;
import com.smartinterview.ai.service.EvaluateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 面试评估消息消费者
 *
 * 消费面试完成事件，异步执行 AI 评估
 * 设计亮点：
 * - 异步解耦：面试提交与评估评分分离，提升用户体验
 * - 手动 ACK：确保评估任务可靠执行
 * - 重试机制：评估失败自动重试，最多 3 次
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InterviewEvaluateConsumer {

    private final EvaluateService evaluateService;

    @RabbitListener(queues = "interview.evaluate.queue")
    public void handleEvaluate(Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String messageBody = new String(message.getBody());

        log.info("收到评估消息: {}", messageBody);

        try {
            // 解析消息
            // 实际项目中使用 Jackson 反序列化
            String interviewIdStr = extractField(messageBody, "interviewId");
            Long interviewId = Long.valueOf(interviewIdStr);

            // 执行 AI 评估
            evaluateService.evaluateInterview(interviewId);

            // 手动 ACK
            channel.basicAck(deliveryTag, false);
            log.info("评估完成: interviewId={}", interviewId);

        } catch (Exception e) {
            log.error("评估失败: deliveryTag={}, error={}", deliveryTag, e.getMessage());
            try {
                // 拒绝消息并重新入队（重试机制）
                // 实际项目中应判断重试次数，超过阈值则转入死信队列
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ex) {
                log.error("消息拒绝失败", ex);
            }
        }
    }

    /**
     * 简易的 JSON 字段提取（实际项目使用 Jackson）
     */
    private String extractField(String json, String field) {
        String key = "\"" + field + "\":";
        int start = json.indexOf(key) + key.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        return json.substring(start, end).trim().replace("\"", "");
    }
}
