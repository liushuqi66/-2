package com.smartinterview.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Gateway WebFlux 全局异常处理器
 * 覆盖默认的 WebFlux 异常处理，避免 Sentinel 心跳等请求打印大量错误日志
 */
@Slf4j
@Order(-2)
@Configuration
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    /**
     * 静默路径：这些路径的 404 不打印 WARN 日志（Sentinel 心跳、浏览器探针等）
     */
    private static final Set<String> SILENT_PATHS = Set.of(
            "/registry/machine",
            "/favicon.ico"
    );

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod() != null
                ? exchange.getRequest().getMethod().name() : "UNKNOWN";

        int status;
        String message;

        if (ex instanceof ResponseStatusException rse) {
            response.setStatusCode(rse.getStatusCode());
            status = rse.getStatusCode().value();
            message = rse.getReason() != null ? rse.getReason() : "请求处理异常";
            boolean silent = status == 404 && SILENT_PATHS.contains(path);
            if (silent) {
                log.debug("内部路由请求 [{}] {} -> {}", method, path, status);
            } else {
                log.warn("网关响应状态异常 [{}] {} -> {} {}", method, path, status, message);
            }
        } else if (ex instanceof NotFoundException) {
            // Sentinel 心跳 /registry/machine 等未匹配路由的请求，静默返回 404
            response.setStatusCode(HttpStatus.NOT_FOUND);
            status = 404;
            message = "请求的资源不存在";
        } else if (ex instanceof ConnectException) {
            // 下游服务连接失败
            response.setStatusCode(HttpStatus.BAD_GATEWAY);
            status = 502;
            message = "下游服务不可用，请稍后重试";
            log.error("网关连接下游服务失败 [{}] {} -> {}", method, path, ex.getMessage());
        } else if (isTimeoutException(ex)) {
            // 下游服务超时
            response.setStatusCode(HttpStatus.GATEWAY_TIMEOUT);
            status = 504;
            message = "下游服务响应超时，请稍后重试";
            log.error("网关请求超时 [{}] {} -> {}", method, path, ex.getMessage());
        } else {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            status = 500;
            message = "网关服务异常，请稍后重试";
            // 打印完整堆栈以便排查
            log.error("网关内部异常 [{}] {}: {}", method, path, ex.getMessage(), ex);
        }

        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format(
                "{\"code\":%d,\"message\":\"%s\",\"data\":null,\"timestamp\":%d}",
                status, message, System.currentTimeMillis());

        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 判断是否为超时异常
     */
    private boolean isTimeoutException(Throwable ex) {
        String className = ex.getClass().getName();
        return className.contains("TimeoutException")
                || className.contains("ReadTimeoutException")
                || className.contains("ConnectTimeoutException")
                || (ex.getMessage() != null && ex.getMessage().contains("timeout"));
    }
}
