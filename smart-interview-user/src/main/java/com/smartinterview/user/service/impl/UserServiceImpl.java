package com.smartinterview.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartinterview.common.dto.Result;
import com.smartinterview.common.exception.BusinessException;
import com.smartinterview.common.utils.JwtUtil;
import com.smartinterview.user.entity.Resume;
import com.smartinterview.user.entity.User;
import com.smartinterview.user.mapper.ResumeMapper;
import com.smartinterview.user.mapper.UserMapper;
import com.smartinterview.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final ResumeMapper resumeMapper;
    private final StringRedisTemplate redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> register(String username, String password, String email, String realName) {
        // 参数校验（双重保险）
        if (StrUtil.isBlank(username)) {
            throw new BusinessException(400, "用户名不能为空");
        }
        if (StrUtil.isBlank(password) || password.length() < 6) {
            throw new BusinessException(400, "密码长度不能少于6位");
        }

        // 校验用户名唯一性
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 构建用户对象
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(StrUtil.isBlank(email) ? null : email);
        user.setRealName(StrUtil.isBlank(realName) ? null : realName);
        user.setRole(0); // 默认候选人
        user.setStatus(1);

        userMapper.insert(user);
        log.info("用户注册成功: username={}", username);
        return Result.success("注册成功");
    }

    @Override
    public Result<String> login(String username, String password) {
        // 查询用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }

        // 校验密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 生成 JWT Token
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole());
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), extraClaims);

        // Token 存入 Redis，实现分布式会话
        String redisKey = "token:" + user.getId();
        redisTemplate.opsForValue().set(redisKey, token, 2, TimeUnit.HOURS);

        log.info("用户登录成功: username={}", username);
        return Result.success("登录成功", token);
    }

    @Override
    public Result<User> getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        // 脱敏处理
        user.setPassword(null);
        return Result.success(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> saveResume(Long userId, Resume resume) {
        resume.setUserId(userId);

        // 查询是否已有简历
        Resume existing = resumeMapper.selectOne(
                new LambdaQueryWrapper<Resume>().eq(Resume::getUserId, userId));

        if (existing != null) {
            resume.setId(existing.getId());
            resumeMapper.updateById(resume);
        } else {
            resumeMapper.insert(resume);
        }

        log.info("简历保存成功: userId={}", userId);
        return Result.success("简历保存成功");
    }

    @Override
    public Result<Resume> getResume(Long userId) {
        Resume resume = resumeMapper.selectOne(
                new LambdaQueryWrapper<Resume>().eq(Resume::getUserId, userId));
        return Result.success(resume);
    }
}
