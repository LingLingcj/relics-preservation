package com.ling.domain.sensor.model.valobj;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author: LingRJ
 * @Description: 传感器消息值对象
 * @DateTime: 2025/6/28
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorMessageVO {
    
    /**
     * 传感器ID
     */
    private String sensorId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 传感器类型
     */
    private String sensorType;
    
    /**
     * 数据值
     */
    private Double value;
    
    /**
     * 单位
     */
    private String unit;
    
    /**
     * 时间戳
     */
    private LocalDateTime timestamp;
    
    /**
     * 位置ID
     */
    private String locationId;
    
    /**
     * 文物ID
     */
    private String relicsId;
    
    /**
     * 创建基本传感器消息
     */
    public static SensorMessageVO create(String sensorId, String sensorType, Double value) {
        return SensorMessageVO.builder()
                .sensorId(sensorId)
                .sensorType(sensorType)
                .value(value)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 获取status
     */
    public void validateStatus() {
        switch (this.sensorType) {
            case "gas":
                if (this.value > 1000) {
                    this.status = 1;
                }
                break;
            case "temp":
                if (this.value > 30 || this.value < 10) {
                    this.status = 1;
                }
                break;
            case "hum":
                if (this.value > 100 || this.value < 10) {
                    this.status = 1;
                }
                break;
        }
    }
} 