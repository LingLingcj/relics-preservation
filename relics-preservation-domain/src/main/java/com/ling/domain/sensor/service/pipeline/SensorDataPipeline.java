package com.ling.domain.sensor.service.pipeline;

import com.ling.domain.sensor.model.valobj.AlertMessageVO;
import com.ling.domain.sensor.model.valobj.SensorMessageVO;
import com.ling.domain.sensor.service.core.ISensorDataService;
import com.ling.domain.sensor.service.message.validation.ISensorValidator;
import com.ling.domain.sensor.service.message.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
                
                // 3. 将数据放入持久化队列
                dataQueue.offer(message);
            }
            
            // 4. 达到批次大小时立即处理
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
        
        // 这里可以添加告警处理逻辑，如：
        // 1. 保存到数据库
        // 2. 发送通知
        // 3. 触发其他业务流程
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
        
        return alert;
    }
    
    private String generateAlertMessage(SensorMessageVO data, Integer severity) {
        return String.format("传感器 %s 检测到异常值: %s %s, 告警级别: %s", 
                data.getSensorId(), 
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
    
    /**
     * 定时执行小时聚合（每小时执行一次）
     */
    @Scheduled(cron = "0 0 1/1 * * ?")
    public void scheduleHourlyAggregation() {
        try {
            LocalDateTime lastHour = LocalDateTime.now().minusHours(1).withMinute(0).withSecond(0).withNano(0);
            int count = sensorDataService.aggregateHourlyData(lastHour);
            log.info("执行小时数据聚合完成: {}条记录, 时间: {}", count, lastHour);
        } catch (Exception e) {
            log.error("执行小时数据聚合失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 定时执行日聚合（每天凌晨0:10执行）
     */
    @Scheduled(cron = "0 10 0 * * ?")
    public void scheduleDailyAggregation() {
        try {
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            int count = sensorDataService.aggregateDailyData(yesterday);
            log.info("执行日数据聚合完成: {}条记录, 日期: {}", count, yesterday);
        } catch (Exception e) {
            log.error("执行日数据聚合失败: {}", e.getMessage(), e);
        }
    }
} 