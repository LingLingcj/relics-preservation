package com.ling.domain.sensor.service.event;

import com.ling.domain.sensor.model.valobj.SensorMessageVO;
import org.springframework.context.ApplicationEvent;

/**
 * @Author: LingRJ
 * @Description: 传感器数据事件
 * @DateTime: 2025/6/30
 **/
public class SensorDataEvent extends ApplicationEvent {
    private final SensorMessageVO sensorData;
    
    public SensorDataEvent(Object source, SensorMessageVO sensorData) {
        super(source);
        this.sensorData = sensorData;
    }
    
    public SensorMessageVO getSensorData() {
        return sensorData;
    }
} 