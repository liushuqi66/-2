package com.smartinterview.interview.service;
import com.smartinterview.common.dto.Result;
import com.smartinterview.interview.dto.CreateInterviewRequest;

public interface InterviewService {
    Result<?> createInterview(Long userId, CreateInterviewRequest req);
    Result<?> startInterview(Long userId, Long interviewId);
    Result<?> submitAnswer(Long interviewId, Long questionId, String answer);
    Result<?> completeInterview(Long userId, Long interviewId);
    Result<?> getInterviewDetail(Long userId, Long interviewId);
    Result<?> listInterviews(Long userId, Integer pageNum, Integer pageSize);
    Result<?> queryQuestions(Integer difficulty, String keyword, Integer pageNum, Integer pageSize);
}
