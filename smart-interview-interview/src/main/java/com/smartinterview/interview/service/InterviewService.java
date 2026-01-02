package com.smartinterview.interview.service;

import com.smartinterview.common.dto.Result;
import com.smartinterview.interview.entity.InterviewAnswer;
import com.smartinterview.interview.entity.InterviewRecord;
import com.smartinterview.interview.entity.Question;

import java.util.List;

/**
 * 面试服务接口
 */
public interface InterviewService {

    /**
     * 创建面试（智能组卷）
     */
    Result<InterviewRecord> createInterview(Long candidateId, String position, Integer difficultyLevel, Integer questionCount);

    /**
     * 开始面试（返回试卷题目）
     */
    Result<List<Question>> startInterview(Long interviewId, Long userId);

    /**
     * 提交答案
     */
    Result<?> submitAnswer(Long interviewId, Long questionId, String answer, Integer answerTime, Long userId);

    /**
     * 完成面试（触发 AI 评估）
     */
    Result<?> finishInterview(Long interviewId, Long userId);

    /**
     * 获取面试记录
     */
    Result<InterviewRecord> getInterviewDetail(Long interviewId);

    /**
     * 分页查询面试记录
     */
    Result<List<InterviewRecord>> getInterviewList(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 查询题库
     */
    Result<List<Question>> getQuestionBank(Integer difficulty, String keyword, Integer pageNum, Integer pageSize);
}
