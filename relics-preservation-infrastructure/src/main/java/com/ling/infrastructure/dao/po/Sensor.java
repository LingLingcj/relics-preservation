package com.ling.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author: LingRJ
 * @Description: 传感器信息实体
 * @DateTime: 2025/6/27 23:55
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Sensor {
    // 自增主键
    private Integer id;
    // 传感器唯一标识
    private String sensorId;
    // 设备名称
    private String name;
    // 传感器类型
    private String type;
    // 设备型号
    private String model;
    // 安装位置ID
    private Integer locationId;
    // 关联的文物ID
    private Integer relicId;
    // 设备状态
    private Byte status;
    // 创建时间
    private Date createTime;
    // 最后更新时间
    private Date updateTime;
}
