package com.smartinterview.user.service;

import com.smartinterview.common.dto.Result;
import com.smartinterview.user.entity.Resume;
import com.smartinterview.user.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    Result<?> register(String username, String password, String email, String realName);

    /**
     * 用户登录
     */
    Result<String> login(String username, String password);

    /**
     * 获取当前用户信息
     */
    Result<User> getCurrentUser(Long userId);

    /**
     * 保存/更新简历
     */
    Result<?> saveResume(Long userId, Resume resume);

    /**
     * 获取简历
     */
    Result<Resume> getResume(Long userId);
}
