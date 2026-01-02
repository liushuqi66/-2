package com.smartinterview.interview.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartinterview.common.dto.Result;
import com.smartinterview.common.enums.InterviewStatus;
import com.smartinterview.common.exception.BusinessException;
import com.smartinterview.common.utils.IdempotentUtil;
import com.smartinterview.interview.algorithm.SmartPaperGenerator;
import com.smartinterview.interview.entity.InterviewAnswer;
import com.smartinterview.interview.entity.InterviewRecord;
import com.smartinterview.interview.entity.Question;
import com.smartinterview.interview.mapper.InterviewAnswerMapper;
import com.smartinterview.interview.mapper.InterviewRecordMapper;
import com.smartinterview.interview.mapper.QuestionMapper;
import com.smartinterview.interview.service.InterviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 面试服务实现
 *
 * 核心设计：
 * 1. 智能组卷：基于贪心算法的知识点覆盖组卷策略
 * 2. 分布式锁：Redisson 保证组卷过程幂等
 * 3. 异步解耦：完成面试后通过 RabbitMQ 异步触发 AI 评估
 * 4. 接口幂等：基于 Redis + 唯一请求 ID 防止重复提交答案
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRecordMapper interviewRecordMapper;
    private final InterviewAnswerMapper interviewAnswerMapper;
    private final QuestionMapper questionMapper;
    private final SmartPaperGenerator paperGenerator;
    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;
    private final RabbitTemplate rabbitTemplate;

    private static final String PAPER_CACHE_KEY = "interview:paper:";
    private static final String ANSWER_IDEMPOTENT_KEY = "interview:answer:";
    private static final String INTERVIEW_LOCK_KEY = "interview:lock:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<InterviewRecord> createInterview(Long candidateId, String position,
                                                    Integer difficultyLevel, Integer questionCount) {
        // 参数校验
        if (candidateId == null) {
            throw new BusinessException(400, "候选人ID不能为空");
        }
        if (StrUtil.isBlank(position)) {
            throw new BusinessException(400, "岗位名称不能为空");
        }
        if (difficultyLevel == null || difficultyLevel < 1 || difficultyLevel > 3) {
            difficultyLevel = 2;
        }
        if (questionCount == null || questionCount < 1) {
            questionCount = 10;
        }
        if (questionCount > 50) {
            questionCount = 50;
        }

        // 分布式锁防止重复创建（带 Redis 回退方案）
        boolean locked = false;
        String lockKey = INTERVIEW_LOCK_KEY + candidateId;

        try {
            // 尝试使用 Redisson 分布式锁
            RLock lock = redissonClient.getLock(lockKey);
            locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redisson 锁获取失败，使用 Redis 简单锁回退: {}", e.getMessage());
            // 回退方案：使用 Redis setIfAbsent 作为简单分布式锁
            locked = Boolean.TRUE.equals(
                    redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS));
        }

        if (!locked) {
            throw new BusinessException("操作过于频繁，请稍后重试");
        }

        try {
            // 检查是否有进行中的面试
            Long count = interviewRecordMapper.selectCount(
                    new LambdaQueryWrapper<InterviewRecord>()
                            .eq(InterviewRecord::getCandidateId, candidateId)
                            .eq(InterviewRecord::getStatus, InterviewStatus.IN_PROGRESS.getCode()));
            if (count > 0) {
                throw new BusinessException("您有进行中的面试，请先完成或取消");
            }

            // 智能组卷
            List<Question> paper = paperGenerator.generatePaper(
                    questionCount, difficultyLevel, position);

            if (paper.isEmpty()) {
                throw new BusinessException("题库中暂无匹配的题目，请联系管理员添加题目");
            }

            // 创建面试记录
            InterviewRecord record = new InterviewRecord();
            record.setCandidateId(candidateId);
            record.setPosition(position);
            record.setDifficultyLevel(difficultyLevel);
            record.setQuestionCount(paper.size());
            record.setStatus(InterviewStatus.PENDING.getCode());
            interviewRecordMapper.insert(record);

            // 缓存试卷（Redis List 存储题目 ID）
            String cacheKey = PAPER_CACHE_KEY + record.getId();
            String[] questionIds = paper.stream()
                    .map(q -> String.valueOf(q.getId()))
                    .toArray(String[]::new);
            redisTemplate.opsForList().rightPushAll(cacheKey, questionIds);
            redisTemplate.expire(cacheKey, 2, TimeUnit.HOURS);

            log.info("面试创建成功: interviewId={}, candidateId={}, 题目数={}",
                    record.getId(), candidateId, paper.size());
            return Result.success("面试创建成功", record);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建面试失败", e);
            throw new BusinessException("创建面试失败: " + e.getMessage());
        } finally {
            // 释放锁
            try {
                RLock lock = redissonClient.getLock(lockKey);
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            } catch (Exception e) {
                // Redisson 不可用时，删除 Redis 简单锁
                redisTemplate.delete(lockKey);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<List<Question>> startInterview(Long interviewId, Long userId) {
        InterviewRecord record = interviewRecordMapper.selectById(interviewId);
        if (record == null) {
            throw new BusinessException(404, "面试记录不存在");
        }
        if (!record.getCandidateId().equals(userId)) {
            throw new BusinessException(403, "无权访问此面试");
        }

        // 更新状态为进行中
        record.setStatus(InterviewStatus.IN_PROGRESS.getCode());
        record.setStartTime(LocalDateTime.now());
        interviewRecordMapper.updateById(record);

        // 从缓存获取试卷题目
        String cacheKey = PAPER_CACHE_KEY + interviewId;
        List<String> questionIdStrs = redisTemplate.opsForList().range(cacheKey, 0, -1);
        if (questionIdStrs == null || questionIdStrs.isEmpty()) {
            throw new BusinessException("试卷已过期，请重新创建面试");
        }

        List<Long> questionIds = questionIdStrs.stream()
                .map(Long::valueOf).toList();
        List<Question> questions = questionMapper.selectBatchIds(questionIds);

        // 隐藏参考答案
        questions.forEach(q -> q.setAnswer(null));

        log.info("面试开始: interviewId={}, userId={}", interviewId, userId);
        return Result.success(questions);
    }

    @Override
    public Result<?> submitAnswer(Long interviewId, Long questionId, String answer,
                                   Integer answerTime, Long userId) {
        // 幂等性校验 - 防止重复提交
        String idempotentKey = ANSWER_IDEMPOTENT_KEY + interviewId + ":" + questionId;
        Boolean isNew = redisTemplate.opsForValue()
                .setIfAbsent(idempotentKey, "1", 10, TimeUnit.MINUTES);
        if (Boolean.FALSE.equals(isNew)) {
            throw new BusinessException("该题已提交，请勿重复提交");
        }

        // 校验面试状态
        InterviewRecord record = interviewRecordMapper.selectById(interviewId);
        if (record == null || !record.getCandidateId().equals(userId)) {
            throw new BusinessException(403, "无权操作此面试");
        }
        if (record.getStatus() != InterviewStatus.IN_PROGRESS.getCode()) {
            throw new BusinessException("面试状态异常，无法提交答案");
        }

        // 保存答案
        InterviewAnswer interviewAnswer = new InterviewAnswer();
        interviewAnswer.setInterviewId(interviewId);
        interviewAnswer.setQuestionId(questionId);
        interviewAnswer.setCandidateAnswer(answer);
        interviewAnswer.setAnswerTime(answerTime);
        interviewAnswerMapper.insert(interviewAnswer);

        log.info("答案提交成功: interviewId={}, questionId={}", interviewId, questionId);
        return Result.success("答案提交成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> finishInterview(Long interviewId, Long userId) {
        InterviewRecord record = interviewRecordMapper.selectById(interviewId);
        if (record == null || !record.getCandidateId().equals(userId)) {
            throw new BusinessException(403, "无权操作此面试");
        }

        // 更新面试状态
        record.setStatus(InterviewStatus.COMPLETED.getCode());
        record.setEndTime(LocalDateTime.now());
        interviewRecordMapper.updateById(record);

        // 异步发送 AI 评估消息到 RabbitMQ
        Map<String, Object> message = new HashMap<>();
        message.put("interviewId", interviewId);
        message.put("candidateId", userId);
        message.put("timestamp", System.currentTimeMillis());
        rabbitTemplate.convertAndSend("interview.exchange", "interview.evaluate", message);

        log.info("面试完成，已触发AI评估: interviewId={}", interviewId);
        return Result.success("面试已完成，AI评估将在后台进行");
    }

    @Override
    public Result<InterviewRecord> getInterviewDetail(Long interviewId) {
        InterviewRecord record = interviewRecordMapper.selectById(interviewId);
        if (record == null) {
            throw new BusinessException(404, "面试记录不存在");
        }
        return Result.success(record);
    }

    @Override
    public Result<List<InterviewRecord>> getInterviewList(Long userId, Integer pageNum, Integer pageSize) {
        Page<InterviewRecord> page = new Page<>(pageNum, pageSize);
        Page<InterviewRecord> result = interviewRecordMapper.selectPage(page,
                new LambdaQueryWrapper<InterviewRecord>()
                        .eq(InterviewRecord::getCandidateId, userId)
                        .orderByDesc(InterviewRecord::getCreateTime));
        return Result.success(result.getRecords());
    }

    @Override
    public Result<List<Question>> getQuestionBank(Integer difficulty, String keyword,
                                                    Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<Question>()
                .eq(Question::getStatus, 1);

        if (difficulty != null) {
            wrapper.eq(Question::getDifficulty, difficulty);
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Question::getTitle, keyword)
                    .or()
                    .like(Question::getTags, keyword);
        }
        wrapper.orderByDesc(Question::getCreateTime);

        Page<Question> page = new Page<>(pageNum, pageSize);
        Page<Question> result = questionMapper.selectPage(page, wrapper);

        // 隐藏参考答案
        result.getRecords().forEach(q -> q.setAnswer(null));

        return Result.success(result.getRecords());
    }
}
