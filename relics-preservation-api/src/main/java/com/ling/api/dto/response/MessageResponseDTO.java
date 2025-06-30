package com.ling.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: LingRJ
 * @Description: 通用消息响应DTO
 * @DateTime: 2025/6/30
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通用消息响应")
public class MessageResponseDTO {
    
    @Schema(description = "响应消息")
    private String message;
} 