package com.ling.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author: LingRJ
 * @Description: 传感器数据持久化对象
 * @DateTime: 2025/6/29
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SensorData {
    // 自增主键
    private Long id;
    // 传感器唯一标识
    private String sensorId;
    // 传感器类型
    private String type;
    // 数据值
    private Double value;
    // 单位
    private String unit;
    // 位置ID
    private Integer locationId;
    // 关联的文物ID
    private Integer relicId;
    // 数据时间戳
    private Date timestamp;
    // 是否异常数据
    private Boolean isAbnormal;
    // 创建时间
    private Date createTime;
} 