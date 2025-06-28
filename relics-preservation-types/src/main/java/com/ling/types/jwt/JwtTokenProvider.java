package com.ling.types.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import jakarta.annotation.PostConstruct;

/**
 * @Author: LingRJ
 * @Description: jwtToken
 * @DateTime: 2025/6/27 9:49
 **/

@Slf4j
@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    private SecretKey secretKey;
    
    @PostConstruct
    public void init() {
        try {
            // 尝试使用配置的密钥
            if (jwtSecret != null && !jwtSecret.isEmpty()) {
                try {
                    // 确保密钥长度足够
                    // 如果密钥长度不够，Base64编码可以增加长度
                    String secret = jwtSecret;
                    // 至少需要32字符（256位）
                    if (secret.length() < 32) {
                        StringBuilder sb = new StringBuilder(secret);
                        while (sb.length() < 32) {
                            // 重复密钥直到长度足够
                            sb.append(secret);
                        }
                        // 取前32个字符
                        secret = sb.substring(0, 32);
                    }
                    
                    // 使用配置的密钥
                    secretKey = Keys.hmacShaKeyFor(secret.getBytes());
                    log.info("使用配置的JWT密钥");
                    return;
                } catch (WeakKeyException e) {
                    log.warn("配置的JWT密钥不安全: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("解析JWT密钥失败: {}", e.getMessage());
        }
        
        // 如果配置的密钥有问题或没有配置，生成一个新密钥
        secretKey = Jwts.SIG.HS256.key().build();
        log.info("生成了新的JWT密钥");
    }

    // 生成 jwtToken
    public String generateToken(Authentication auth) {
        String username = auth.getName();

        Date currentDate = new Date();
        Date expirationDate = new Date(currentDate.getTime() + jwtExpiration * 1000);

        return Jwts.builder()
                .subject(username)
                .issuedAt(currentDate)
                .expiration(expirationDate)
                .signWith(secretKey)
                .compact();
    }

    //获取密钥
    private SecretKey key() {
        return secretKey;
    }

    // 从jwtToken获取用户名
    public String getUsername(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String username = claims.getSubject();
        return username;
    }

    // 验证 Jwt token
    public boolean validateToken(String token){
        try{
            Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parse(token);
            return true;

        } catch (MalformedJwtException e) {
            log.error("无效的JWT令牌: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token 已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token 不支持: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims 声明字符串为空: {}", e.getMessage());
        }
        return false;
    }
}
