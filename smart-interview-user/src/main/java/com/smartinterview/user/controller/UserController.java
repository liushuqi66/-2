package com.smartinterview.user.controller;

import com.smartinterview.common.dto.Result;
import com.smartinterview.user.dto.RegisterRequest;
import com.smartinterview.user.entity.Resume;
import com.smartinterview.user.entity.User;
import com.smartinterview.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器
 */
@Tag(name = "用户服务", description = "注册、登录、简历管理")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户服务根路径")
    @GetMapping
    public Result<?> root() {
        return Result.success("用户服务运行中，请使用 /api/user/info、/api/user/login 等子路径访问");
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(
                request.getUsername(),
                request.getPassword(),
                request.getEmail(),
                request.getRealName() != null ? request.getRealName() : ""
        );
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody Map<String, String> params) {
        return userService.login(params.get("username"), params.get("password"));
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<User> getInfo(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return userService.getCurrentUser(userId);
    }

    @Operation(summary = "保存/更新简历")
    @PostMapping("/resume")
    public Result<?> saveResume(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @RequestBody Resume resume) {
        return userService.saveResume(userId, resume);
    }

    @Operation(summary = "获取简历")
    @GetMapping("/resume")
    public Result<Resume> getResume(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return userService.getResume(userId);
    }

    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("用户服务运行正常");
    }
}
