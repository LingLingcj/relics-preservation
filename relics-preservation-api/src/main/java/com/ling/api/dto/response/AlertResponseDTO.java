package com.ling.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author: LingRJ
 * @Description: 告警响应DTO
 * @DateTime: 2025/7/5
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponseDTO {

    private String alertId;

    private String sensorId;

    private String alertType;

    private String severity;

    private String message;

    private Long relicsId;

    private Double currentValue;

    private Double threshold;

    private String status;

    private LocalDateTime timestamp;

    private LocalDateTime resolvedTime;
} 