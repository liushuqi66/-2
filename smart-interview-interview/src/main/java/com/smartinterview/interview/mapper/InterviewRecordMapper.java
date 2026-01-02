package com.smartinterview.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartinterview.interview.entity.InterviewRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 面试记录 Mapper
 */
@Mapper
public interface InterviewRecordMapper extends BaseMapper<InterviewRecord> {
}
