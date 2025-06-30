package com.ling.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: LingRJ
 * @Description: 文物上传响应DTO
 * @DateTime: 2025/6/30
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文物上传响应")
public class RelicsUploadResponseDTO {
    
    @Schema(description = "是否成功")
    private boolean success;
    
    @Schema(description = "响应消息")
    private String message;
    
    @Schema(description = "文物ID")
    private Long relicsId;
} 