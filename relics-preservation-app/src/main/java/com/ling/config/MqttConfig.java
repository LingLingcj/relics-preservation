package com.ling.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.router.HeaderValueRouter;
import org.springframework.messaging.MessageChannel;

import java.util.Arrays;
import java.util.List;

/**
 * @Author: LingRJ
 * @Description: MQTT配置类
 * @DateTime: 2025/6/28
 **/
@Configuration
@EnableIntegration
@IntegrationComponentScan({"com.ling.trigger.listener", "com.ling.trigger.gateway"})
public class MqttConfig {

    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Value("${mqtt.client.id:}")
    private String clientId;

    @Value("${mqtt.username}")
    private String username;

    @Value("${mqtt.password}")
    private String password;

    @Value("${mqtt.topics}")
    private String[] sensorTopics;

    // 传感器主题前缀列表
    private static final List<String> SENSOR_TOPIC_PREFIXES = Arrays.asList(
            "ems", "light_intensity_1", "temperature_", "humidity_",
            "light", "temperature", "humidity"
    );

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[] { brokerUrl });
        
        if (username != null && !username.isEmpty()) {
            options.setUserName(username);
        }
        if (password != null && !password.isEmpty()) {
            options.setPassword(password.toCharArray());
        }
        
        options.setCleanSession(false);
        // 开启自动重连
        options.setAutomaticReconnect(true);
        // 更进一步减小心跳间隔，防止连接断开
        options.setKeepAliveInterval(5);
        // 设置连接超时时间
        options.setConnectionTimeout(30);
        // 设置最大重连间隔
        options.setMaxReconnectDelay(1000);
        // 设置遗嘱消息，帮助检测断开连接
        options.setWill("mqtt/disconnect", ("Client " + clientId + " disconnected").getBytes(), 1, false);
        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel sensorChannel() {
        return new DirectChannel();
    }
    
    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(clientId + "_inbound", mqttClientFactory(),
                        sensorTopics);
        // 增加完成超时时间
        adapter.setCompletionTimeout(10000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        // 提高QoS级别
        adapter.setQos(0);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    @Bean
    public HeaderValueRouter mqttMessageRouter() {
        HeaderValueRouter router = new HeaderValueRouter("mqtt_receivedTopic");
        
        // 使用循环添加所有传感器主题映射
        for (String prefix : SENSOR_TOPIC_PREFIXES) {
            router.setChannelMapping(prefix, "sensorChannel");
        }
        
        // 告警主题 TODO
        return router;
    }
    
    @Bean
    public IntegrationFlow mqttInFlow() {
        return IntegrationFlow
                .from(mqttInputChannel())
                .route(mqttMessageRouter())
                .get();
    }
    
    @Bean
    public MqttPahoMessageHandler mqttOutbound() {
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(clientId + "_outbound", mqttClientFactory());
        messageHandler.setAsync(true);
        messageHandler.setDefaultQos(1);
        return messageHandler;
    }
    
    @Bean
    public IntegrationFlow mqttOutFlow() {
        return IntegrationFlow
                .from(mqttOutboundChannel())
                .handle(mqttOutbound())
                .get();
    }
}