package com.ling.infrastructure.dao;

import com.ling.infrastructure.dao.po.SensorData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * @Author: LingRJ
 * @Description: 传感器数据DAO接口
 * @DateTime: 2025/6/29
 **/
@Mapper
public interface ISensorDataDao {
    
    /**
     * 批量插入传感器数据
     * @param sensorDataList 传感器数据列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<SensorData> sensorDataList);
    
    /**
     * 根据条件查询传感器数据
     * @param sensorId 传感器ID
     * @param type 传感器类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param isAbnormal 是否异常数据
     * @param limit 限制条数
     * @return 传感器数据列表
     */
    List<SensorData> querySensorData(
            @Param("sensorId") String sensorId,
            @Param("type") String type,
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("isAbnormal") Boolean isAbnormal,
            @Param("limit") Integer limit);
    
    /**
     * 查询指定时间段内的异常数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 异常数据列表
     */
    List<SensorData> queryAbnormalData(
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime);
    
    /**
     * 删除指定时间之前的数据（用于数据归档）
     * @param beforeTime 时间点
     * @return 影响行数
     */
    int deleteDataBefore(@Param("beforeTime") Date beforeTime);
} 