package com.ling.domain.sensor.service.pipeline;

import com.ling.domain.sensor.model.valobj.AlertMessageVO;
import com.ling.domain.sensor.model.valobj.SensorMessageVO;
import com.ling.domain.sensor.service.core.ISensorDataService;
import com.ling.domain.sensor.service.message.validation.ISensorValidator;
import com.ling.domain.sensor.service.message.validation.ValidatorFactory;
import com.ling.domain.sensor.service.notification.NotificationService;
import com.ling.domain.sensor.service.notification.model.AlertNotification;
import com.ling.domain.sensor.service.notification.impl.WebSocketAlertNotificationService;
import com.ling.domain.sensor.service.notification.model.SensorNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author: LingRJ
 * @Description: 传感器数据处理管道
 * @DateTime: 2025/7/3
 **/
@Component
@Slf4j
public class SensorDataPipeline {
    
    @Autowired
    private ISensorDataService sensorDataService;
    
    @Autowired
    @Qualifier("webSocketAlertNotificationService")
    private NotificationService<AlertNotification> alertNotificationService;
    
    @Autowired
    @Qualifier("webSocketSensorDataService")
    private NotificationService<SensorNotification> sensorDataNotificationService;
    
    private final ConcurrentLinkedQueue<SensorMessageVO> dataQueue = new ConcurrentLinkedQueue<>();
    
    private final ExecutorService processorPool = Executors.newFixedThreadPool(2);
    
    @Value("${sensor.data.batch.size:100}")
    private int batchSize;
    
    @Value("${sensor.data.batch.interval:30000}")
    private long batchIntervalMs;

    /**
     * 处理传感器数据流水线入口
     * @param messages 传感器消息列表
     */
    public void process(List<SensorMessageVO> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        
        // 异步处理数据
        processorPool.submit(() -> {
            for (SensorMessageVO message : messages) {
                // 1. 验证数据并设置状态
                validateAndEnrichData(message);
                
                // 2. 处理告警
                if (message.getStatus() != null && message.getStatus() > 0) {
                    processAlert(message);
                }
                
                // 3. 发送WebSocket传感器数据通知
                sendSensorDataNotification(message);
                
                // 4. 将数据放入持久化队列
                dataQueue.offer(message);
            }
            
            // 5. 达到批次大小时立即处理
            if (dataQueue.size() >= batchSize) {
                processBatch();
            }
        });
    }
    
    /**
     * 验证数据并丰富元数据
     */
    private void validateAndEnrichData(SensorMessageVO message) {
        if (message.getTimestamp() == null) {
            message.setTimestamp(LocalDateTime.now());
        }
        
        if (message.getSensorType() != null) {
            ISensorValidator validator = ValidatorFactory.getValidator(message.getSensorType());
            if (validator != null) {
                Integer status = validator.validateStatus(message.getValue());
                message.setStatus(status);
                log.debug("传感器类型：{}，传感器值：{}，传感器状态：{}", 
                        message.getSensorType(), message.getValue(), message.getStatus());
            }
        }
    }
    
    /**
     * 处理告警逻辑
     */
    private void processAlert(SensorMessageVO data) {
        AlertMessageVO alert = createAlert(data, data.getStatus());
        log.warn("生成告警: [{}] {}", alert.getSeverity(), alert.getMessage());
        
        // 处理告警
        // 1. 转换告警消息为通知
        AlertNotification notification = ((WebSocketAlertNotificationService)alertNotificationService).convertFromAlertMessage(alert);
        
        // 2. 发送WebSocket通知
        alertNotificationService.send(notification);
    }
    
    /**
     * 发送传感器数据通知到WebSocket
     */
    private void sendSensorDataNotification(SensorMessageVO data) {
        SensorNotification notification = new SensorNotification(
                data.getLocationId(),
                data.getSensorType(),
                data.getValue(),
                data.getTimestamp(),
                null
        );
        
        sensorDataNotificationService.send(notification);
    }
    
    /**
     * 创建告警对象
     */
    private AlertMessageVO createAlert(SensorMessageVO data, Integer severity) {
        AlertMessageVO alert = new AlertMessageVO();
        alert.setAlertId(UUID.randomUUID().toString());
        alert.setAlertType(data.getSensorType() + "_alert");
//        alert.setSeverity(data.getStatus());
        alert.setMessage(generateAlertMessage(data, severity));
        alert.setTimestamp(LocalDateTime.now());
        alert.setSensorId(data.getSensorId());
        alert.setSensorType(data.getSensorType());
        alert.setLocationId(data.getLocationId());
        alert.setRelicsId(data.getRelicsId());
        alert.setCurrentReading(data.getValue());
        
        // 添加阈值信息（需要从验证器获取）
        ISensorValidator validator = ValidatorFactory.getValidator(data.getSensorType());
        if (validator != null) {
            // 假设验证器有提供获取阈值的方法
            // 如果没有，可以添加该接口方法或使用配置值
            double threshold = 0.0; // 默认值，实际项目中应该从验证器获取
            alert.setThreshold(threshold);
        }
        
        return alert;
    }
    
    private String generateAlertMessage(SensorMessageVO data, Integer severity) {
        return String.format("传感器 %s 检测到 %s 异常值: %s %s, 告警级别: %s",
                data.getSensorId(),
                data.getSensorType(),
                data.getValue(), 
                data.getUnit() != null ? data.getUnit() : "", 
                data.getStatus());
    }
    
    /**
     * 定时批处理数据持久化
     */
    @Scheduled(fixedDelayString = "${sensor.data.batch.interval:30000}")
    public void processBatch() {
        if (dataQueue.isEmpty()) {
            return;
        }
        
        List<SensorMessageVO> batch = new ArrayList<>();
        SensorMessageVO data;
        while ((data = dataQueue.poll()) != null) {
            batch.add(data);
        }
        
        if (!batch.isEmpty()) {
            sensorDataService.batchSaveSensorData(batch);
            log.info("批量保存传感器数据 {} 条", batch.size());
        }
    }
} 