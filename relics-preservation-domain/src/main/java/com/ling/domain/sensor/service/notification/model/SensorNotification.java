package com.ling.domain.sensor.service.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @Author: LingRJ
 * @Description: 传感器消息
 * @DateTime: 2025/7/2 15:04
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorNotification {
    private Long locationId;
    private String sensorType;
    private Double value;
    private LocalDateTime timestamp;
    // 可能需要的额外数据
    private Map<String, Object> additionalData;
}
