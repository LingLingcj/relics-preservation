package com.ling.domain.sensor.service.pattern.subject;

import com.ling.domain.sensor.model.valobj.SensorMessageVO;
import com.ling.domain.sensor.service.pattern.observer.ISensorDataObserver;

import java.util.List;

/**
 * @Author: LingRJ
 * @Description: 主题(Subject)接口
 * @DateTime: 2025/6/30 20:52
 **/
public interface ISensorDataSubject {
    /**
     * 注册Observer
     * @param observer 观察者类
     */
    void registerObserver(ISensorDataObserver observer);

    /**
     * 删除Observer
     * @param observer 观察者类
     */
    void deleteObserver(ISensorDataObserver observer);

    /**
     * 通知Observer单条消息
     * @param sensorMessageVO 通知内容
     */
    void notifyObserver(SensorMessageVO sensorMessageVO);

    /**
     * 通知Observer多条消息
     * @param sensorMessageVOList 通知内容
     */
    void notifyObserver(List<SensorMessageVO> sensorMessageVOList);
}
