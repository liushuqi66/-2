package com.smartinterview.common.utils;

import java.util.UUID;

/**
 * 幂等性工具类
 * 基于唯一请求 ID 保证接口幂等
 */
public class IdempotentUtil {

    private static final String IDEMPOTENT_KEY_PREFIX = "idempotent:";

    /**
     * 生成唯一请求 ID
     */
    public static String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 构建 Redis 幂等 Key
     */
    public static String buildIdempotentKey(String requestId) {
        return IDEMPOTENT_KEY_PREFIX + requestId;
    }
}
