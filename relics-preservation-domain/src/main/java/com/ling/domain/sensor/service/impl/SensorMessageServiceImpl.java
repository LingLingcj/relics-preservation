package com.ling.domain.sensor.service.impl;

import com.alibaba.fastjson2.JSON;
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
    
    // 传感器最新值缓存
    private final Map<String, SensorMessageVO> latestValueCache = new ConcurrentHashMap<>();
    
    // 传感器阈值配置
    private final Map<String, Double> thresholdConfig = new ConcurrentHashMap<>();
    
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
        thresholdConfig.put("temperature", 1.5);
        // 湿度变化超过5%记录
        thresholdConfig.put("humidity", 5.0);
        // 光照变化超过100lux记录
        thresholdConfig.put("light", 100.0);
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
    
    /**
     * 从topic解析传感器信息并创建SensorMessageVO对象
     * @param topic 传感器主题，格式如 "light_intensity_1"
     * @param payload 传感器数据，JSON
     * @return 解析后的SensorMessageVO对象，如果解析失败则返回null
     */
    private SensorMessageVO parseSensorMessage(String topic, String payload) {
        try {
            // 直接尝试解析JSON格式的完整消息
            SensorMessageVO sensorMessage = JSON.parseObject(payload, SensorMessageVO.class);
            if (sensorMessage != null && sensorMessage.getValue() != null) {
                // 如果已经是完整的SensorMessageVO对象，直接返回
                return sensorMessage;
            }
            
            // 如果不是完整的SensorMessageVO，创建新对象
            sensorMessage = new SensorMessageVO();
            
            // 从topic中提取传感器信息 (格式为: light_intensity_1)
            String[] topicParts = topic.split("_");
            if (topicParts.length >= 2) {
                // 查找最后一个下划线的位置
                int lastUnderscoreIndex = topic.lastIndexOf("_");
                if (lastUnderscoreIndex > 0) {
                    // 传感器类型为最后一个下划线之前的所有内容
                    String sensorType = topic.substring(0, lastUnderscoreIndex);
                    sensorMessage.setSensorType(sensorType);
                    
                    // 传感器ID为最后一个下划线之后的内容
                    String sensorId = topic.substring(lastUnderscoreIndex + 1);
                    sensorMessage.setSensorId(sensorId);
                } else {
                    // 如果没有下划线，使用整个topic作为传感器类型
                    sensorMessage.setSensorType(topic);
                    sensorMessage.setSensorId("unknown");
                }
            } else {
                // 如果topic格式不符合预期，使用整个topic作为传感器类型
                sensorMessage.setSensorType(topic);
                sensorMessage.setSensorId("unknown");
            }
            
            // 解析payload中的值 - 处理类似 {"intensity": 1878} 格式
            try {
                com.alibaba.fastjson2.JSONObject jsonObj = JSON.parseObject(payload);
                
                // 检查是否包含intensity字段
                if (jsonObj.containsKey("intensity")) {
                    sensorMessage.setValue(jsonObj.getDouble("intensity"));
                    log.debug("从intensity字段中解析到值: {}", sensorMessage.getValue());
                } else {
                    // 尝试直接解析为数值
                    try {
                        sensorMessage.setValue(Double.parseDouble(payload.trim()));
                    } catch (NumberFormatException nfe) {
                        log.warn("无法解析传感器消息值: {}", payload);
                        return null;
                    }
                }
            } catch (Exception e) {
                log.warn("解析传感器消息时发生异常: {}", e.getMessage());
                return null;
            }
            
            // 设置时间戳（如果为空）
            if (sensorMessage.getTimestamp() == null) {
                sensorMessage.setTimestamp(LocalDateTime.now());
            }
            
            return sensorMessage;
        } catch (Exception e) {
            log.warn("解析传感器消息时发生异常: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public void processSensorMessage(String topic, String payload) {
        try {
            // 解析传感器消息
            SensorMessageVO sensorMessage = parseSensorMessage(topic, payload);

            if (sensorMessage == null || sensorMessage.getValue() == null) {
                log.warn("无法解析传感器消息: {}", payload);
                return;
            }
            
            // 设置时间戳（如果为空）
            if (sensorMessage.getTimestamp() == null) {
                sensorMessage.setTimestamp(LocalDateTime.now());
            }
            
            // 检查是否需要记录此数据（采样或阈值判断）
            if (shouldRecordData(sensorMessage)) {
                // 添加到队列中等待批处理
                dataQueue.offer(sensorMessage);
                
                // 更新最新值缓存
                updateLatestValueCache(sensorMessage);
                
                // 如果是异常数据，立即保存
                if (isAbnormalData(sensorMessage)) {
                    sensorDataService.saveSensorData(sensorMessage, true);
                    log.warn("检测到异常传感器数据: 传感器={}, 类型={}, 值={}", 
                            sensorMessage.getSensorId(), sensorMessage.getSensorType(), sensorMessage.getValue());
                }
                
                // 记录日志（仅在DEBUG级别）
                log.debug("已缓存传感器数据: 传感器={}, 类型={}, 值={}", 
                        sensorMessage.getSensorId(), sensorMessage.getSensorType(), sensorMessage.getValue());
            } else {
                log.debug("跳过记录传感器数据: 传感器={}, 类型={}, 值={}", 
                        sensorMessage.getSensorId(), sensorMessage.getSensorType(), sensorMessage.getValue());
            }
        } catch (Exception e) {
            log.error("处理传感器消息失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 判断是否应该记录此数据（采样或阈值判断）
     * @param sensorMessage 传感器消息
     * @return 是否记录
     */
    private boolean shouldRecordData(SensorMessageVO sensorMessage) {
        // 计数器递增
        counter = (counter + 1) % sampleRate;
        
        // 采样记录
        if (counter == 0) {
            return true;
        }
        
        // 阈值判断
        if (thresholdEnabled) {
            String cacheKey = sensorMessage.getSensorType() + ":" + sensorMessage.getSensorId();
            SensorMessageVO lastMessage = latestValueCache.get(cacheKey);
            
            // 如果没有缓存的上一次值，则记录
            if (lastMessage == null) {
                return true;
            }
            
            // 检查值变化是否超过阈值
            Double threshold = thresholdConfig.getOrDefault(sensorMessage.getSensorType(), 0.0);
            if (Math.abs(sensorMessage.getValue() - lastMessage.getValue()) > threshold) {
                return true;
            }
            
            // 检查时间间隔，如果超过5分钟没有记录，则记录
            if (lastMessage.getTimestamp().until(sensorMessage.getTimestamp(), ChronoUnit.MINUTES) >= 5) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 更新最新值缓存
     * @param sensorMessage 传感器消息
     */
    private void updateLatestValueCache(SensorMessageVO sensorMessage) {
        String cacheKey = sensorMessage.getSensorType() + ":" + sensorMessage.getSensorId();
        latestValueCache.put(cacheKey, sensorMessage);
    }
    
    /**
     * 判断是否为异常数据
     * @param sensorMessage 传感器消息
     * @return 是否异常
     */
    private boolean isAbnormalData(SensorMessageVO sensorMessage) {
        // 这里可以根据不同传感器类型判断数据是否异常
        // 例如：温度超过30度，湿度低于20%等
        
        String type = sensorMessage.getSensorType();
        double value = sensorMessage.getValue();
        
        if ("temperature".equals(type)) {
            return value > 30.0 || value < 10.0;
        } else if ("humidity".equals(type)) {
            return value > 80.0 || value < 20.0;
        } else if ("light".equals(type) || "light_intensity".equals(type)) {
            return value > 2000.0 || value < 200.0;
        }
        
        return false;
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