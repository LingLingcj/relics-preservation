package com.ling.domain.sensor.service;

/**
 * @Author: LingRJ
 * @Description: TODO
 * @DateTime: 2025/6/28 22:09
 **/
public interface ISensorMessageService {
    void processSensorMessage(String topic, String payload);
}
