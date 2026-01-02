package com.smartinterview.ai.controller;

import com.smartinterview.ai.service.EvaluateService;
import com.smartinterview.common.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * AI 评估控制器
 */
@Tag(name = "AI评估服务", description = "面试评分、评估报告")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class EvaluateController {

    private final EvaluateService evaluateService;

    @Operation(summary = "AI评估服务根路径")
    @GetMapping
    public Result<?> root() {
        return Result.success("AI评估服务运行中，请使用 /api/ai/report/{interviewId} 等子路径访问");
    }

    @Operation(summary = "获取面试评估报告")
    @GetMapping("/report/{interviewId}")
    public Result<?> getReport(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long interviewId) {
        return evaluateService.getEvaluationReport(interviewId);
    }

    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("AI评估服务运行正常");
    }
}
