package com.smartinterview.interview.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateInterviewRequest {
    @NotBlank(message = "岗位不能为空")
    private String position;
    @Min(1) @Max(3)
    private Integer difficultyLevel = 2;
    @Min(1) @Max(50)
    private Integer questionCount = 10;
}
