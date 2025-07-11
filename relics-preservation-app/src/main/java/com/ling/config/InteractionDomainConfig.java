package com.ling.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 交互领域配置
 * @Author: LingRJ
 * @Description: 配置交互领域相关的组件扫描和Bean注册
 * @DateTime: 2025/7/11
 */
@Configuration
@ComponentScan(basePackages = {
    "com.ling.domain.interaction.service.impl",
    "com.ling.domain.interaction.adapter",
    "com.ling.domain.interaction.event.handler",
    "com.ling.infrastructure.repository"
})
public class InteractionDomainConfig {
    
    // 这个配置类确保交互领域的所有组件都被Spring扫描到
    // 包括：
    // - 交互服务实现类
    // - 交互仓储实现类
    // - 交互事件处理器
    // - Infrastructure层的仓储实现
}
