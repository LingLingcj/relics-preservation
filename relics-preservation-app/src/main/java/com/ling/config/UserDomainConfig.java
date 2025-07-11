package com.ling.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 用户领域配置
 * @Author: LingRJ
 * @Description: 配置用户领域相关的组件扫描和Bean注册
 * @DateTime: 2025/7/11
 */
@Configuration
@ComponentScan(basePackages = {
    "com.ling.domain.user.service.impl",
    "com.ling.domain.user.adapter",
    "com.ling.domain.user.event.handler"
})
public class UserDomainConfig {
    
    // 这个配置类确保用户领域的所有组件都被Spring扫描到
    // 包括：
    // - 用户服务实现类
    // - 用户仓储实现类
    // - 用户事件处理器
}
