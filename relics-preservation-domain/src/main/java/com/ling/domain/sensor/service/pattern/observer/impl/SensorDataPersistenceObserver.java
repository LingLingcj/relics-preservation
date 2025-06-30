package com.ling.domain.sensor.service.pattern.observer.impl;

import com.ling.domain.sensor.model.valobj.SensorMessageVO;
import com.ling.domain.sensor.service.pattern.observer.ISensorDataObserver;
import com.ling.domain.sensor.service.core.ISensorDataService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author: LingRJ
 * @Description: 消息接收观察者
 * @DateTime: 2025/6/30 20:58
 **/
@Slf4j
public class SensorDataPersistenceObserver implements ISensorDataObserver {

    @Autowired
    private ISensorDataService sensorDataService;

    private final LinkedBlockingQueue<SensorMessageVO> dataQueue = new LinkedBlockingQueue<>();

    @Value("${sensor.data.batch.size:100}")
    // 批处理的最大数据量，默认为100条。如果未配置，则使用默认值。
    private int batchSize;

    @Value("${sensor.data.batch.interval:30000}")
    // 批处理的时间间隔（毫秒），默认为30000毫秒（30秒）。如果未配置，则使用默认值。
    private long batchIntervalMs;

    @PostConstruct
    public void init() {
        // 启动批处理线程
        startBatchProcessThread();
    }

    @Override
    public void update(SensorMessageVO sensorMessageVO) {
        if (sensorMessageVO == null || sensorMessageVO.getValue() == null) {
            return;
        }

        if (sensorMessageVO.getTimestamp() == null) {
            sensorMessageVO.setTimestamp(LocalDateTime.now());
        }

        dataQueue.offer(sensorMessageVO);
    }

    /**
     * 批处理线程
     */
    private void startBatchProcessThread() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {

                    Thread.sleep(batchIntervalMs);
                } catch (InterruptedException e) {
                    log.error("批处理线程处理中断：{}", e.getMessage(), e);
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    log.error("批处理线程处理失败：{}", e.getMessage(), e);
                }
            }
        });
        thread.setDaemon(true);
        thread.setName("batch-process-thread");
        thread.start();
    }

    private void processBatch() {
        if (dataQueue.isEmpty()) {
            return;
        }

        List<SensorMessageVO> batch = new ArrayList<>();
        dataQueue.drainTo(batch);

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
