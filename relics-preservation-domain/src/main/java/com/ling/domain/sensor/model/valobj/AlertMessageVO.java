package com.ling.domain.sensor.model.valobj;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: LingRJ
 * @Description: 告警消息值对象
 * @DateTime: 2025/6/28
 **/
@Data
public class AlertMessageVO {
    
    /**
     * 告警ID
     */
    private String alertId;
    
    /**
     * 告警类型
     */
    private String alertType;
    
    /**
     * 告警级别：INFO、WARNING、CRITICAL
     */
    private String severity;
    
    /**
     * 告警消息
     */
    private String message;
    
    /**
     * 告警时间
     */
    private LocalDateTime timestamp;
    
    /**
     * 关联的传感器ID
     */
    private String sensorId;
    
    /**
     * 传感器类型
     */
    private String sensorType;
    
    /**
     * 位置ID
     */
    private Long locationId;
    
    /**
     * 文物ID
     */
    private Long relicsId;
    
    /**
     * 当前读数
     */
    private Double currentReading;
    
    /**
     * 阈值
     */
    private Double threshold;
    
    /**
     * 获取当前值（兼容方法，用于通知服务）
     * @return 当前读数
     */
    public Double getCurrentValue() {
        return this.currentReading;
    }
} 