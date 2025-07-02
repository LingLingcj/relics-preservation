package com.ling.domain.sensor.service.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author: LingRJ
 * @Description: 传感器报警通知模型
 * @DateTime: 2025/7/2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertNotification {
    private String sensorId;
    private Long relicsId;
    // TEMPERATURE, HUMIDITY, GAS
    private String alertType;
    private String message;
    private double value;
    private double threshold;
    // ACTIVE, RESOLVED
    private String status;
    private LocalDateTime timestamp;
} 