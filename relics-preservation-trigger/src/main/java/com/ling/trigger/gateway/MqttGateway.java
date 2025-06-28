package com.ling.trigger.gateway;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * @Author: LingRJ
 * @Description: MQTT消息网关接口
 * @DateTime: 2025/6/28
 **/
@Component
@MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
public interface MqttGateway {
    
    /**
     * 发送消息到默认主题
     * @param payload 消息内容
     */
    void sendToMqtt(String payload);
    
    /**
     * 发送消息到指定主题
     * @param payload 消息内容
     * @param topic 主题
     */
    void sendToMqtt(String payload, @Header(MqttHeaders.TOPIC) String topic);
    
    /**
     * 发送消息到指定主题和QoS
     * @param payload 消息内容
     * @param topic 主题
     * @param qos QoS
     */
    void sendToMqtt(String payload, @Header(MqttHeaders.TOPIC) String topic, @Header(MqttHeaders.QOS) int qos);
} 