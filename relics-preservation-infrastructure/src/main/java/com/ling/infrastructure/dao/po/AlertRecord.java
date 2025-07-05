package com.ling.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author: LingRJ
 * @Description: 告警记录持久化对象
 * @DateTime: 2025/7/5
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlertRecord {
    // 自增主键
    private Long id;
    // 告警ID
    private String alertId;
    // 传感器ID
    private String sensorId;
    // 告警类型
    private String alertType;
    // 告警级别：INFO、WARNING、CRITICAL
    private String severity;
    // 告警消息
    private String message;
    // 文物ID
    private Long relicsId;
    // 位置ID
    private Long locationId;
    // 当前读数
    private Double currentValue;
    // 阈值
    private Double threshold;
    // 告警状态：ACTIVE、RESOLVED
    private String status;
    // 告警时间
    private Date timestamp;
    // 解决时间
    private Date resolvedTime;
    // 创建时间
    private Date createTime;
} 