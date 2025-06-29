package com.ling.domain.sensor.adapter;

import com.ling.domain.sensor.model.valobj.SensorMessageVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: LingRJ
 * @Description: 传感器数据仓库接口
 * @DateTime: 2025/6/29
 **/
public interface ISensorDataRepository {
    
    /**
     * 保存单条传感器数据
     * @param sensorMessage 传感器消息
     * @param isAbnormal 是否异常数据
     * @return 是否保存成功
     */
    boolean saveSensorData(SensorMessageVO sensorMessage, boolean isAbnormal);
    
    /**
     * 批量保存传感器数据
     * @param sensorMessages 传感器消息列表
     * @return 成功保存的数据条数
     */
    int batchSaveSensorData(List<SensorMessageVO> sensorMessages);
    
    /**
     * 查询传感器数据
     * @param sensorId 传感器ID
     * @param sensorType 传感器类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param isAbnormal 是否异常数据
     * @param limit 限制条数
     * @return 传感器数据列表
     */
    List<SensorMessageVO> querySensorData(
            String sensorId, 
            String sensorType, 
            LocalDateTime startTime, 
            LocalDateTime endTime, 
            Boolean isAbnormal,
            Integer limit);
    
    /**
     * 执行小时聚合
     * @param hour 小时时间戳
     * @return 聚合记录数
     */
    int aggregateHourlyData(LocalDateTime hour);
    
    /**
     * 执行日聚合
     * @param day 日期
     * @return 聚合记录数
     */
    int aggregateDailyData(LocalDateTime day);
    
    /**
     * 清理历史数据
     * @param beforeTime 时间点（删除此时间点之前的数据）
     * @return 删除记录数
     */
    int cleanHistoricalData(LocalDateTime beforeTime);
} 