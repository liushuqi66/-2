package com.smartinterview.common.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 200; r.message = "success"; r.data = data; r.timestamp = LocalDateTime.now();
        return r;
    }
    public static <T> Result<T> success() { return success(null); }
    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> r = new Result<>();
        r.code = code; r.message = message; r.timestamp = LocalDateTime.now();
        return r;
    }
    public static <T> Result<T> fail(String message) { return fail(500, message); }
    public static <T> Result<T> unauthorized() { return fail(401, "未授权访问"); }
    public static <T> Result<T> forbidden() { return fail(403, "禁止访问"); }
}
