package com.smartinterview.gateway;

import cn.hutool.core.util.StrUtil;
import com.smartinterview.common.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 全局鉴权过滤器
 * 在 Gateway 层统一校验 JWT Token
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    /** 白名单路径（无需鉴权） */
    private static final List<String> WHITE_LIST = List.of(
            "/",
            "/api/user/health",
            "/api/interview/health",
            "/api/ai/health",
            "/api/user/login",
            "/api/user/register",
            "/doc.html",
            "/swagger-ui.html",
            "/webjars",
            "/v3/api-docs",
            "/swagger-resources",
            "/favicon.ico",
            "/user/v3/api-docs",
            "/interview/v3/api-docs",
            "/ai/v3/api-docs"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 白名单路径直接放行
        if (isWhitePath(path)) {
            return chain.filter(exchange);
        }

        // 获取 Token
        String token = extractToken(request);
        if (StrUtil.isBlank(token)) {
            return unauthorized(exchange, "未提供认证令牌");
        }

        // 验证 Token
        try {
            if (JwtUtil.isExpired(token)) {
                return unauthorized(exchange, "认证令牌已过期");
            }
            Long userId = JwtUtil.getUserId(token);
            String username = JwtUtil.getUsername(token);

            // 将用户信息透传给下游服务
            ServerHttpRequest newRequest = request.mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .header("X-Username", username)
                    .build();
            return chain.filter(exchange.mutate().request(newRequest).build());

        } catch (Exception e) {
            log.warn("Token 校验失败: {}", e.getMessage());
            return unauthorized(exchange, "无效的认证令牌");
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    /**
     * 从请求头提取 Token
     */
    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StrUtil.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * 判断是否白名单路径
     * 白名单分为两类：精确匹配（如 /、/favicon.ico）和 前缀匹配（如 /api/user/login）
     */
    private boolean isWhitePath(String path) {
        return WHITE_LIST.stream().anyMatch(p -> p.equals(path) || (p.length() > 1 && path.startsWith(p)));
    }

    /**
     * 返回 401 未授权响应
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = String.format("{\"code\":401,\"message\":\"%s\",\"data\":null,\"timestamp\":%d}",
                message, System.currentTimeMillis());
        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
