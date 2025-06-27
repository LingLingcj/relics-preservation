package com.ling.types.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;


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

    // 生成 jwtToken
    public String generateToken(Authentication auth) {
        String username = auth.getName();

        Date currentDate = new Date();
        Date expirationDate = new Date(currentDate.getTime() + jwtExpiration * 1000);

        return Jwts.builder()
                .subject(username)
                .issuedAt(currentDate)
                .expiration(expirationDate)
                .signWith(key())
                .compact();
    }

    //生成key
    private SecretKey key() {
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtSecret)
        );
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
