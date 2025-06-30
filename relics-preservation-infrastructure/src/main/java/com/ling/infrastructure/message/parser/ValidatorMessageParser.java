package com.ling.infrastructure.message.parser;

import com.ling.domain.sensor.model.valobj.SensorMessageVO;
import com.ling.domain.sensor.service.message.parser.IMessageParser;
import com.ling.domain.sensor.service.message.validation.ISensorValidator;
import com.ling.domain.sensor.service.message.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @Author: LingRJ
 * @Description: 状态验证器
 * @DateTime: 2025/6/30 16:01
 **/
@Slf4j
public class ValidatorMessageParser implements IMessageParser {
    private final IMessageParser messageParser;

    public ValidatorMessageParser(IMessageParser messageParser) {
        this.messageParser = messageParser;
    }

    @Override
    public List<SensorMessageVO> parse(String topic, String payload) {


        // 处理json字符串
        List<SensorMessageVO> sensorMessageVOList = messageParser.parse(topic, payload);


        sensorMessageVOList.forEach(message -> {
            ISensorValidator validator = ValidatorFactory.getValidator(message.getSensorType());
            if (validator != null) {
                message.setStatus(validator.validateStatus(message.getValue()));
                log.info("传感器类型：{}，传感器状态：{}", message.getSensorType(), message.getStatus());
            }
        });

        return sensorMessageVOList;
    }

}
