package com.smartinterview.interview.controller;

import com.smartinterview.common.dto.Result;
import com.smartinterview.interview.dto.CreateInterviewRequest;
import com.smartinterview.interview.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {
    private final InterviewService interviewService;

    @PostMapping("/create")
    public Result<?> create(@RequestHeader("X-User-Id") Long userId, @Valid @RequestBody CreateInterviewRequest req) {
        return interviewService.createInterview(userId, req);
    }

    @PostMapping("/{id}/start")
    public Result<?> start(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        return interviewService.startInterview(userId, id);
    }

    @PostMapping("/{id}/answer")
    public Result<?> answer(@RequestHeader("X-User-Id") Long userId, @PathVariable("id") Long interviewId,
                             @RequestParam Long questionId, @RequestBody String answer) {
        return interviewService.submitAnswer(interviewId, questionId, answer);
    }

    @PostMapping("/{id}/complete")
    public Result<?> complete(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        return interviewService.completeInterview(userId, id);
    }

    @GetMapping("/{id}")
    public Result<?> detail(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        return interviewService.getInterviewDetail(userId, id);
    }

    @GetMapping("/list")
    public Result<?> list(@RequestHeader("X-User-Id") Long userId,
                           @RequestParam(defaultValue = "1") Integer pageNum,
                           @RequestParam(defaultValue = "10") Integer pageSize) {
        return interviewService.listInterviews(userId, pageNum, pageSize);
    }

    @GetMapping("/questions")
    public Result<?> questions(@RequestParam(required = false) Integer difficulty,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize) {
        return interviewService.queryQuestions(difficulty, keyword, pageNum, pageSize);
    }

    @GetMapping("/health")
    public Result<?> health() { return Result.success("Interview service OK"); }
}
