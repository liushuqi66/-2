package com.smartinterview.ai.controller;

import com.smartinterview.ai.service.EvaluateService;
import com.smartinterview.common.dto.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class EvaluateController {
    private final EvaluateService evaluateService;

    @GetMapping("/report/{interviewId}")
    public Result<?> getReport(@PathVariable Long interviewId) {
        return evaluateService.getReport(interviewId);
    }

    @GetMapping("/health")
    public Result<?> health() { return Result.success("AI service OK"); }
}
