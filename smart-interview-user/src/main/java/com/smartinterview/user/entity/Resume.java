package com.smartinterview.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 简历实体
 */
@Data
@TableName("resume")
public class Resume {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 学历 */
    private String education;

    /** 毕业院校 */
    private String school;

    /** 专业 */
    private String major;

    /** 工作年限 */
    private Integer experienceYears;

    /** 技能标签（JSON数组） */
    private String skills;

    /** 个人简介 */
    private String summary;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
