package com.ling.domain.sensor.model.valobj;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: LingRJ
 * @Description: 传感器消息值对象
 * @DateTime: 2025/6/28
 **/
@Data
public class SensorMessageVO {
    
    /**
     * 传感器ID
     */
    private String sensorId;
    
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
} 