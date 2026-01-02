package com.smartinterview.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartinterview.interview.entity.InterviewAnswer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 面试答题记录 Mapper
 */
@Mapper
public interface InterviewAnswerMapper extends BaseMapper<InterviewAnswer> {
}
