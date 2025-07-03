package com.ling.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;

import java.security.Principal;
import java.util.Map;

/**
 * @Author: LingRJ
 * @Description: websocket配置
 * @DateTime: 2025/7/2 9:12
 **/
@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 允许所有域名访问，添加多个端点以提高兼容性
        registry.addEndpoint("/ws", "/websocket", "/socket")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                // 禁用JSESSIONID cookie以提高穿透兼容性
                .setSessionCookieNeeded(false)
                // 增加心跳时间
                .setHeartbeatTime(25000);
                
        // 添加一个不使用SockJS的端点，用于原生WebSocket连接
        registry.addEndpoint("/ws", "/websocket", "/socket")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .setHandshakeHandler(new DefaultHandshakeHandler());
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 用户可以订阅的主题前缀
        registry.enableSimpleBroker("/topic", "/queue");
        // 发送消息的前缀
        registry.setApplicationDestinationPrefixes("/app");
        // 增加用户目的地前缀
        registry.setUserDestinationPrefix("/user");
    }
}
