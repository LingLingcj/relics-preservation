package com.ling.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: LingRJ
 * @Description: 刷新令牌请求DTO
 * @DateTime: 2025/7/16
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "刷新令牌请求")
public class RefreshTokenRequestDTO {

    @Schema(description = "刷新令牌", required = true, example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;
}
