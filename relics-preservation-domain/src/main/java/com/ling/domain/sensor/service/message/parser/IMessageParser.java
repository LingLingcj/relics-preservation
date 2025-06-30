package com.ling.domain.sensor.service.message.parser;

import com.ling.domain.sensor.model.valobj.SensorMessageVO;

import java.util.List;

/**
 * @Author: LingRJ
 * @Description: 装饰器接口
 * @DateTime: 2025/6/30 15:53
 **/
public interface IMessageParser {
    List<SensorMessageVO> parse(String topic, String payload);
}
