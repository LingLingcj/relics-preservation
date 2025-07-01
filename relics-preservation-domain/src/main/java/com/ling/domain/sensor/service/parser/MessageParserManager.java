package com.ling.domain.sensor.service.parser;

import com.ling.domain.sensor.model.valobj.SensorMessageVO;
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
    private final Map<String, IMessageParser> parsers;
    private final IMessageParser defaultParser;
    
    public MessageParserManager(Map<String, IMessageParser> parsers) {
        this.parsers = parsers;
        this.defaultParser = parsers.getOrDefault("default", parsers.values().stream().findFirst().orElse(null));
        
        if (this.defaultParser == null) {
            throw new IllegalStateException("至少需要一个消息解析器");
        }
    }
    
    public List<SensorMessageVO> parse(String topic, String payload) {
        // 根据topic前缀选择合适的解析器
        String parserType = determineParserType(topic);
        IMessageParser parser = parsers.getOrDefault(parserType, defaultParser);
        
        // 解析消息并返回
        return parser.parse(topic, payload);
    }
    
    private String determineParserType(String topic) {
        // 根据topic确定解析器类型
        // 可以根据实际需求实现不同的选择逻辑
        return "default";
    }
} 