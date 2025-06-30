package com.ling.domain.sensor.service.pattern.subject.impl;

import com.ling.domain.sensor.model.valobj.SensorMessageVO;
import com.ling.domain.sensor.service.pattern.observer.ISensorDataObserver;
import com.ling.domain.sensor.service.pattern.subject.ISensorDataSubject;
import jakarta.annotation.PostConstruct;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Author: LingRJ
 * @Description: 主题(Subject)实现
 * @DateTime: 2025/6/30 20:55
 **/
@Component
public class SensorDataSubjectImpl implements ISensorDataSubject {
    private final List<ISensorDataObserver> observers = new CopyOnWriteArrayList<>();
    private final ApplicationContext applicationContext;

    public SensorDataSubjectImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        Map<String, ISensorDataObserver> observerMap = applicationContext.getBeansOfType(ISensorDataObserver.class);
        for (ISensorDataObserver observer : observerMap.values()) {
            registerObserver(observer);
        }
    }

    @Override
    public void registerObserver(ISensorDataObserver observer) {
        observers.add(observer);
    }

    @Override
    public void deleteObserver(ISensorDataObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObserver(SensorMessageVO sensorMessageVO) {
        if (observers.isEmpty() || sensorMessageVO==null) {
            return;
        }

        for (ISensorDataObserver observer : observers) {
            observer.update(sensorMessageVO);
        }
    }

    @Override
    public void notifyObserver(List<SensorMessageVO> sensorMessageVOList) {
        if (sensorMessageVOList.isEmpty() || observers.isEmpty()) {
            return;
        }

        for (ISensorDataObserver observer : observers) {
            for (SensorMessageVO sensorMessageVO : sensorMessageVOList) {
                observer.update(sensorMessageVO);
            }
        }
    }
}
