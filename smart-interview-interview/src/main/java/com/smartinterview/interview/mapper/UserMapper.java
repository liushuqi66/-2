package com.smartinterview.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartinterview.interview.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper（面试服务本地只读，用于查询面试官）
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
