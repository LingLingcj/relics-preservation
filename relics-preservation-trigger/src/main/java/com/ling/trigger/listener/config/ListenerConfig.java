package com.ling.trigger.listener.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.router.HeaderValueRouter;
import org.springframework.messaging.MessageChannel;

/**
 * @Author: LingRJ
 * @Description: 监听器配置类
 * @DateTime: 2025/6/28
 **/
@Configuration
@EnableIntegration
@IntegrationComponentScan("com.ling.trigger.listener")
public class ListenerConfig {

    /**
     * 配置路由器，根据主题将消息路由到不同的处理器
     * @param mqttInputChannel MQTT输入通道
     * @return 头部值路由器
     */
    @Bean
    public HeaderValueRouter mqttMessageRouter(MessageChannel mqttInputChannel) {
        HeaderValueRouter router = new HeaderValueRouter("mqtt_receivedTopic");
        
        // 根据主题前缀路由消息
        router.setChannelMapping("sensor/", "sensorChannel");
        router.setChannelMapping("alert/", "alertChannel");
        router.setDefaultOutputChannelName("defaultChannel");
        
        return router;
    }
    
    /**
     * 传感器数据通道
     * @return 消息通道
     */
    @Bean
    public MessageChannel sensorChannel() {
        return new PublishSubscribeChannel();
    }
    
    /**
     * 告警数据通道
     * @return 消息通道
     */
    @Bean
    public MessageChannel alertChannel() {
        return new PublishSubscribeChannel();
    }
    
    /**
     * 默认通道
     * @return 消息通道
     */
    @Bean
    public MessageChannel defaultChannel() {
        return new PublishSubscribeChannel();
    }
} 