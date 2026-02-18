package com.smartinterview.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("resume")
public class Resume {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String education;
    private String school;
    private String major;
    private Integer experienceYears;
    private String skills;
    private String summary;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updateTime;
}
