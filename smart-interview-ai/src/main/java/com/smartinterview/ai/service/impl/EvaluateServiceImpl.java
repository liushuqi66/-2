package com.smartinterview.ai.service.impl;

import com.smartinterview.ai.service.EvaluateService;
import com.smartinterview.common.dto.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AI 评估服务实现
 *
 * 评估策略：
 * 1. 基于规则引擎的自动评分（关键词匹配 + 答案长度评估）
 * 2. 知识点覆盖分析
 * 3. 答题耗时分析
 * 4. 综合能力评级与改进建议
 *
 * 设计亮点：
 * - 分布式锁保证评估任务不重复执行
 * - 评估结果缓存到 Redis，支持快速查询
 * - 预留 Spring AI 集成接口，支持接入大模型评估
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluateServiceImpl implements EvaluateService {

    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;

    private static final String EVALUATE_LOCK_KEY = "evaluate:lock:";
    private static final String EVALUATE_RESULT_KEY = "evaluate:result:";

    @Override
    public void evaluateInterview(Long interviewId) {
        // 分布式锁防止重复评估
        RLock lock = redissonClient.getLock(EVALUATE_LOCK_KEY + interviewId);
        try {
            if (!lock.tryLock(3, 30, TimeUnit.SECONDS)) {
                log.warn("评估任务已在执行中: interviewId={}", interviewId);
                return;
            }

            log.info("开始AI评估: interviewId={}", interviewId);

            // === 模拟 AI 评估过程 ===
            // 实际项目中可接入 Spring AI 或调用大模型 API

            // 1. 模拟评分计算
            int totalScore = calculateMockScore(interviewId);

            // 2. 生成能力分析
            Map<String, Object> report = generateMockReport(interviewId, totalScore);

            // 3. 评估结果缓存到 Redis
            redisTemplate.opsForHash().putAll(EVALUATE_RESULT_KEY + interviewId,
                    toStringMap(report));
            redisTemplate.expire(EVALUATE_RESULT_KEY + interviewId, 24, TimeUnit.HOURS);

            log.info("AI评估完成: interviewId={}, totalScore={}", interviewId, totalScore);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("评估任务被中断", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public Result<?> getEvaluationReport(Long interviewId) {
        Map<Object, Object> report = redisTemplate.opsForHash()
                .entries(EVALUATE_RESULT_KEY + interviewId);

        if (report.isEmpty()) {
            return Result.fail(404, "评估报告尚未生成，请稍后再试");
        }

        Map<String, Object> result = new HashMap<>();
        report.forEach((k, v) -> result.put(k.toString(), v));
        return Result.success(result);
    }

    /**
     * 模拟评分计算
     * 实际项目中根据：答案与参考答案的语义相似度、关键词覆盖率、答案完整度等
     */
    private int calculateMockScore(Long interviewId) {
        // 模拟分数：60-95 分之间
        return 60 + (int) (Math.random() * 36);
    }

    /**
     * 模拟生成评估报告
     */
    private Map<String, Object> generateMockReport(Long interviewId, int totalScore) {
        Map<String, Object> report = new HashMap<>();
        report.put("interviewId", interviewId);
        report.put("totalScore", totalScore);

        // 评级
        String level;
        if (totalScore >= 90) level = "优秀";
        else if (totalScore >= 80) level = "良好";
        else if (totalScore >= 70) level = "中等";
        else level = "待提升";
        report.put("level", level);

        // 各维度评分
        report.put("basicKnowledge", 60 + (int)(Math.random() * 40));
        report.put("systemDesign", 60 + (int)(Math.random() * 40));
        report.put("problemSolving", 60 + (int)(Math.random() * 40));
        report.put("codeQuality", 60 + (int)(Math.random() * 40));

        // 改进建议
        report.put("suggestion", generateSuggestion(totalScore));

        // 知识点薄弱环节
        report.put("weakPoints", "分布式系统设计、高并发场景优化");

        // 推荐学习方向
        report.put("recommendDirection", "建议深入学习 Spring Cloud 微服务体系、消息队列高级特性、系统性能调优");

        return report;
    }

    /**
     * 根据分数生成改进建议
     */
    private String generateSuggestion(int score) {
        if (score >= 90) {
            return "基础扎实，建议向架构设计方向发展，深入学习分布式系统设计模式";
        } else if (score >= 80) {
            return "整体表现良好，建议加强系统设计能力和高并发场景实战经验";
        } else if (score >= 70) {
            return "基础需要加强，建议系统学习 Java 核心知识、数据库优化和常用中间件";
        } else {
            return "建议从基础开始系统学习，重点掌握 Java 基础、数据结构和算法";
        }
    }

    /**
     * 将 Map 转为全 String 类型的 Map（用于 Redis Hash 存储）
     */
    private Map<String, String> toStringMap(Map<String, Object> map) {
        Map<String, String> result = new HashMap<>();
        map.forEach((k, v) -> result.put(k, String.valueOf(v)));
        return result;
    }
}
