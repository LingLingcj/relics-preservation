package com.ling.domain.sensor.service.sensor;

import com.ling.domain.sensor.model.valobj.SensorMessageVO;
import java.util.List;

/**
 * @Author: LingRJ
 * @Description: 传感器消息服务接口
 * @DateTime: 2025/6/28 22:09
 **/
public interface ISensorMessageService {
    
    /**
     * 处理多个消息
     * @param topic 主题
     * @param messages 消息列表
     */
    void processSensorMessages(String topic, List<SensorMessageVO> messages);
}
