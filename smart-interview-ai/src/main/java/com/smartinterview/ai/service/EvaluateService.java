package com.smartinterview.ai.service;

import com.smartinterview.common.dto.Result;

/**
 * AI 评估服务接口
 */
public interface EvaluateService {

    /**
     * 评估面试（评分 + AI 反馈）
     */
    void evaluateInterview(Long interviewId);

    /**
     * 获取面试评估报告
     */
    Result<?> getEvaluationReport(Long interviewId);
}
