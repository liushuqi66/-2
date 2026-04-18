package com.smartinterview.common.utils;

/**
 * 安全工具类 - XSS过滤、输入净化
 */
public class SecurityUtil {

    public static String sanitize(String input) {
        if (input == null) return null;
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("/", "&#x2F;");
    }

    public static String sanitizeHtml(String input) {
        if (input == null) return null;
        return input.replaceAll("<script[^>]*>.*?</script>", "")
                .replaceAll("<[^>]+>", "")
                .trim();
    }
}
