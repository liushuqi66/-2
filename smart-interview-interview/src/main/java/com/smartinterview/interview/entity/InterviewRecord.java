package com.smartinterview.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 面试记录实体
 */
@Data
@TableName("interview_record")
public class InterviewRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 候选人ID */
    private Long candidateId;

    /** 面试官ID */
    private Long interviewerId;

    /** 面试岗位 */
    private String position;

    /** 状态：0-待开始 1-进行中 2-已完成 3-已取消 */
    private Integer status;

    /** 总分 */
    private Integer totalScore;

    /** 面试难度等级 */
    private Integer difficultyLevel;

    /** 题目数量 */
    private Integer questionCount;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
