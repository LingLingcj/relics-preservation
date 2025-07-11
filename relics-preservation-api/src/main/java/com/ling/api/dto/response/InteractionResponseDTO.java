package com.ling.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 交互响应DTO
 * @Author: LingRJ
 * @Description: 用户交互操作的响应DTO
 * @DateTime: 2025/7/11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "交互响应DTO")
public class InteractionResponseDTO {
    
    @Schema(description = "操作是否成功")
    private Boolean success;
    
    @Schema(description = "响应消息")
    private String message;
    
    @Schema(description = "文物ID")
    private Long relicsId;
    
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "操作时间")
    private LocalDateTime operationTime;
    
    @Schema(description = "额外数据")
    private Object data;
    
    /**
     * 创建成功响应
     */
    public static InteractionResponseDTO success(String message, Long relicsId, String username) {
        return InteractionResponseDTO.builder()
                .success(true)
                .message(message)
                .relicsId(relicsId)
                .username(username)
                .operationTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建成功响应（带数据）
     */
    public static InteractionResponseDTO success(String message, Long relicsId, String username, Object data) {
        return InteractionResponseDTO.builder()
                .success(true)
                .message(message)
                .relicsId(relicsId)
                .username(username)
                .operationTime(LocalDateTime.now())
                .data(data)
                .build();
    }
    
    /**
     * 创建失败响应
     */
    public static InteractionResponseDTO failure(String message, Long relicsId, String username) {
        return InteractionResponseDTO.builder()
                .success(false)
                .message(message)
                .relicsId(relicsId)
                .username(username)
                .operationTime(LocalDateTime.now())
                .build();
    }
}
