package com.ling.trigger.listener;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ling.domain.sensor.model.valobj.SensorMessageVO;
import com.ling.domain.sensor.service.parser.IMessageParser;
import com.ling.domain.sensor.service.sensor.ISensorMessageService;
import com.ling.domain.sensor.service.validation.ISensorValidator;
import com.ling.domain.sensor.service.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: LingRJ
 * @Description: 传感器数据MQTT监听器
 * @DateTime: 2025/6/28
 **/
@Component
@Slf4j
public class SensorDataListener {

    @Autowired
    private ISensorMessageService sensorMessageService;
    @Autowired
    private IMessageParser messageParser;
    
    // 消息统计
    private final AtomicInteger messageCounter = new AtomicInteger(0);
    private volatile long lastLogTime = System.currentTimeMillis();
    // 每分钟记录一次统计日志
    private static final long LOG_INTERVAL_MS = 60000;

    /**
     * 监听传感器MQTT消息
     * @param message MQTT消息
     */
    @ServiceActivator(inputChannel = "sensorChannel")
    public void handleMessage(Message<?> message) {
        String topic = message.getHeaders().get("mqtt_receivedTopic").toString();
        String payload = message.getPayload().toString();

        // 增加消息计数
        int count = messageCounter.incrementAndGet();

        // 仅在DEBUG级别记录详细消息内容
        log.debug("接收到传感器消息，主题: {}, 内容: {}", topic, payload);

        try {
            // 使用装饰后的parser
            List<SensorMessageVO> sensorMessages = messageParser.parse(
                    topic,
                    payload
            );
            
            // 处理有效数据
            if (!sensorMessages.isEmpty()) {
                sensorMessageService.processSensorMessages(topic, sensorMessages);
                log.debug("成功处理{}个传感器数据字段", sensorMessages.size());
            }
            
            // 定期记录统计信息
            logMessageStats();
            
        } catch (Exception e) {
            log.error("处理传感器消息失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 定期记录消息统计信息
     */
    private void logMessageStats() {
        long currentTime = System.currentTimeMillis();
        long timePassed = currentTime - lastLogTime;
        
        // 每分钟记录一次统计信息
        if (timePassed >= LOG_INTERVAL_MS) {
            int count = messageCounter.getAndSet(0);
            double messagesPerSecond = count * 1000.0 / timePassed;
            log.info("传感器消息统计: 最近{}秒接收{}条消息, 平均每秒{}条", 
                    timePassed/1000, count, String.format("%.2f", messagesPerSecond));
            lastLogTime = currentTime;
        }
    }

}
