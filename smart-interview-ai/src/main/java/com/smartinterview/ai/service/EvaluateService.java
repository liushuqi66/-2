package com.smartinterview.ai.service;
import com.smartinterview.common.dto.Result;

/**
 * AI评估服务接口 - 自动评分与报告生成
 */
public interface EvaluateService {
    void evaluate(Long interviewId);
    Result<?> getReport(Long interviewId);
}
