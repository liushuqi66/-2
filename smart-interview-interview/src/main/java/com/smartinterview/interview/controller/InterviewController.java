package com.smartinterview.interview.controller;

import com.smartinterview.common.dto.Result;
import com.smartinterview.interview.dto.CreateInterviewRequest;
import com.smartinterview.interview.entity.InterviewRecord;
import com.smartinterview.interview.entity.Question;
import com.smartinterview.interview.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 面试控制器
 */
@Tag(name = "面试服务", description = "智能组卷、答题、面试管理")
@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @Operation(summary = "创建面试（智能组卷）")
    @PostMapping("/create")
    public Result<InterviewRecord> createInterview(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateInterviewRequest request) {
        return interviewService.createInterview(userId,
                request.getPosition(),
                request.getDifficultyLevel(),
                request.getQuestionCount());
    }

    @Operation(summary = "开始面试（获取试卷）")
    @PostMapping("/start/{interviewId}")
    public Result<List<Question>> startInterview(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long interviewId) {
        return interviewService.startInterview(interviewId, userId);
    }

    @Operation(summary = "提交答案")
    @PostMapping("/answer/{interviewId}")
    public Result<?> submitAnswer(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long interviewId,
            @RequestBody Map<String, Object> params) {
        Long questionId = Long.valueOf(params.get("questionId").toString());
        String answer = (String) params.get("answer");
        Integer answerTime = (Integer) params.getOrDefault("answerTime", 0);
        return interviewService.submitAnswer(interviewId, questionId, answer, answerTime, userId);
    }

    @Operation(summary = "完成面试")
    @PostMapping("/finish/{interviewId}")
    public Result<?> finishInterview(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long interviewId) {
        return interviewService.finishInterview(interviewId, userId);
    }

    @Operation(summary = "获取面试详情")
    @GetMapping("/detail/{interviewId}")
    public Result<InterviewRecord> getInterviewDetail(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long interviewId) {
        return interviewService.getInterviewDetail(interviewId);
    }

    @Operation(summary = "我的面试列表")
    @GetMapping("/list")
    public Result<List<InterviewRecord>> getInterviewList(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return interviewService.getInterviewList(userId, pageNum, pageSize);
    }

    @Operation(summary = "查询题库")
    @GetMapping("/questions")
    public Result<List<Question>> getQuestionBank(
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return interviewService.getQuestionBank(difficulty, keyword, pageNum, pageSize);
    }

    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("面试服务运行正常");
    }
}
