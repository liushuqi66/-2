package com.smartinterview.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建面试请求 DTO
 */
@Data
@Schema(description = "创建面试请求")
public class CreateInterviewRequest {

    @Schema(description = "面试岗位", example = "Java后端工程师", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "岗位名称不能为空")
    @Size(max = 128, message = "岗位名称长度不能超过128个字符")
    private String position;

    @Schema(description = "难度等级: 1-简单 2-中等 3-困难", example = "2", defaultValue = "2")
    @Min(value = 1, message = "难度等级最小为1")
    @Max(value = 3, message = "难度等级最大为3")
    private Integer difficultyLevel = 2;

    @Schema(description = "题目数量", example = "5", defaultValue = "10")
    @Min(value = 1, message = "题目数量至少为1")
    @Max(value = 50, message = "题目数量最多为50")
    private Integer questionCount = 10;
}
