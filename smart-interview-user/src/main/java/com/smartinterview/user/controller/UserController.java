package com.smartinterview.user.controller;

import com.smartinterview.common.dto.Result;
import com.smartinterview.user.dto.LoginRequest;
import com.smartinterview.user.dto.RegisterRequest;
import com.smartinterview.user.entity.Resume;
import com.smartinterview.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterRequest req) {
        return userService.register(req.getUsername(), req.getPassword(), req.getEmail());
    }

    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginRequest req) {
        return userService.login(req.getUsername(), req.getPassword());
    }

    @GetMapping("/info")
    public Result<?> info(@RequestHeader("X-User-Id") Long userId) {
        return userService.getUserInfo(userId);
    }

    @PostMapping("/resume")
    public Result<?> saveResume(@RequestHeader("X-User-Id") Long userId, @RequestBody Resume resume) {
        return userService.saveResume(userId, resume);
    }

    @GetMapping("/resume")
    public Result<?> getResume(@RequestHeader("X-User-Id") Long userId) {
        return userService.getResume(userId);
    }

    @GetMapping("/health")
    public Result<?> health() { return Result.success("User service OK"); }
}
