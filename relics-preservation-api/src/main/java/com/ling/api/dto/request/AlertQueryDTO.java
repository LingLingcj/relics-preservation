package com.ling.api.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: LingRJ
 * @Description: 告警查询DTO
 * @DateTime: 2025/7/5
 **/
@Data
public class AlertQueryDTO {

    private String sensorId;

    private String alertType;

    private String status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer limit = 100;
} 