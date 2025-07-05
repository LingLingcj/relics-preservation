package com.ling.domain.sensor.service.alert;

import com.ling.domain.sensor.service.notification.model.AlertNotification;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: LingRJ
 * @Description: 告警服务接口
 * @DateTime: 2025/7/5
 **/
public interface IAlertService {
    
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
    
    /**
     * 更新告警状态
     * @param alertId 告警ID
     * @param status 告警状态
     * @return 是否更新成功
     */
    boolean updateAlertStatus(String alertId, String status);
} 