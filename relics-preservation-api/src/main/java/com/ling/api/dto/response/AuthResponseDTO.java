package com.ling.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author: LingRJ
 * @Description: 认证响应DTO
 * @DateTime: 2025/6/30
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "认证响应数据")
public class AuthResponseDTO {

    @Schema(description = "JWT令牌（向后兼容）")
    private String token;

    @Schema(description = "访问令牌")
    private String accessToken;

    @Schema(description = "刷新令牌")
    private String refreshToken;

    @Schema(description = "令牌类型", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "访问令牌过期时间")
    private LocalDateTime accessTokenExpiresAt;

    @Schema(description = "刷新令牌过期时间")
    private LocalDateTime refreshTokenExpiresAt;

    @Schema(description = "访问令牌有效期（秒）")
    private Long accessTokenExpiresIn;

    @Schema(description = "刷新令牌有效期（秒）")
    private Long refreshTokenExpiresIn;

    @Schema(description = "响应消息")
    private String message;

    /**
     * 创建单Token响应（向后兼容）
     */
    public static AuthResponseDTO singleToken(String token, String message) {
        return AuthResponseDTO.builder()
                .token(token)
                .accessToken(token) // 同时设置accessToken保持一致性
                .message(message)
                .build();
    }

    /**
     * 创建双Token响应
     */
    public static AuthResponseDTO dualToken(String accessToken, String refreshToken,
                                          LocalDateTime accessTokenExpiresAt, LocalDateTime refreshTokenExpiresAt,
                                          Long accessTokenExpiresIn, Long refreshTokenExpiresIn,
                                          String message) {
        return AuthResponseDTO.builder()
                .token(accessToken) // 向后兼容
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresAt(accessTokenExpiresAt)
                .refreshTokenExpiresAt(refreshTokenExpiresAt)
                .accessTokenExpiresIn(accessTokenExpiresIn)
                .refreshTokenExpiresIn(refreshTokenExpiresIn)
                .message(message)
                .build();
    }
}