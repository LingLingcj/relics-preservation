package com.ling.domain.sensor.service.pattern.observer;

import com.ling.domain.sensor.model.valobj.SensorMessageVO;

/**
* @Author: LingRJ
* @Description: 观察者接口
* @DateTime: 2025/6/30 20:23
**/
public interface ISensorDataObserver {
    /**
     * 传感器数据更新时
     * @param sensorMessageVO 传感器数据
     */
    void update(SensorMessageVO sensorMessageVO);
}
