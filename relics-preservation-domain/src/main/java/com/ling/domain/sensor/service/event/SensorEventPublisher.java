package com.ling.domain.sensor.service.event;

import com.ling.domain.sensor.model.valobj.SensorMessageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: LingRJ
 * @Description: 传感器事件发布器
 * @DateTime: 2025/6/30
 **/
@Service
@Slf4j
public class SensorEventPublisher {
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    public void publishSensorData(SensorMessageVO sensorData) {
        eventPublisher.publishEvent(new SensorDataEvent(this, sensorData));
    }
    
    public void publishSensorDataBatch(List<SensorMessageVO> sensorDataList) {
        sensorDataList.forEach(this::publishSensorData);
    }
} 