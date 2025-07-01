package com.ling.domain.sensor.service.handler;

import com.ling.domain.sensor.model.valobj.SensorMessageVO;
import com.ling.domain.sensor.service.core.ISensorDataService;
import com.ling.domain.sensor.service.event.SensorDataEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @Author: LingRJ
 * @Description: 传感器数据持久化处理器
 * @DateTime: 2025/6/30
 **/
@Component
@Slf4j
public class SensorDataPersistenceHandler {
    @Autowired
    private ISensorDataService sensorDataService;
    
    private final ConcurrentLinkedQueue<SensorMessageVO> dataQueue = new ConcurrentLinkedQueue<>();
    
    @Value("${sensor.data.batch.size:100}")
    private int batchSize;
    
    @Value("${sensor.data.batch.interval:30000}")
    private long batchIntervalMs;
    
    @EventListener
    @Async
    public void handleSensorData(SensorDataEvent event) {
        SensorMessageVO data = event.getSensorData();
        if (data == null || data.getValue() == null) {
            return;
        }
        
        if (data.getTimestamp() == null) {
            data.setTimestamp(LocalDateTime.now());
        }
        
        dataQueue.offer(data);
        
        // 当队列达到一定大小时批量处理
        if (dataQueue.size() >= batchSize) {
            processBatch();
        }
    }
    
    // 定时批处理方法
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