package com.erp.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

/**
 * JWT 工具。登录时签发管理员令牌，后续受保护接口用它鉴权。
 *
 * <p>密钥取配置里的 {@code erp.jwt.secret}（base64 编码的 64 位密钥），
 * HS256 要求密钥至少 32 字节，当前配置解码后为 48 字节，满足要求。
 */
@Component
public class JwtUtil {

    private final SecretKey key;

    /** 令牌有效期（分钟） */
    private final long expireMinutes;

    public JwtUtil(@Value("${erp.jwt.secret}") String secret,
                   @Value("${erp.jwt.expire-minutes:120}") long expireMinutes) {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret.trim()));
        this.expireMinutes = expireMinutes;
    }

    /**
     * 签发管理员令牌。
     *
     * <p>claims 里带 id / username / avatar，前端可直接解 payload 做展示，
     * 不必每次都回查后端。
     */
    public String generateAdminToken(Long adminId, String username, String avatar) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + expireMinutes * 60_000L);
        return Jwts.builder()
                .subject(String.valueOf(adminId))
                .claim("id", adminId)
                .claim("username", username)
                .claim("avatar", avatar)
                .issuedAt(now)
                .expiration(expiresAt)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 解析并校验令牌。令牌被篡改或已过期时抛 {@link io.jsonwebtoken.JwtException}。
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
