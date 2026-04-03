package com.smartinterview.ai.service.impl;

import com.smartinterview.ai.service.EvaluateService;
import com.smartinterview.common.dto.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluateServiceImpl implements EvaluateService {
    private final RedissonClient redissonClient;
    private final StringRedisTemplate redisTemplate;

    private static final List<String> DIMENSIONS = List.of("技术深度", "沟通表达", "问题分析", "系统设计", "实践经验");

    @Override
    public void evaluate(Long interviewId) {
        String lockKey = "evaluate:lock:" + interviewId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(3, 30, TimeUnit.SECONDS)) {
                log.warn("Duplicate evaluation skipped: {}", interviewId);
                return;
            }
            if (redisTemplate.opsForValue().get("evaluate:report:" + interviewId) != null) {
                log.info("Evaluation already exists: {}", interviewId);
                return;
            }

            String report = generateReport(interviewId);
            redisTemplate.opsForValue().set("evaluate:report:" + interviewId, report, 24, TimeUnit.HOURS);
            log.info("Report generated: {}", interviewId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }

    @Override
    public Result<?> getReport(Long interviewId) {
        String report = redisTemplate.opsForValue().get("evaluate:report:" + interviewId);
        if (report == null) return Result.fail(404, "评估报告未生成或已过期");
        return Result.success(report);
    }

    private String generateReport(Long interviewId) {
        Random rand = new Random();
        Map<String, Integer> scores = new LinkedHashMap<>();
        int total = 0;
        for (String dim : DIMENSIONS) { int s = 60 + rand.nextInt(36); scores.put(dim, s); total += s; }
        int avgScore = total / DIMENSIONS.size();

        StringBuilder sb = new StringBuilder();
        sb.append("{").append("\"interviewId\":").append(interviewId)
          .append(",\"totalScore\":").append(avgScore)
          .append(",\"level\":\"").append(getLevel(avgScore)).append("\"")
          .append(",\"dimensions\":{");
        boolean first = true;
        for (Map.Entry<String, Integer> e : scores.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(e.getKey()).append("\":").append(e.getValue());
            first = false;
        }
        sb.append("},\"suggestions\":[\"多加练习实际项目\",\"深入理解底层原理\",\"关注系统设计能力\"]");
        sb.append(",\"weakness\":[\"系统设计\",\"性能调优\"]");
        sb.append(",\"recommended\":[\"分布式系统设计\",\"高并发实践\",\"DDD领域驱动设计\"]}");
        return sb.toString();
    }

    private String getLevel(int score) {
        if (score >= 85) return "优秀";
        if (score >= 75) return "良好";
        if (score >= 60) return "合格";
        return "待提升";
    }
}
