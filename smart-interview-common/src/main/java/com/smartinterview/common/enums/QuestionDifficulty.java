package com.smartinterview.common.enums;

import lombok.Getter;

/**
 * 题目难度枚举
 */
@Getter
public enum QuestionDifficulty {

    EASY(1, "简单"),
    MEDIUM(2, "中等"),
    HARD(3, "困难");

    private final Integer code;
    private final String desc;

    QuestionDifficulty(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
