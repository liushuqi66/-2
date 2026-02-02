package com.smartinterview.gateway;

import com.smartinterview.common.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final List<String> WHITE_LIST = List.of(
            "/api/user/login", "/api/user/register",
            "/actuator/health", "/doc.html", "/webjars/",
            "/v3/api-docs", "/swagger-resources", "/swagger-ui.html", "/favicon.ico"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (WHITE_LIST.stream().anyMatch(path::startsWith)) return chain.filter(exchange);

        String token = extractToken(exchange);
        if (token == null || token.isBlank() || JwtUtil.isExpired(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        exchange = exchange.mutate()
                .request(r -> r.header("X-User-Id", String.valueOf(JwtUtil.getUserId(token)))
                               .header("X-Username", JwtUtil.getUsername(token)))
                .build();
        return chain.filter(exchange);
    }

    private String extractToken(ServerWebExchange exchange) {
        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        return (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : null;
    }

    @Override public int getOrder() { return -100; }
}
