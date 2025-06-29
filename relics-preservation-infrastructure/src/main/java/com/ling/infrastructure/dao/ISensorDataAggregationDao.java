package com.ling.infrastructure.dao;

import com.ling.infrastructure.dao.po.SensorDataDaily;
import com.ling.infrastructure.dao.po.SensorDataHourly;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * @Author: LingRJ
 * @Description: 传感器数据聚合DAO接口
 * @DateTime: 2025/6/29
 **/
@Mapper
public interface ISensorDataAggregationDao {
    
    /**
     * 插入小时聚合数据
     * @param hourlyData 小时聚合数据
     * @return 影响行数
     */
    int insertHourlyData(SensorDataHourly hourlyData);
    
    /**
     * 插入日聚合数据
     * @param dailyData 日聚合数据
     * @return 影响行数
     */
    int insertDailyData(SensorDataDaily dailyData);
    
    /**
     * 查询小时聚合数据
     * @param sensorId 传感器ID
     * @param type 传感器类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 小时聚合数据列表
     */
    List<SensorDataHourly> queryHourlyData(
            @Param("sensorId") String sensorId,
            @Param("type") String type,
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime);
    
    /**
     * 查询日聚合数据
     * @param sensorId 传感器ID
     * @param type 传感器类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日聚合数据列表
     */
    List<SensorDataDaily> queryDailyData(
            @Param("sensorId") String sensorId,
            @Param("type") String type,
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime);
    
    /**
     * 根据原始数据计算并插入小时聚合数据
     * @param hour 小时时间戳
     * @return 影响行数
     */
    int aggregateHourlyData(@Param("hour") Date hour);
    
    /**
     * 根据小时聚合数据计算并插入日聚合数据
     * @param day 日期
     * @return 影响行数
     */
    int aggregateDailyData(@Param("day") Date day);
} 