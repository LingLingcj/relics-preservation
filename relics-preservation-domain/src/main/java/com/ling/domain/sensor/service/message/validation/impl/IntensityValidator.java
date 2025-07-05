package com.ling.domain.sensor.service.message.validation.impl;

import com.ling.domain.sensor.service.message.validation.ISensorValidator;

/**
 * @Author: LingRJ
 * @Description: 光照校验
 * @DateTime: 2025/7/3 15:15
 **/
public class IntensityValidator implements ISensorValidator {
    private final Double IntensityVal;
    public IntensityValidator(Double intensityVal) {
        IntensityVal = intensityVal;
    }

    @Override
    public Integer validateStatus(double value) {
        if (value > IntensityVal) {
            return 1;
        }
        return 0;
    }

    @Override
    public Double getThreshold(double value) {
        if (value > IntensityVal) {return IntensityVal;}
        return 0.0;
    }
}
