package com.ling.domain.sensor.adapter;

import com.ling.domain.sensor.model.valobj.AlertMessageVO;
import com.ling.domain.sensor.service.notification.model.AlertNotification;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: LingRJ
 * @Description: 告警记录仓库接口
 * @DateTime: 2025/7/5
 **/
public interface IAlertRecordRepository {
    
    /**
     * 保存告警记录
     * @param alertMessage 告警消息
     * @return 是否保存成功
     */
    boolean saveAlertRecord(AlertMessageVO alertMessage);
    
    /**
     * 保存告警通知
     * @param alertNotification 告警通知
     * @return 是否保存成功
     */
    boolean saveAlertNotification(AlertNotification alertNotification);
    
    /**
     * 查询是否存在活跃的告警
     * @param sensorId 传感器ID
     * @param alertType 告警类型
     * @return 是否存在活跃告警
     */
    boolean existsActiveAlert(String sensorId, String alertType);
    
    /**
     * 更新告警状态
     * @param alertId 告警ID
     * @param status 状态
     * @param resolvedTime 解决时间
     * @return 是否更新成功
     */
    boolean updateAlertStatus(String alertId, String status, LocalDateTime resolvedTime);
    
    /**
     * 查询告警记录
     * @param sensorId 传感器ID
     * @param alertType 告警类型
     * @param status 告警状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制条数
     * @return 告警记录列表
     */
    List<AlertNotification> queryAlerts(
            String sensorId,
            String alertType,
            String status,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer limit);
} 