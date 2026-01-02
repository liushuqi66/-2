package com.smartinterview.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

/**
 * Knife4j Swagger 资源配置（WebFlux RouterFunction 方式）
 * 在 Gateway 层直接提供 /swagger-resources 和 /v3/api-docs/swagger-config 端点
 */
@Configuration
public class Knife4jSwaggerConfig {

    /**
     * /swagger-resources 返回格式（Knife4j 4.x 期望）:
     * [{"name":"服务名","url":"v3/api-docs路径","swaggerVersion":"3.0","location":"v3/api-docs路径"}]
     */
    private static final byte[] SWAGGER_RESOURCES = ("["
            + "{\"name\":\"用户服务\",\"url\":\"/user/v3/api-docs\",\"swaggerVersion\":\"3.0\",\"location\":\"/user/v3/api-docs\"},"
            + "{\"name\":\"面试服务\",\"url\":\"/interview/v3/api-docs\",\"swaggerVersion\":\"3.0\",\"location\":\"/interview/v3/api-docs\"},"
            + "{\"name\":\"AI评估服务\",\"url\":\"/ai/v3/api-docs\",\"swaggerVersion\":\"3.0\",\"location\":\"/ai/v3/api-docs\"}"
            + "]").getBytes(StandardCharsets.UTF_8);

    /**
     * /v3/api-docs/swagger-config 返回格式（Knife4j 4.x 期望）:
     * 预置多个全局 Header 参数（Authorization / Content-Type / X-Request-Id），
     * 解决网关模式下界面缺少"添加 Header"按钮的问题，用户直接填值即可。
     */
    private static final byte[] SWAGGER_CONFIG = ("{"
            + "\"configUrl\":\"/v3/api-docs/swagger-config\","
            + "\"oauth2RedirectUrl\":\"\","
            + "\"urls\":["
            + "{\"url\":\"/user/v3/api-docs\",\"name\":\"用户服务\"},"
            + "{\"url\":\"/interview/v3/api-docs\",\"name\":\"面试服务\"},"
            + "{\"url\":\"/ai/v3/api-docs\",\"name\":\"AI评估服务\"}"
            + "],"
            + "\"validatorUrl\":\"\","
            + "\"globalOperationParameters\":[{"
            + "\"name\":\"Authorization\","
            + "\"description\":\"JWT Token（格式: Bearer &lt;token&gt;）\","
            + "\"modelRef\":\"string\","
            + "\"parameterType\":\"header\","
            + "\"required\":false,"
            + "\"in\":\"header\""
            + "},{"
            + "\"name\":\"Content-Type\","
            + "\"description\":\"请求体格式\","
            + "\"modelRef\":\"string\","
            + "\"parameterType\":\"header\","
            + "\"required\":false,"
            + "\"in\":\"header\","
            + "\"example\":\"application/json\""
            + "},{"
            + "\"name\":\"X-Request-Id\","
            + "\"description\":\"请求追踪ID（可选）\","
            + "\"modelRef\":\"string\","
            + "\"parameterType\":\"header\","
            + "\"required\":false,"
            + "\"in\":\"header\""
            + "}]"
            + "}").getBytes(StandardCharsets.UTF_8);

    @Bean
    public RouterFunction<ServerResponse> knife4jSwaggerRoutes() {
        return RouterFunctions
                .route(GET("/swagger-resources"),
                        request -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(Mono.fromSupplier(() -> wrap(SWAGGER_RESOURCES)), DataBuffer.class))
                .andRoute(GET("/v3/api-docs/swagger-config"),
                        request -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(Mono.fromSupplier(() -> wrap(SWAGGER_CONFIG)), DataBuffer.class));
    }

    private static DataBuffer wrap(byte[] bytes) {
        return new DefaultDataBufferFactory().wrap(bytes);
    }
}
