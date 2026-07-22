package com.smartinterview.gateway;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel Gateway 限流配置
 * 为网关路由配置 QPS 限流规则
 */
@Slf4j
@Configuration
public class SentinelGatewayConfig {

    @PostConstruct
    public void initGatewayRules() {
        List<FlowRule> rules = new ArrayList<>();

        // === 用户服务路由限流 ===
        // 登录接口：每秒最多 10 个请求（防止暴力破解）
        rules.add(createGatewayFlowRule("user-login-api", 10));
        // 注册接口：每秒最多 5 个请求
        rules.add(createGatewayFlowRule("user-register-api", 5));
        // 用户服务全局：每秒最多 100 个请求
        rules.add(createGatewayFlowRule("smart-interview-user", 100));

        // === 面试服务路由限流 ===
        // 创建面试（智能组卷）：每秒最多 20 个请求（计算密集型）
        rules.add(createGatewayFlowRule("interview-create-api", 20));
        // 提交答案：每秒最多 50 个请求
        rules.add(createGatewayFlowRule("interview-answer-api", 50));
        // 面试服务全局：每秒最多 200 个请求
        rules.add(createGatewayFlowRule("smart-interview-interview", 200));

        // === AI 服务路由限流 ===
        // AI 评估报告：每秒最多 10 个请求（AI 调用成本高）
        rules.add(createGatewayFlowRule("ai-report-api", 10));
        // AI 服务全局：每秒最多 50 个请求
        rules.add(createGatewayFlowRule("smart-interview-ai", 50));

        FlowRuleManager.loadRules(rules);
        log.info("[Sentinel Gateway] 网关限流规则已加载，共 {} 条", rules.size());
    }

    private FlowRule createGatewayFlowRule(String resource, double qps) {
        FlowRule rule = new FlowRule();
        rule.setResource(resource);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(qps);
        return rule;
    }

    /**
     * Sentinel Gateway 过滤器 Bean（Spring Cloud 2022+ 需要显式声明）
     */
    @Bean
    public SentinelGatewayFilter sentinelGatewayFilter() {
        return new SentinelGatewayFilter();
    }

    /**
     * Gateway 限流异常处理器
     */
    @Bean
    public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler(
            List<ViewResolver> viewResolvers, ServerCodecConfigurer serverCodecConfigurer) {
        return new SentinelGatewayBlockExceptionHandler(viewResolvers, serverCodecConfigurer);
    }

    /**
     * 自定义限流响应（JSON 格式）
     */
    @PostConstruct
    public void initBlockHandler() {
        BlockRequestHandler blockRequestHandler = (exchange, t) ->
                ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue("{\"code\":429,\"message\":\"请求过于频繁，网关已限流，请稍后重试\",\"data\":null,\"timestamp\":" +
                                System.currentTimeMillis() + "}");
        GatewayCallbackManager.setBlockHandler(blockRequestHandler);
        log.info("[Sentinel Gateway] 自定义限流响应已注册");
    }
}
