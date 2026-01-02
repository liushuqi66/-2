package com.smartinterview.common.enums;

import lombok.Getter;

/**
 * 面试状态枚举
 */
@Getter
public enum InterviewStatus {

    /** 待开始 */
    PENDING(0, "待开始"),
    /** 进行中 */
    IN_PROGRESS(1, "进行中"),
    /** 已完成 */
    COMPLETED(2, "已完成"),
    /** 已取消 */
    CANCELLED(3, "已取消");

    private final Integer code;
    private final String desc;

    InterviewStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
