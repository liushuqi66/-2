package com.smartinterview.interview.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal; import java.time.LocalDateTime;

@Data @TableName("interview_record")
public class InterviewRecord {
    @TableId(type = IdType.AUTO) private Long id;
    private Long userId; private String position; private Integer difficultyLevel;
    private Integer status; private BigDecimal score; private String paperId;
    private LocalDateTime startTime; private LocalDateTime endTime;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updateTime;
}
