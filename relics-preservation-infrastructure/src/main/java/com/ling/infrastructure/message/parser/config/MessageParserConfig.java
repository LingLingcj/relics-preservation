package com.ling.infrastructure.message.parser.config;

import com.ling.domain.sensor.service.parser.IMessageParser;
import com.ling.domain.sensor.service.validation.ISensorValidator;
import com.ling.infrastructure.message.parser.BasicMessageParser;
import com.ling.infrastructure.message.parser.ValidatorMessageParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @Author: LingRJ
 * @Description: 处理器配置
 * @DateTime: 2025/6/30 16:03
 **/
@Configuration
public class MessageParserConfig {
    @Bean
    @Primary
    public IMessageParser messageParser() {
        IMessageParser basicParser = new BasicMessageParser();

        // 可以链式添加多个装饰器
        return new ValidatorMessageParser(basicParser);
    }
}
