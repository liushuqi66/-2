package com.smartinterview.ai.service;
import com.smartinterview.common.dto.Result;

public interface EvaluateService {
    void evaluate(Long interviewId);
    Result<?> getReport(Long interviewId);
}
