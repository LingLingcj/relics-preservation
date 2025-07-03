package com.ling.domain.sensor.service.message.validation.impl;

import com.ling.domain.sensor.service.message.validation.ISensorValidator;

/**
 * @Author: LingRJ
 * @Description: 光照校验
 * @DateTime: 2025/7/3 15:15
 **/
public class IntensityValidator implements ISensorValidator {
    @Override
    public Integer validateStatus(double value) {
        return 0;
    }
}
