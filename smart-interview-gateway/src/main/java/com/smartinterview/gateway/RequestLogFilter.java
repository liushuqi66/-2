package com.smartinterview.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关请求日志过滤器
 * 记录所有经过网关的请求路径和响应状态，方便排查问题
 */
@Slf4j
@Component
public class RequestLogFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";

        long startTime = System.currentTimeMillis();

        return chain.filter(exchange).doFinally(signalType -> {
            long elapsed = System.currentTimeMillis() - startTime;
            int status = exchange.getResponse().getStatusCode() != null
                    ? exchange.getResponse().getStatusCode().value() : 0;

            if (status >= 400) {
                log.warn("网关请求异常 [{}] {} -> HTTP {} ({}ms)", method, path, status, elapsed);
            } else if (elapsed > 3000) {
                log.warn("网关请求慢 [{}] {} -> HTTP {} ({}ms)", method, path, status, elapsed);
            } else {
                log.debug("网关请求 [{}] {} -> HTTP {} ({}ms)", method, path, status, elapsed);
            }
        });
    }

    @Override
    public int getOrder() {
        // 在 AuthGlobalFilter (-100) 之后执行
        return -50;
    }
}
