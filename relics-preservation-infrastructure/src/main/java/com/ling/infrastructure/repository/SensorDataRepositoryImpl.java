package com.ling.infrastructure.repository;

import com.ling.domain.sensor.adapter.ISensorDataRepository;
import com.ling.domain.sensor.model.valobj.SensorMessageVO;
import com.ling.infrastructure.dao.ISensorDataAggregationDao;
import com.ling.infrastructure.dao.ISensorDataDao;
import com.ling.infrastructure.dao.po.SensorData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: LingRJ
 * @Description: 传感器数据仓库实现
 * @DateTime: 2025/6/29
 **/
@Repository
@Slf4j
public class SensorDataRepositoryImpl implements ISensorDataRepository {

    @Autowired
    private ISensorDataDao sensorDataDao;
    
    @Autowired
    private ISensorDataAggregationDao sensorDataAggregationDao;
    
    @Override
    public boolean saveSensorData(SensorMessageVO sensorMessage, boolean isAbnormal) {
        try {
            SensorData sensorData = convertToSensorData(sensorMessage, isAbnormal);
            List<SensorData> dataList = new ArrayList<>();
            dataList.add(sensorData);
            return sensorDataDao.batchInsert(dataList) > 0;
        } catch (Exception e) {
            log.error("保存传感器数据失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public int batchSaveSensorData(List<SensorMessageVO> sensorMessages) {
        try {
            List<SensorData> dataList = sensorMessages.stream()
                    .map(message -> convertToSensorData(message, !message.getStatus().equals(0)))
                    .collect(Collectors.toList());
            return sensorDataDao.batchInsert(dataList);
        } catch (Exception e) {
            log.error("批量保存传感器数据失败: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public List<SensorMessageVO> querySensorData(
            String sensorId, 
            String sensorType, 
            LocalDateTime startTime, 
            LocalDateTime endTime, 
            Boolean isAbnormal,
            Integer limit) {
        try {
            Date startDate = startTime != null ? 
                    Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant()) : null;
            Date endDate = endTime != null ? 
                    Date.from(endTime.atZone(ZoneId.systemDefault()).toInstant()) : null;
            
            List<SensorData> dataList = sensorDataDao.querySensorData(
                    sensorId, sensorType, startDate, endDate, isAbnormal, limit);
            
            return dataList.stream()
                    .map(this::convertToSensorMessageVO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("查询传感器数据失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public int aggregateHourlyData(LocalDateTime hour) {
        try {
            Date hourDate = Date.from(hour.atZone(ZoneId.systemDefault()).toInstant());
            return sensorDataAggregationDao.aggregateHourlyData(hourDate);
        } catch (Exception e) {
            log.error("执行小时聚合失败: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public int aggregateDailyData(LocalDateTime day) {
        try {
            Date dayDate = Date.from(day.atZone(ZoneId.systemDefault()).toInstant());
            return sensorDataAggregationDao.aggregateDailyData(dayDate);
        } catch (Exception e) {
            log.error("执行日聚合失败: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public int cleanHistoricalData(LocalDateTime beforeTime) {
        try {
            Date beforeDate = Date.from(beforeTime.atZone(ZoneId.systemDefault()).toInstant());
            return sensorDataDao.deleteDataBefore(beforeDate);
        } catch (Exception e) {
            log.error("清理历史数据失败: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * 将传感器消息值对象转换为传感器数据持久化对象
     * @param sensorMessage 传感器消息值对象
     * @param isAbnormal 是否异常数据
     * @return 传感器数据持久化对象
     */
    private SensorData convertToSensorData(SensorMessageVO sensorMessage, boolean isAbnormal) {
        SensorData sensorData = new SensorData();
        sensorData.setSensorId(sensorMessage.getSensorId());
        sensorData.setType(sensorMessage.getSensorType());
        sensorData.setValue(sensorMessage.getValue());
        sensorData.setUnit(sensorMessage.getUnit());
        // 转换时间戳
        sensorData.setTimestamp(Date.from(sensorMessage.getTimestamp().atZone(ZoneId.systemDefault()).toInstant()));
        sensorData.setIsAbnormal(isAbnormal);
        
        return sensorData;
    }
    
    /**
     * 将传感器数据持久化对象转换为传感器消息值对象
     * @param sensorData 传感器数据持久化对象
     * @return 传感器消息值对象
     */
    private SensorMessageVO convertToSensorMessageVO(SensorData sensorData) {
        SensorMessageVO sensorMessage = new SensorMessageVO();
        sensorMessage.setSensorId(sensorData.getSensorId());
        sensorMessage.setSensorType(sensorData.getType());
        sensorMessage.setValue(sensorData.getValue());
        sensorMessage.setUnit(sensorData.getUnit());
        sensorMessage.setIsAbnormal(sensorData.getIsAbnormal());
        
        // 转换时间戳
        sensorMessage.setTimestamp(sensorData.getTimestamp().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());
        
        return sensorMessage;
    }
} 