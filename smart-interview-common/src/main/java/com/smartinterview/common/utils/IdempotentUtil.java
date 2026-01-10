package com.smartinterview.common.utils;
import java.util.UUID;

public class IdempotentUtil {
    public static String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
