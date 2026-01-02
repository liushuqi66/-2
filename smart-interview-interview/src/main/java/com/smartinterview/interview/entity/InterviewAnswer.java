package com.smartinterview.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 面试答题记录实体
 */
@Data
@TableName("interview_answer")
public class InterviewAnswer {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 面试记录ID */
    private Long interviewId;

    /** 题目ID */
    private Long questionId;

    /** 候选人答案 */
    private String candidateAnswer;

    /** 得分 */
    private Integer score;

    /** AI评价反馈 */
    private String aiFeedback;

    /** 答题耗时（秒） */
    private Integer answerTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
