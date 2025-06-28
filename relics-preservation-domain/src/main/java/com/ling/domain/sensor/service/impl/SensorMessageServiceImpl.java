package com.ling.domain.sensor.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ling.domain.sensor.service.ISensorMessageService;
import com.ling.domain.sensor.model.valobj.SensorMessageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Author: LingRJ
 * @Description: 传感器消息服务实现
 * @DateTime: 2025/6/28
 **/
@Service
@Slf4j
public class SensorMessageServiceImpl implements ISensorMessageService {
    
    @Override
    public void processSensorMessage(String topic, String payload) {
        log.info("接收到传感器消息，主题: {}，内容: {}", topic, payload);
        
        try {
            // 解析传感器消息
            SensorMessageVO sensorMessage = JSON.parseObject(payload, SensorMessageVO.class);
            
            // 处理传感器数据
            // 1. 存储数据
            // 2. 检查是否触发告警
            // 3. 更新传感器状态
            
            log.info("处理传感器消息成功: {}", sensorMessage);
        } catch (Exception e) {
            log.error("处理传感器消息失败: {}", e.getMessage(), e);
        }
    }
} 