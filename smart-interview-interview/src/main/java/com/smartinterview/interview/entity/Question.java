package com.smartinterview.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目实体
 */
@Data
@TableName("question")
public class Question {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 题目标题 */
    private String title;

    /** 题目内容 */
    private String content;

    /** 题目类型：SINGLE_CHOICE/MULTI_CHOICE/ESSAY/CODING */
    private String type;

    /** 难度：1-简单 2-中等 3-困难 */
    private Integer difficulty;

    /** 知识点标签 */
    private String tags;

    /** 参考答案 */
    private String answer;

    /** 默认分值 */
    private Integer score;

    /** 状态 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
