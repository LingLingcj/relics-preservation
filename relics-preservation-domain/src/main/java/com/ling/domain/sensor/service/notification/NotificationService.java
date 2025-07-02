package com.ling.domain.sensor.service.notification;

import com.ling.domain.sensor.model.valobj.AlertMessageVO;
import com.ling.domain.sensor.service.notification.model.AlertNotification;

/**
 * @Author: LingRJ
 * @Description: 传感器通知服务泛型接口
 * @DateTime: 2025/7/2
 * @param <T> 通知类型
 */
public interface NotificationService<T> {
    
    /**
     * 发送通知
     * @param notification 通知对象
     */
    void send(T notification);
    
    // 以下是AlertNotification专用的方法，由实现类决定是否需要实现
    
    /**
     * 发送报警通知
     * @param alertNotification 报警通知
     */
    default void sendAlertNotification(AlertNotification alertNotification) {
        throw new UnsupportedOperationException("不支持的操作");
    }
    
    /**
     * 从报警消息转换为通知
     * @param alertMessage 报警消息
     * @return 报警通知
     */
    default AlertNotification convertFromAlertMessage(AlertMessageVO alertMessage) {
        throw new UnsupportedOperationException("不支持的操作");
    }
    
    /**
     * 判断是否需要发送通知
     * @param alertNotification 报警通知
     * @return 是否需要发送
     */
    default boolean shouldSendNotification(AlertNotification alertNotification) {
        return true;
    }
} 