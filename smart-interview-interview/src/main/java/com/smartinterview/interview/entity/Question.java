package com.smartinterview.interview.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data @TableName("question")
public class Question {
    @TableId(type = IdType.AUTO) private Long id;
    private String title; private String content; private String referenceAnswer;
    private Integer difficulty; private String tags; private String knowledgePoints;
    private String type; private Integer status;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updateTime;
}
