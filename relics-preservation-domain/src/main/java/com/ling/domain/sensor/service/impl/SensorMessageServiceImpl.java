package com.ling.domain.sensor.service.impl;

import com.ling.domain.sensor.model.valobj.SensorMessageVO;
import com.ling.domain.sensor.service.ISensorDataService;
import com.ling.domain.sensor.service.ISensorMessageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author: LingRJ
 * @Description: 传感器消息服务实现
 * @DateTime: 2025/6/28
 **/
@Service
@Slf4j
public class SensorMessageServiceImpl implements ISensorMessageService {
    
    @Autowired
    private ISensorDataService sensorDataService;
    
    @Value("${sensor.data.batch.size:100}")
    // 批处理的最大数据量，默认为100条。如果未配置，则使用默认值。
    private int batchSize;
    
    @Value("${sensor.data.batch.interval:30000}")
    // 批处理的时间间隔（毫秒），默认为30000毫秒（30秒）。如果未配置，则使用默认值。
    private long batchIntervalMs;
    
    @Value("${sensor.data.sample.rate:10}")
    // 数据采样率，默认为10。表示每10条数据中记录1条。如果未配置，则使用默认值。
    private int sampleRate;
    
    @Value("${sensor.data.threshold.enabled:true}")
    // 是否启用阈值判断，默认为启用（true）。如果未配置，则使用默认值
    private boolean thresholdEnabled;
    
    // 传感器数据缓存队列
    private final LinkedBlockingQueue<SensorMessageVO> dataQueue = new LinkedBlockingQueue<>();

    
    // 传感器阈值配置
    private final Map<String, Double> changeThresholds = new ConcurrentHashMap<>();
    
    // 计数器，用于采样
    private int counter = 0;

    @PostConstruct
    public void init() {
        // 初始化阈值配置
        initThresholdConfig();
        
        // 启动批处理线程
        startBatchProcessThread();
    }
    
    /**
     * 初始化阈值配置
     */
    private void initThresholdConfig() {
        // 这里可以从配置文件或数据库加载阈值配置
        // 温度变化超过1.5度记录
        changeThresholds.put("temperature", 1.5);
        // 湿度变化超过5%记录
        changeThresholds.put("humidity", 5.0);
        // 光照变化超过100lux记录
        changeThresholds.put("light", 100.0);
    }
    
    /**
     * 启动批处理线程
     */
    private void startBatchProcessThread() {
        Thread batchThread = new Thread(() -> {
            while (true) {
                try {
                    processBatch();
                    Thread.sleep(batchIntervalMs);
                } catch (InterruptedException e) {
                    log.error("批处理线程被中断: {}", e.getMessage(), e);
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("批处理异常: {}", e.getMessage(), e);
                }
            }
        });
        batchThread.setName("sensor-data-batch-thread");
        batchThread.setDaemon(true);
        batchThread.start();
        log.info("传感器数据批处理线程已启动");
    }
    
    @Override
    public void processSensorMessages(String topic, List<SensorMessageVO> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        
        try {
            // 处理每个传感器消息
            for (SensorMessageVO message : messages) {
                processSingleSensorMessage(message);
            }
            log.debug("成功处理{}个传感器数据", messages.size());
        } catch (Exception e) {
            log.error("批量处理传感器消息失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 处理单个传感器消息
     * @param sensorMessage 传感器消息对象
     */
    private void processSingleSensorMessage(SensorMessageVO sensorMessage) {
        if (sensorMessage == null || sensorMessage.getValue() == null) {
            return;
        }
        
        // 设置时间戳（如果为空）
        if (sensorMessage.getTimestamp() == null) {
            sensorMessage.setTimestamp(LocalDateTime.now());
        }

        // 添加到队列中等待批处理
        dataQueue.offer(sensorMessage);

    }


    
    /**
     * 处理批量数据
     */
    private void processBatch() {
        if (dataQueue.isEmpty()) {
            return;
        }
        
        List<SensorMessageVO> batch = new ArrayList<>(batchSize);
        dataQueue.drainTo(batch, batchSize);
        
        if (!batch.isEmpty()) {
            int count = sensorDataService.batchSaveSensorData(batch);
            log.info("批量保存传感器数据: {}条", count);
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
    
    /**
     * 定时清理历史数据（每月1日凌晨1:00执行）
     */
    @Scheduled(cron = "0 0 1 1 * ?")
    public void scheduleDataCleanup() {
        try {
            // 清理3个月前的数据
            LocalDateTime beforeTime = LocalDateTime.now().minusMonths(3);
            int count = sensorDataService.cleanHistoricalData(beforeTime);
            log.info("清理历史数据完成: {}条记录, 删除{}之前的数据", count, beforeTime);
        } catch (Exception e) {
            log.error("清理历史数据失败: {}", e.getMessage(), e);
        }
    }
} 