package com.ling.domain.sensor.service.parser;

import com.ling.domain.sensor.model.valobj.SensorMessageVO;
import com.ling.domain.sensor.service.message.validation.ISensorValidator;
import com.ling.domain.sensor.service.message.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @Author: LingRJ
 * @Description: 消息解析器管理器
 * @DateTime: 2025/6/30
 **/
@Component
@Slf4j
public class MessageParserManager {
    private final Map<String, MessageParser> parsers;
    private final MessageParser defaultParser;
    
    public MessageParserManager(Map<String, MessageParser> parsers) {
        this.parsers = parsers;
        this.defaultParser = parsers.getOrDefault("default", parsers.values().stream().findFirst().orElse(null));
        
        if (this.defaultParser == null) {
            throw new IllegalStateException("至少需要一个消息解析器");
        }
    }
    
    public List<SensorMessageVO> parse(String topic, String payload) {
        // 根据topic前缀选择合适的解析器
        String parserType = determineParserType(topic);
        MessageParser parser = parsers.getOrDefault(parserType, defaultParser);
        
        // 解析消息
        List<SensorMessageVO> messages = parser.parse(topic, payload);
        
        // 对所有消息进行验证
        validateMessages(messages);
        
        return messages;
    }
    
    private void validateMessages(List<SensorMessageVO> messages) {
        messages.forEach(message -> {
            ISensorValidator validator = ValidatorFactory.getValidator(message.getSensorType());
            if (validator != null) {
                message.setStatus(validator.validateStatus(message.getValue()));
                log.debug("传感器类型：{}，传感器值：{}，传感器状态：{}", 
                        message.getSensorType(), message.getValue(), message.getStatus());
            }
        });
    }
    
    private String determineParserType(String topic) {
        // 根据topic确定解析器类型
        // 这里可以根据实际需求实现不同的选择逻辑
        return "default";
    }
} 