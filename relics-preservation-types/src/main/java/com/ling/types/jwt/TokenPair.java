package com.ling.types.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author: LingRJ
 * @Description: Token对，包含Access Token和Refresh Token
 * @DateTime: 2025/7/16
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenPair {
    
    /**
     * 访问令牌 - 短期有效，用于API访问
     */
    private String accessToken;
    
    /**
     * 刷新令牌 - 长期有效，用于刷新访问令牌
     */
    private String refreshToken;
    
    /**
     * 访问令牌过期时间
     */
    private LocalDateTime accessTokenExpiresAt;
    
    /**
     * 刷新令牌过期时间
     */
    private LocalDateTime refreshTokenExpiresAt;
    
    /**
     * 令牌类型，通常为 "Bearer"
     */
    @Builder.Default
    private String tokenType = "Bearer";
    
    /**
     * 访问令牌有效期（秒）
     */
    private Long accessTokenExpiresIn;
    
    /**
     * 刷新令牌有效期（秒）
     */
    private Long refreshTokenExpiresIn;
}
