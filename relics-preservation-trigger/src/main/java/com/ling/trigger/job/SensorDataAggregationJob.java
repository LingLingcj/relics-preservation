package com.ling.trigger.job;

import com.ling.domain.sensor.service.core.ISensorDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 传感器数据聚合定时任务
 */
@Slf4j
@Component
public class SensorDataAggregationJob {

    @Autowired
    private ISensorDataService sensorDataService;

    /**
     * 定时执行小时聚合（每小时执行一次，在每小时的0分时执行）
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