package com.ling.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    
    @Schema(description = "JWT令牌")
    private String token;
    
    @Schema(description = "响应消息")
    private String message;
} 