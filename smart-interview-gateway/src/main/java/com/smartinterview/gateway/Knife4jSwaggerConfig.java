package com.smartinterview.gateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
@Configuration
public class Knife4jSwaggerConfig {
    @Bean
    public RouterFunction<ServerResponse> swaggerRouter() {
        String json = "{\\"configUrl\\":\\"/v3/api-docs/swagger-config\\",\\"urls\\":[{\\"url\\":\\"/user/v3/api-docs\\",\\"name\\":\\"用户服务\\"},{\\"url\\":\\"/interview/v3/api-docs\\",\\"name\\":\\"面试服务\\"},{\\"url\\":\\"/ai/v3/api-docs\\",\\"name\\":\\"AI评估服务\\"}]}";
        return route(GET("/v3/api-docs/swagger-config"), request ->
                ServerResponse.ok().bodyValue(json));
    }
}
