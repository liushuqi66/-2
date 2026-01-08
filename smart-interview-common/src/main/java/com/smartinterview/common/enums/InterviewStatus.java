package com.smartinterview.common.enums;
import lombok.Getter;

@Getter
public enum InterviewStatus {
    PENDING(0, "待开始"), IN_PROGRESS(1, "进行中"), COMPLETED(2, "已完成"), CANCELLED(3, "已取消");
    private final Integer code;
    private final String desc;
    InterviewStatus(Integer code, String desc) { this.code = code; this.desc = desc; }
}
