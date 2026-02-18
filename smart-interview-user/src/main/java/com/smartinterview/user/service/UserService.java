package com.smartinterview.user.service;

import com.smartinterview.common.dto.Result;
import com.smartinterview.user.entity.Resume;
import com.smartinterview.user.entity.User;

public interface UserService {
    Result<?> register(String username, String password, String email);
    Result<?> login(String username, String password);
    Result<User> getUserInfo(Long userId);
    Result<?> saveResume(Long userId, Resume resume);
    Result<Resume> getResume(Long userId);
}
