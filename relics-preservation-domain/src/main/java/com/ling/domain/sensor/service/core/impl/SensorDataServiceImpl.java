package com.ling.domain.sensor.service.core.impl;

import com.ling.domain.sensor.adapter.ISensorDataRepository;
import com.ling.domain.sensor.model.valobj.SensorMessageVO;
import com.ling.domain.sensor.service.core.ISensorDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: LingRJ
 * @Description: 传感器数据服务实现
 * @DateTime: 2025/6/29
 **/
@Service
@Slf4j
public class SensorDataServiceImpl implements ISensorDataService {

    @Autowired
    private ISensorDataRepository sensorDataRepository;
    
    @Override
    public boolean saveSensorData(SensorMessageVO sensorMessage, boolean isAbnormal) {
        return sensorDataRepository.saveSensorData(sensorMessage, isAbnormal);
    }

    @Override
    public int batchSaveSensorData(List<SensorMessageVO> sensorMessages) {
        return sensorDataRepository.batchSaveSensorData(sensorMessages);
    }

    @Override
    public List<SensorMessageVO> querySensorData(
            String sensorId, 
            String sensorType, 
            LocalDateTime startTime, 
            LocalDateTime endTime, 
            Integer limit) {
        return sensorDataRepository.querySensorData(sensorId, sensorType, startTime, endTime, null, limit);
    }

    @Override
    public List<SensorMessageVO> queryAbnormalData(LocalDateTime startTime, LocalDateTime endTime) {
        return sensorDataRepository.querySensorData(null, null, startTime, endTime, true, null);
    }

    @Override
    public int aggregateHourlyData(LocalDateTime hour) {
        return sensorDataRepository.aggregateHourlyData(hour);
    }

    @Override
    public int aggregateDailyData(LocalDateTime day) {
        return sensorDataRepository.aggregateDailyData(day);
    }

    @Override
    public int cleanHistoricalData(LocalDateTime beforeTime) {
        return sensorDataRepository.cleanHistoricalData(beforeTime);
    }
} 