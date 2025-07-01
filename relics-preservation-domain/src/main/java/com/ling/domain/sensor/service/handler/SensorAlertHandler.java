package com.ling.domain.sensor.service.handler;

import com.ling.domain.sensor.model.valobj.AlertMessageVO;
import com.ling.domain.sensor.model.valobj.SensorMessageVO;
import com.ling.domain.sensor.service.event.SensorDataEvent;
import com.ling.domain.sensor.service.message.validation.ISensorValidator;
import com.ling.domain.sensor.service.message.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @Author: LingRJ
 * @Description: 传感器告警处理器
 * @DateTime: 2025/6/30
 **/
@Component
@Slf4j
public class SensorAlertHandler {

    @EventListener
    @Order(1) // 优先级高于持久化处理
    public void handleSensorData(SensorDataEvent event) {
        SensorMessageVO data = event.getSensorData();
        if (data == null || data.getValue() == null || data.getSensorType() == null) {
            return;
        }
        
        // 检查是否异常数据
        ISensorValidator validator = ValidatorFactory.getValidator(data.getSensorType());
        if (validator != null) {
            Integer status = validator.validateStatus(data.getValue());
            data.setStatus(status);
            
            // 如果状态异常，生成告警
            if (status != null && status > 0) {
                AlertMessageVO alert = createAlert(data, status);
                processAlert(alert);
            }
        }
    }
    
    private AlertMessageVO createAlert(SensorMessageVO data, Integer severity) {
        AlertMessageVO alert = new AlertMessageVO();
        alert.setAlertId(UUID.randomUUID().toString());
        alert.setAlertType(data.getSensorType() + "_alert");
        alert.setSeverity(getSeverityLevel(severity));
        alert.setMessage(generateAlertMessage(data, severity));
        alert.setTimestamp(LocalDateTime.now());
        alert.setSensorId(data.getSensorId());
        alert.setSensorType(data.getSensorType());
        alert.setLocationId(data.getLocationId());
        alert.setRelicsId(data.getRelicsId());
        alert.setCurrentReading(data.getValue());
        
        return alert;
    }
    
    private String getSeverityLevel(Integer severity) {
        switch (severity) {
            case 1:
                return "INFO";
            case 2:
                return "WARNING";
            case 3:
                return "CRITICAL";
            default:
                return "UNKNOWN";
        }
    }
    
    private String generateAlertMessage(SensorMessageVO data, Integer severity) {
        return String.format("传感器 %s 检测到异常值: %s %s, 告警级别: %s", 
                data.getSensorId(), 
                data.getValue(), 
                data.getUnit() != null ? data.getUnit() : "", 
                getSeverityLevel(severity));
    }
    
    private void processAlert(AlertMessageVO alert) {
        // 这里可以添加告警处理逻辑，如：
        // 1. 保存到数据库
        // 2. 发送通知
        // 3. 触发其他业务流程
        
        log.warn("生成告警: [{}] {}", alert.getSeverity(), alert.getMessage());
    }
}