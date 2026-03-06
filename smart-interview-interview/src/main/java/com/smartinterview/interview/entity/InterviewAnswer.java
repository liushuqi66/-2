package com.smartinterview.interview.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal; import java.time.LocalDateTime;

@Data @TableName("interview_answer")
public class InterviewAnswer {
    @TableId(type = IdType.AUTO) private Long id;
    private Long interviewId; private Long questionId; private String answer;
    private BigDecimal score; private String feedback; private Integer questionOrder;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createTime;
}
