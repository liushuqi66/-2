package com.smartinterview.interview.service;
import com.smartinterview.common.dto.Result;
import com.smartinterview.interview.dto.CreateInterviewRequest;

/**
 * 面试服务接口 - 提供面试全生命周期管理
 * 包括：创建面试、组卷、答题提交、评估触发
 */
public interface InterviewService {
    Result<?> createInterview(Long userId, CreateInterviewRequest req);
    Result<?> startInterview(Long userId, Long interviewId);
    Result<?> submitAnswer(Long interviewId, Long questionId, String answer);
    Result<?> completeInterview(Long userId, Long interviewId);
    Result<?> getInterviewDetail(Long userId, Long interviewId);
    Result<?> listInterviews(Long userId, Integer pageNum, Integer pageSize);
    Result<?> queryQuestions(Integer difficulty, String keyword, Integer pageNum, Integer pageSize);
}
