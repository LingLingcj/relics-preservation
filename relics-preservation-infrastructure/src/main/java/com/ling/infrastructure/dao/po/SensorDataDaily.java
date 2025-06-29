package com.ling.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author: LingRJ
 * @Description: 传感器数据日聚合持久化对象
 * @DateTime: 2025/6/29
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SensorDataDaily {
    // 自增主键
    private Long id;
    // 传感器唯一标识
    private String sensorId;
    // 传感器类型
    private String type;
    // 最小值
    private Double minValue;
    // 最大值
    private Double maxValue;
    // 平均值
    private Double avgValue;
    // 标准差
    private Double stdDev;
    // 样本数量
    private Integer sampleCount;
    // 单位
    private String unit;
    // 位置ID
    private Integer locationId;
    // 关联的文物ID
    private Integer relicId;
    // 日期
    private Date dayTimestamp;
    // 创建时间
    private Date createTime;
} 