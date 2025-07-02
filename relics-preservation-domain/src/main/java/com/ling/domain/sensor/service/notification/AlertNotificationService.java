package com.ling.domain.sensor.service.notification;

import com.ling.domain.sensor.model.valobj.AlertMessageVO;
import com.ling.domain.sensor.service.notification.model.AlertNotification;

/**
 * @Author: LingRJ
 * @Description: 传感器报警通知服务接口
 * @DateTime: 2025/7/2
 */
public interface AlertNotificationService {
    
    /**
     * 发送报警通知
     * @param alertNotification 报警通知
     */
    void sendAlertNotification(AlertNotification alertNotification);
    
    /**
     * 从报警消息转换为通知
     * @param alertMessage 报警消息
     * @return 报警通知
     */
    AlertNotification convertFromAlertMessage(AlertMessageVO alertMessage);
    
    /**
     * 判断是否需要发送通知
     * @param alertNotification 报警通知
     * @return 是否需要发送
     */
    boolean shouldSendNotification(AlertNotification alertNotification);
} 