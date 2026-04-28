package com.smartinterview.interview.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateInterviewRequest {
    @NotBlank(message = "岗位不能为空")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9\\s()（）+\\-]{2,50}$", message = "岗位格式不正确")
    @Size(min = 2, max = 50, message = "岗位名称2-50字符")
    private String position;
    @Min(1) @Max(3)
    private Integer difficultyLevel = 2;
    @Min(1) @Max(50)
    private Integer questionCount = 10;
}
