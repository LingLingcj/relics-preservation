package com.ling.domain.sensor.service.validation.impl;

import com.ling.domain.sensor.service.validation.ISensorValidator;

/**
 * @Author: LingRJ
 * @Description: 湿度
 * @DateTime: 2025/6/30 15:39
 **/
public class HumValidator implements ISensorValidator {
    private final double MIN_HUM_VAL;
    private final double MAX_HUM_VAL;

    public HumValidator(double minHumVal, double maxHumVal) {
        MIN_HUM_VAL = minHumVal;
        MAX_HUM_VAL = maxHumVal;
    }

    @Override
    public Integer validateStatus(double value) {
        return 0;
    }
}
