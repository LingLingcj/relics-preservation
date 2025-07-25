package com.ling.trigger.listener;

import com.ling.domain.sensor.model.valobj.SensorMessageVO;
import com.ling.domain.sensor.service.parser.MessageParserManager;
import com.ling.domain.sensor.service.pipeline.SensorDataPipeline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

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
    private MessageParserManager messageParser;
    
    @Autowired
    private SensorDataPipeline sensorDataPipeline;
    
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

        // 如果为重复消息，则不处理
        try {
            Object duplicate =  message.getHeaders().get("mqtt_duplicate");
            if (Boolean.TRUE.equals(duplicate)) {
                log.debug("重复消息，忽略处理");
                return;
            }
        }
        catch (Exception e) {
            log.warn("无法判断消息是否重复：{}，错误信息：{}", message.getHeaders().get("mqtt_duplicate"), e.getMessage(), e);
        }

        // 增加消息计数
        int count = messageCounter.incrementAndGet();

        // 仅在DEBUG级别记录详细消息内容
        log.debug("接收到传感器消息，主题: {}, 内容: {}", topic, payload);

        try {
            // 解析消息
            List<SensorMessageVO> sensorMessages = messageParser.parse(topic, payload);
            
            // 通过管道处理消息
            if (!sensorMessages.isEmpty()) {
                sensorDataPipeline.process(sensorMessages);
                log.debug("成功提交{}个传感器数据字段到处理管道", sensorMessages.size());
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
