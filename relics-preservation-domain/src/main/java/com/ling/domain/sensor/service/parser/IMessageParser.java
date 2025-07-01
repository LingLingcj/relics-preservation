package com.ling.domain.sensor.service.parser;

import com.ling.domain.sensor.model.valobj.SensorMessageVO;

import java.util.List;

/**
 * @Author: LingRJ
 * @Description: 消息解析器接口
 * @DateTime: 2025/6/30
 **/
public interface IMessageParser {
    /**
     * 解析消息
     * @param topic 主题
     * @param payload 消息内容
     * @return 解析后的传感器消息列表
     */
    List<SensorMessageVO> parse(String topic, String payload);
} 