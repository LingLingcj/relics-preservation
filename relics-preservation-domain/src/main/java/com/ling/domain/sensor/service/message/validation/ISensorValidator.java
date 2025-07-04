package com.ling.domain.sensor.service.message.validation;

/**
 * @Author: LingRJ
 * @Description: 消息检测接口
 * @DateTime: 2025/6/30 10:48
 **/
public interface ISensorValidator {
    Integer validateStatus(double value);
    Double getThreshold(double value);
}
