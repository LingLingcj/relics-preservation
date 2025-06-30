package com.ling.infrastructure.message.parser;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ling.domain.sensor.model.valobj.SensorMessageVO;
import com.ling.domain.sensor.service.parser.IMessageParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: LingRJ
 * @Description: 基本消息处理
 * @DateTime: 2025/6/30 15:59
 **/
@Slf4j
@Component
// 基础解析器
public class BasicMessageParser implements IMessageParser {
    @Override
    public List<SensorMessageVO> parse(String topic, String payload) {
        List<SensorMessageVO> result = new ArrayList<>();
        String sensorId = extractSensorIdFromTopic(topic);

        try {
            JSONObject jsonObj = JSON.parseObject(payload);
            if (jsonObj.containsKey("stat")) {
                int stat = jsonObj.getIntValue("stat");
                if (stat != 0) {
                    log.warn("传感器检测到异常，危险等级：{}",stat);
                }
                jsonObj.remove("stat");
            }


            jsonObj.forEach((key, value) -> {
                if (value instanceof Number) {
                    result.add(SensorMessageVO.create(
                            sensorId, key, ((Number) value).doubleValue()
                    ));
                }
            });
        } catch (Exception e) {
            log.warn("无法解析传感器消息值: {}", payload);
        }

        return result;
    }

    /**
     * 从主题中提取传感器ID
     * @param topic 主题
     * @return 传感器ID
     */
    private String extractSensorIdFromTopic(String topic) {
        int lastUnderscoreIndex = topic.lastIndexOf("_");
        return lastUnderscoreIndex > 0 ?
                topic.substring(lastUnderscoreIndex + 1) : topic;
    }
}
