package com.smartinterview.common.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类 - Token生成/解析/验证
 * 有效期: 2小时, 签名算法: HS256
 */
@Slf4j
public class JwtUtil {
    private static final String SECRET = "SmartInterviewJwtSecretKey2024VeryLongEnough";
    private static final long EXPIRE_MILLIS = 2 * 60 * 60 * 1000L;
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USERNAME = "username";

    private static SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public static String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, userId);
        claims.put(CLAIM_USERNAME, username);
        return Jwts.builder().claims(claims).issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRE_MILLIS))
                .signWith(getKey()).compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser().verifyWith(getKey()).build()
                .parseSignedClaims(token).getPayload();
    }

    public static boolean isExpired(String token) {
        try { return parseToken(token).getExpiration().before(new Date()); }
        catch (ExpiredJwtException e) { return true; }
        catch (Exception e) { log.warn("Token check failed: {}", e.getMessage()); return true; }
    }

    public static Long getUserId(String token) {
        return parseToken(token).get(CLAIM_USER_ID, Long.class);
    }

    public static String getUsername(String token) {
        return parseToken(token).get(CLAIM_USERNAME, String.class);
    }

    public static boolean validate(String token) {
        try { parseToken(token); return !isExpired(token); }
        catch (MalformedJwtException | SignatureException e) {
            log.warn("Invalid token signature"); return false;
        } catch (Exception e) { log.warn("Token err: {}", e.getMessage()); return false; }
    }
}
