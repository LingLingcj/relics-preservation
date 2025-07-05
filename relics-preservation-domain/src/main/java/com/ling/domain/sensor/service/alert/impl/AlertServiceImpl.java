package com.ling.domain.sensor.service.alert.impl;

import com.ling.domain.sensor.adapter.IAlertRecordRepository;
import com.ling.domain.sensor.service.alert.IAlertService;
import com.ling.domain.sensor.service.notification.model.AlertNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: LingRJ
 * @Description: 告警服务实现
 * @DateTime: 2025/7/5
 **/
@Service
@Slf4j
public class AlertServiceImpl implements IAlertService {
    
    @Autowired
    private IAlertRecordRepository alertRecordRepository;
    
    @Override
    public List<AlertNotification> queryAlerts(
            String sensorId, 
            String alertType, 
            String status, 
            LocalDateTime startTime, 
            LocalDateTime endTime, 
            Integer limit) {
        return alertRecordRepository.queryAlerts(
                sensorId, alertType, status, startTime, endTime, limit);
    }
    
    @Override
    public boolean updateAlertStatus(String alertId, String status) {
        LocalDateTime resolvedTime = null;
        if ("RESOLVED".equals(status)) {
            resolvedTime = LocalDateTime.now();
        }
        return alertRecordRepository.updateAlertStatus(alertId, status, resolvedTime);
    }
} 