package com.ling.infrastructure.dao;

import com.ling.infrastructure.dao.po.AlertRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * @Author: LingRJ
 * @Description: 告警记录DAO接口
 * @DateTime: 2025/7/5
 **/
@Mapper
public interface IAlertRecordDao {
    
    /**
     * 插入告警记录
     * @param alertRecord 告警记录
     * @return 影响行数
     */
    int insert(AlertRecord alertRecord);
    
    /**
     * 根据告警ID查询告警记录
     * @param alertId 告警ID
     * @return 告警记录
     */
    AlertRecord findByAlertId(@Param("alertId") String alertId);
    
    /**
     * 根据传感器ID和告警类型查询最近的告警记录
     * @param sensorId 传感器ID
     * @param alertType 告警类型
     * @param status 告警状态
     * @return 告警记录
     */
    AlertRecord findLatestBySensorIdAndType(
            @Param("sensorId") String sensorId,
            @Param("alertType") String alertType,
            @Param("status") String status);
    
    /**
     * 更新告警状态
     * @param alertId 告警ID
     * @param status 告警状态
     * @param resolvedTime 解决时间
     * @return 影响行数
     */
    int updateStatus(
            @Param("alertId") String alertId,
            @Param("status") String status,
            @Param("resolvedTime") Date resolvedTime);
    
    /**
     * 查询告警记录
     * @param sensorId 传感器ID
     * @param alertType 告警类型
     * @param status 告警状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制条数
     * @return 告警记录列表
     */
    List<AlertRecord> queryAlerts(
            @Param("sensorId") String sensorId,
            @Param("alertType") String alertType,
            @Param("status") String status,
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("limit") Integer limit);
} 