package com.smartinterview.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartinterview.common.dto.Result;
import com.smartinterview.common.enums.InterviewStatus;
import com.smartinterview.common.exception.BusinessException;
import com.smartinterview.interview.algorithm.SmartPaperGenerator;
import com.smartinterview.interview.config.RabbitMQConfig;
import com.smartinterview.interview.dto.CreateInterviewRequest;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {
    private final QuestionMapper questionMapper;
    private final InterviewRecordMapper recordMapper;
    private final InterviewAnswerMapper answerMapper;
    private final SmartPaperGenerator paperGenerator;
    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public Result<?> createInterview(Long userId, CreateInterviewRequest req) {
        String lockKey = "lock:interview:create:" + userId;
        boolean locked = false;
        RLock lock = redissonClient.getLock(lockKey);
        try { locked = lock.tryLock(1, 10, TimeUnit.SECONDS); }
        catch (Exception e) {
            log.warn("Redisson lock fail, fallback to Redis: {}", e.getMessage());
            locked = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS));
        }
        if (!locked) throw new BusinessException("操作频繁，请稍后重试");

        try {
            Long inProgress = recordMapper.selectCount(new LambdaQueryWrapper<InterviewRecord>()
                    .eq(InterviewRecord::getUserId, userId).eq(InterviewRecord::getStatus, InterviewStatus.IN_PROGRESS.getCode()));
            if (inProgress > 0) throw new BusinessException("有进行中的面试，请先完成");

            List<Question> pool = questionMapper.selectList(new LambdaQueryWrapper<Question>().eq(Question::getStatus, 1));
            if (pool.size() < req.getQuestionCount()) throw new BusinessException("题库数量不足");

            List<Question> paper = paperGenerator.generate(pool, req.getQuestionCount(), req.getDifficultyLevel());

            InterviewRecord record = new InterviewRecord();
            record.setUserId(userId);
            record.setPosition(req.getPosition());
            record.setDifficultyLevel(req.getDifficultyLevel());
            record.setStatus(InterviewStatus.PENDING.getCode());
            record.setPaperId(UUID.randomUUID().toString().substring(0, 8));
            recordMapper.insert(record);

            // Cache paper in Redis
            for (int i = 0; i < paper.size(); i++) {
                Question q = paper.get(i);
                String base = "paper:" + record.getId() + ":q:" + q.getId();
                redisTemplate.opsForValue().set(base + ":title", q.getTitle(), 2, TimeUnit.HOURS);
                redisTemplate.opsForValue().set(base + ":content", q.getContent(), 2, TimeUnit.HOURS);
                redisTemplate.opsForValue().set(base + ":difficulty", String.valueOf(q.getDifficulty()), 2, TimeUnit.HOURS);
                redisTemplate.opsForValue().set(base + ":order", String.valueOf(i + 1), 2, TimeUnit.HOURS);
            }
            redisTemplate.opsForValue().set("paper:" + record.getId() + ":count", String.valueOf(paper.size()), 2, TimeUnit.HOURS);

            log.info("Interview created: id={}, userId={}, paper={}, qCount={}", record.getId(), userId, record.getPaperId(), paper.size());
            return Result.success(record);
        } finally {
            try { if (lock.isHeldByCurrentThread()) lock.unlock(); }
            catch (Exception e) { redisTemplate.delete(lockKey); }
        }
    }

    @Override
    public Result<?> startInterview(Long userId, Long interviewId) {
        InterviewRecord record = recordMapper.selectById(interviewId);
        if (record == null || !record.getUserId().equals(userId)) throw new BusinessException("面试记录不存在");
        if (record.getStatus() != InterviewStatus.PENDING.getCode()) throw new BusinessException("面试状态不允许开始");
        record.setStatus(InterviewStatus.IN_PROGRESS.getCode());
        record.setStartTime(LocalDateTime.now());
        recordMapper.updateById(record);
        return Result.success("面试已开始");
    }

    @Override
    public Result<?> submitAnswer(Long interviewId, Long questionId, String answer) {
        InterviewRecord record = recordMapper.selectById(interviewId);
        if (record == null || record.getStatus() != InterviewStatus.IN_PROGRESS.getCode())
            throw new BusinessException("面试状态不正确");

        String ikey = "answer:" + interviewId + ":" + questionId;
        if (Boolean.FALSE.equals(redisTemplate.opsForValue().setIfAbsent(ikey, "1", 10, TimeUnit.MINUTES)))
            throw new BusinessException("该题已提交过答案");

        InterviewAnswer ans = new InterviewAnswer();
        ans.setInterviewId(interviewId); ans.setQuestionId(questionId); ans.setAnswer(answer);
        answerMapper.insert(ans);
        return Result.success("答案提交成功");
    }

    @Override
    public Result<?> completeInterview(Long userId, Long interviewId) {
        InterviewRecord record = recordMapper.selectById(interviewId);
        if (record == null || !record.getUserId().equals(userId)) throw new BusinessException("面试记录不存在");
        if (record.getStatus() != InterviewStatus.IN_PROGRESS.getCode()) throw new BusinessException("状态不正确");

        record.setStatus(InterviewStatus.COMPLETED.getCode());
        record.setEndTime(LocalDateTime.now());
        recordMapper.updateById(record);

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.EVALUATE_RK, interviewId);
        log.info("Evaluation message sent for interview: {}", interviewId);
        return Result.success("面试已完成，AI评估中...");
    }

    @Override
    public Result<?> getInterviewDetail(Long userId, Long interviewId) {
        InterviewRecord record = recordMapper.selectById(interviewId);
        if (record == null || !record.getUserId().equals(userId)) throw new BusinessException("不存在");
        List<InterviewAnswer> answers = answerMapper.selectList(
                new LambdaQueryWrapper<InterviewAnswer>().eq(InterviewAnswer::getInterviewId, interviewId));
        return Result.success(Map.of("record", record, "answers", answers));
    }

    @Override
    public Result<?> listInterviews(Long userId, Integer pageNum, Integer pageSize) {
        Page<InterviewRecord> page = new Page<>(pageNum, pageSize);
        IPage<InterviewRecord> result = recordMapper.selectPage(page,
                new LambdaQueryWrapper<InterviewRecord>().eq(InterviewRecord::getUserId, userId)
                        .orderByDesc(InterviewRecord::getCreateTime));
        return Result.success(result);
    }

    @Override
    public Result<?> queryQuestions(Integer difficulty, String keyword, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Question> w = new LambdaQueryWrapper<Question>().eq(Question::getStatus, 1);
        if (difficulty != null) w.eq(Question::getDifficulty, difficulty);
        if (keyword != null && !keyword.isBlank()) w.and(w2 -> w2.like(Question::getTitle, keyword).or().like(Question::getTags, keyword));
        Page<Question> page = new Page<>(pageNum, pageSize);
        IPage<Question> result = questionMapper.selectPage(page, w);
        result.getRecords().forEach(q -> q.setReferenceAnswer(null));
        return Result.success(result);
    }
}
