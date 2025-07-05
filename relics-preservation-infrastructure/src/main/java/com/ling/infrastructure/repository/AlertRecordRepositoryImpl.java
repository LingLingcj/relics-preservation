package com.ling.infrastructure.repository;

import com.ling.domain.sensor.adapter.IAlertRecordRepository;
import com.ling.domain.sensor.model.valobj.AlertMessageVO;
import com.ling.domain.sensor.service.notification.model.AlertNotification;
import com.ling.infrastructure.dao.IAlertRecordDao;
import com.ling.infrastructure.dao.po.AlertRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @Author: LingRJ
 * @Description: 告警记录仓库实现
 * @DateTime: 2025/7/5
 **/
@Repository
@Slf4j
public class AlertRecordRepositoryImpl implements IAlertRecordRepository {

    @Autowired
    private IAlertRecordDao alertRecordDao;

    @Override
    public boolean saveAlertRecord(AlertMessageVO alertMessage) {
        try {
            // 检查是否已经存在活跃的告警
            if (existsActiveAlert(alertMessage.getSensorId(), alertMessage.getAlertType())) {
                log.info("已存在活跃的告警记录，不重复记录. sensorId={}, alertType={}", 
                        alertMessage.getSensorId(), alertMessage.getAlertType());
                return true;
            }
            
            // 转换为持久化对象
            AlertRecord alertRecord = convertToAlertRecord(alertMessage);
            // 生成告警ID
            alertRecord.setAlertId(generateAlertId());
            // 设置状态为活跃
            alertRecord.setStatus("ACTIVE");
            
            return alertRecordDao.insert(alertRecord) > 0;
        } catch (Exception e) {
            log.error("保存告警记录失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean saveAlertNotification(AlertNotification alertNotification) {
        try {
            // 检查是否已经存在活跃的告警
            if (existsActiveAlert(alertNotification.getSensorId(), alertNotification.getAlertType())) {
                log.info("已存在活跃的告警记录，不重复记录. sensorId={}, alertType={}", 
                        alertNotification.getSensorId(), alertNotification.getAlertType());
                return true;
            }
            
            // 转换为持久化对象
            AlertRecord alertRecord = convertToAlertRecord(alertNotification);
            // 生成告警ID
            alertRecord.setAlertId(generateAlertId());
            // 设置状态为活跃
            alertRecord.setStatus(alertNotification.getStatus() != null ? 
                    alertNotification.getStatus() : "ACTIVE");
            
            return alertRecordDao.insert(alertRecord) > 0;
        } catch (Exception e) {
            log.error("保存告警通知失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean existsActiveAlert(String sensorId, String alertType) {
        try {
            AlertRecord record = alertRecordDao.findLatestBySensorIdAndType(sensorId, alertType, "ACTIVE");
            return record != null;
        } catch (Exception e) {
            log.error("查询活跃告警失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean updateAlertStatus(String alertId, String status, LocalDateTime resolvedTime) {
        try {
            Date resolvedDate = resolvedTime != null ? 
                    Date.from(resolvedTime.atZone(ZoneId.systemDefault()).toInstant()) : null;
            return alertRecordDao.updateStatus(alertId, status, resolvedDate) > 0;
        } catch (Exception e) {
            log.error("更新告警状态失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<AlertNotification> queryAlerts(
            String sensorId, 
            String alertType, 
            String status, 
            LocalDateTime startTime, 
            LocalDateTime endTime, 
            Integer limit) {
        try {
            Date startDate = startTime != null ? 
                    Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant()) : null;
            Date endDate = endTime != null ? 
                    Date.from(endTime.atZone(ZoneId.systemDefault()).toInstant()) : null;
            
            List<AlertRecord> records = alertRecordDao.queryAlerts(
                    sensorId, alertType, status, startDate, endDate, limit);
            
            return records.stream()
                    .map(this::convertToAlertNotification)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("查询告警记录失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 生成告警ID
     * @return 告警ID
     */
    private String generateAlertId() {
        return "ALERT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
    
    /**
     * 将告警消息转换为告警记录持久化对象
     * @param alertMessage 告警消息
     * @return 告警记录持久化对象
     */
    private AlertRecord convertToAlertRecord(AlertMessageVO alertMessage) {
        AlertRecord alertRecord = new AlertRecord();
        alertRecord.setSensorId(alertMessage.getSensorId());
        alertRecord.setAlertType(alertMessage.getAlertType());
        alertRecord.setSeverity(alertMessage.getSeverity());
        alertRecord.setMessage(alertMessage.getMessage());
        alertRecord.setRelicsId(alertMessage.getRelicsId());
        alertRecord.setLocationId(alertMessage.getLocationId());
        alertRecord.setCurrentValue(alertMessage.getCurrentReading());
        alertRecord.setThreshold(alertMessage.getThreshold());
        alertRecord.setTimestamp(Date.from(alertMessage.getTimestamp().atZone(ZoneId.systemDefault()).toInstant()));
        return alertRecord;
    }
    
    /**
     * 将告警通知转换为告警记录持久化对象
     * @param alertNotification 告警通知
     * @return 告警记录持久化对象
     */
    private AlertRecord convertToAlertRecord(AlertNotification alertNotification) {
        AlertRecord alertRecord = new AlertRecord();
        alertRecord.setSensorId(alertNotification.getSensorId());
        alertRecord.setAlertType(alertNotification.getAlertType());
        alertRecord.setSeverity("WARNING"); // 默认级别
        alertRecord.setMessage(alertNotification.getMessage());
        alertRecord.setRelicsId(alertNotification.getRelicsId());
        alertRecord.setCurrentValue(alertNotification.getValue());
        alertRecord.setThreshold(alertNotification.getThreshold());
        alertRecord.setTimestamp(Date.from(alertNotification.getTimestamp().atZone(ZoneId.systemDefault()).toInstant()));
        return alertRecord;
    }
    
    /**
     * 将告警记录持久化对象转换为告警通知
     * @param alertRecord 告警记录持久化对象
     * @return 告警通知
     */
    private AlertNotification convertToAlertNotification(AlertRecord alertRecord) {
        AlertNotification notification = new AlertNotification();
        notification.setSensorId(alertRecord.getSensorId());
        notification.setAlertType(alertRecord.getAlertType());
        notification.setMessage(alertRecord.getMessage());
        notification.setRelicsId(alertRecord.getRelicsId());
        notification.setValue(alertRecord.getCurrentValue());
        notification.setThreshold(alertRecord.getThreshold());
        notification.setStatus(alertRecord.getStatus());
        notification.setTimestamp(alertRecord.getTimestamp().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());
        return notification;
    }
} 