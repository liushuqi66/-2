package com.smartinterview.common.exception;

import com.smartinterview.common.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusiness(BusinessException e) {
        log.warn("Business: code={}, msg={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.NOT_FOUND)
    public Result<?> handle404(NoResourceFoundException e) {
        return Result.fail(404, "资源不存在");
    }

    @ExceptionHandler(BindException.class)
    public Result<?> handleValidation(BindException e) {
        FieldError fe = e.getFieldError();
        String msg = fe != null ? fe.getDefaultMessage() : "参数校验失败";
        return Result.fail(400, msg);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public Result<?> handleMissingHeader(MissingRequestHeaderException e) {
        return Result.fail(400, "缺少必要请求头: " + e.getHeaderName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<?> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return Result.fail(400, "参数类型不匹配: " + e.getName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<?> handleBadJson(HttpMessageNotReadableException e) {
        return Result.fail(400, "请求体解析失败，请检查JSON格式");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<?> handleMissingParam(MissingServletRequestParameterException e) {
        return Result.fail(400, "缺少必要参数: " + e.getParameterName());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<?> handleDataConflict(DataIntegrityViolationException e) {
        log.warn("Data conflict: {}", e.getMessage());
        return Result.fail(409, "数据冲突，请检查输入");
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleUnknown(Exception e) {
        log.error("Unexpected error", e);
        return Result.fail(500, "服务器内部错误");
    }
}
