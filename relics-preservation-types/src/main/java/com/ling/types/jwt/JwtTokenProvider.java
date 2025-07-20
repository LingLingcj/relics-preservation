package com.ling.types.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

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

    // 双Token配置
    @Value("${jwt.access-token.expiration:1800}") // 默认30分钟
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration:2592000}") // 默认30天
    private long refreshTokenExpiration;

    @Value("${jwt.dual-token.enabled:false}")
    private boolean dualTokenEnabled;

    private SecretKey secretKey;



    @PostConstruct
    public void init() {
        try {
            // 尝试使用配置的密钥
            if (jwtSecret != null && !jwtSecret.isEmpty()) {
                try {
                    // 确保密钥长度足够（至少32字符/256位）
                    if (jwtSecret.length() < 32) {
                        log.warn("配置的JWT密钥长度不足（{}字符），需要至少32字符。将生成新的安全密钥。", jwtSecret.length());
                        throw new WeakKeyException("密钥长度不足");
                    }

                    // 使用配置的密钥
                    secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
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


    /**
     * 生成 jwtToken（向后兼容）
     */
    public String generateToken(Authentication auth) {
        String username = auth.getName();

        // 获取用户的token版本
        Long tokenVersion = getUserTokenVersion(username);

        Date currentDate = new Date();
        Date expirationDate = new Date(currentDate.getTime() + jwtExpiration * 1000);

        return Jwts.builder()
                .subject(username)
                .claim("tokenVersion", tokenVersion)
                .issuedAt(currentDate)
                .expiration(expirationDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 生成包含令牌版本的JWT（用于密码修改后失效其他会话）
     */
    public String generateTokenWithVersion(Authentication auth, Long tokenVersion) {
        String username = auth.getName();

        Date currentDate = new Date();
        Date expirationDate = new Date(currentDate.getTime() + jwtExpiration * 1000);

        return Jwts.builder()
                .subject(username)
                .claim("tokenVersion", tokenVersion)
                .issuedAt(currentDate)
                .expiration(expirationDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 生成Token对（Access Token + Refresh Token）
     */
    public TokenPair generateTokenPair(Authentication auth) {
        String username = auth.getName();
        Date currentDate = new Date();

        // 获取用户的token版本
        Long tokenVersion = getUserTokenVersion(username);

        // 生成Access Token
        Date accessTokenExpirationDate = new Date(currentDate.getTime() + accessTokenExpiration * 1000);
        String accessToken = Jwts.builder()
                .subject(username)
                .claim("type", "access")
                .claim("tokenVersion", tokenVersion)
                .issuedAt(currentDate)
                .expiration(accessTokenExpirationDate)
                .signWith(secretKey)
                .compact();

        // 生成Refresh Token
        Date refreshTokenExpirationDate = new Date(currentDate.getTime() + refreshTokenExpiration * 1000);
        String refreshTokenId = UUID.randomUUID().toString();
        String refreshToken = Jwts.builder()
                .subject(username)
                .claim("type", "refresh")
                .claim("tokenVersion", tokenVersion)
                .claim("jti", refreshTokenId) // JWT ID，用于撤销
                .issuedAt(currentDate)
                .expiration(refreshTokenExpirationDate)
                .signWith(secretKey)
                .compact();

        return TokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresAt(accessTokenExpirationDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .refreshTokenExpiresAt(refreshTokenExpirationDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .accessTokenExpiresIn(accessTokenExpiration)
                .refreshTokenExpiresIn(refreshTokenExpiration)
                .build();
    }

    /**
     * 使用Refresh Token生成新的Access Token
     */
    public String generateAccessTokenFromRefresh(String refreshToken) {
        if (!validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("无效的刷新令牌");
        }

        String username = getUsername(refreshToken);
        // 获取用户当前的token版本
        Long tokenVersion = getUserTokenVersion(username);

        Date currentDate = new Date();
        Date expirationDate = new Date(currentDate.getTime() + accessTokenExpiration * 1000);

        return Jwts.builder()
                .subject(username)
                .claim("type", "access")
                .claim("tokenVersion", tokenVersion)
                .issuedAt(currentDate)
                .expiration(expirationDate)
                .signWith(secretKey)
                .compact();
    }

    //获取密钥
    private SecretKey key() {
        return secretKey;
    }

    /**
     * 统一的JWT解析方法，处理所有异常
     */
    private Claims parseTokenClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (MalformedJwtException e) {
            log.error("无效的JWT令牌: {}", e.getMessage());
            throw e;
        } catch (ExpiredJwtException e) {
            log.error("JWT token 已过期: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("JWT token 不支持: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("JWT claims 声明字符串为空: {}", e.getMessage());
            throw e;
        }
    }

    // 从jwtToken获取用户名
    public String getUsername(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject();
        } catch (Exception e) {
            log.error("从JWT令牌获取用户名失败: {}", e.getMessage());
            throw new IllegalArgumentException("无效的JWT令牌", e);
        }
    }

    /**
     * 验证Access Token
     */
    public boolean validateAccessToken(String token) {
        try {
            Claims claims = parseTokenClaims(token);
            String tokenType = claims.get("type", String.class);
            return "access".equals(tokenType) || tokenType == null; // null为向后兼容

        } catch (Exception e) {
            log.error("验证Access Token失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证Refresh Token
     */
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = parseTokenClaims(token);
            String tokenType = claims.get("type", String.class);
            return "refresh".equals(tokenType);

        } catch (Exception e) {
            log.error("验证Refresh Token失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取Token类型
     */
    public String getTokenType(String token) {
        try {
            Claims claims = parseTokenClaims(token);
            return claims.get("type", String.class);
        } catch (Exception e) {
            log.error("获取Token类型失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取Refresh Token的JTI（JWT ID）
     */
    public String getRefreshTokenId(String refreshToken) {
        try {
            Claims claims = parseTokenClaims(refreshToken);
            return claims.get("jti", String.class);
        } catch (Exception e) {
            log.error("获取Refresh Token ID失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从令牌中获取令牌版本
     */
    public Long getTokenVersion(String token) {
        try {
            Claims claims = parseTokenClaims(token);
            return claims.get("tokenVersion", Long.class);
        } catch (Exception e) {
            log.error("获取令牌版本失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查是否启用双Token模式
     */
    public boolean isDualTokenEnabled() {
        return dualTokenEnabled;
    }

    /**
     * 获取用户的令牌版本
     * 注意：这是一个简化版本，实际应用中应该从用户服务获取
     * 为了避免循环依赖，这里返回默认版本
     */
    private Long getUserTokenVersion(String username) {
        // TODO: 在实际应用中，应该通过事件或者回调机制从用户服务获取版本
        // 这里为了避免types模块依赖domain模块，返回默认版本
        log.debug("使用默认令牌版本1为用户: {}", username);
        return 1L; // 默认版本为1
    }

    /**
     * 验证令牌并检查版本
     */
    public boolean validateTokenWithVersion(String token) {
        try {
            Claims claims = parseTokenClaims(token);

            String username = claims.getSubject();
            Long tokenVersion = claims.get("tokenVersion", Long.class);

            // 获取用户当前的令牌版本
            Long currentUserTokenVersion = getUserTokenVersion(username);

            // 如果令牌版本不匹配，则认为令牌无效
            if (tokenVersion == null || !tokenVersion.equals(currentUserTokenVersion)) {
                log.warn("用户 {} 的令牌版本不匹配，令牌版本: {}, 当前版本: {}",
                        username, tokenVersion, currentUserTokenVersion);
                return false;
            }

            return true;

        } catch (MalformedJwtException e) {
            log.error("无效的JWT令牌: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token 已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token 不支持: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims 声明字符串为空: {}", e.getMessage());
        } catch (Exception e) {
            log.error("验证令牌时发生异常: {}", e.getMessage());
        }
        return false;
    }
}

