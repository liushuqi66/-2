package com.smartinterview.user.service.impl;

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

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final ResumeMapper resumeMapper;
    private final StringRedisTemplate redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public Result<?> register(String username, String password, String email) {
        Long cnt = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (cnt > 0) throw new BusinessException(400, "用户名已存在");
        Long emailCnt = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (emailCnt > 0) throw new BusinessException(400, "该邮箱已被注册");

        User user = new User();
        user.setUsername(username);
        // Password strength validation
        if (password.length() < 6 || password.length() > 64) {
            throw new BusinessException(400, "密码长度6-64字符");
        }
        if (!password.matches(".*[A-Z].*") && !password.matches(".*[a-z].*")) {
            throw new BusinessException(400, "密码必须包含至少一个字母");
        }
        if (!password.matches(".*\\d.*")) {
            throw new BusinessException(400, "密码必须包含至少一个数字");
        }

        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole("candidate");
        user.setStatus(1);
        userMapper.insert(user);
        log.info("User registered: {}", username);
        return Result.success("注册成功");
    }

    @Override
    public Result<?> login(String username, String password) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) throw new BusinessException(401, "用户名或密码错误");
        if (user.getStatus() == 0) throw new BusinessException(403, "账号已被禁用，请联系管理员");
        if (!passwordEncoder.matches(password, user.getPassword())) throw new BusinessException(401, "用户名或密码错误");

        String token = JwtUtil.generateToken(user.getId(), user.getUsername());
        redisTemplate.opsForValue().set("token:" + user.getId(), token, 2, TimeUnit.HOURS);
        redisTemplate.opsForValue().set("user:info:" + user.getId(), username, 30, TimeUnit.MINUTES);

        // Rate limit check (Redis)
        String rateKey = "login:rate:" + username;
        String rateCount = redisTemplate.opsForValue().get(rateKey);
        if (rateCount != null && Integer.parseInt(rateCount) >= 5) {
            throw new BusinessException(429, "登录尝试过多，请5分钟后再试");
        }
        redisTemplate.opsForValue().increment(rateKey);
        redisTemplate.expire(rateKey, 5, TimeUnit.MINUTES);

        log.info("User logged in: {}", username);
        return Result.success(token);
    }

    @Override
    public Result<User> getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(404, "用户不存在");
        user.setPassword(null); // mask password
        return Result.success(user);
    }

    @Override
    @Transactional
    public Result<?> saveResume(Long userId, Resume resume) {
        Resume existing = resumeMapper.selectOne(new LambdaQueryWrapper<Resume>().eq(Resume::getUserId, userId));
        resume.setUserId(userId);
        if (existing != null) {
            resume.setId(existing.getId());
            resumeMapper.updateById(resume);
        } else {
            resumeMapper.insert(resume);
        }
        log.info("Resume saved for user: {}", userId);
        return Result.success("简历保存成功");
    }

    @Override
    public Result<Resume> getResume(Long userId) {
        return Result.success(resumeMapper.selectOne(new LambdaQueryWrapper<Resume>().eq(Resume::getUserId, userId)));
    }
}
