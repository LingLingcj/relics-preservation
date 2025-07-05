package com.ling.domain.sensor.service.message.validation.impl;

import com.ling.domain.sensor.service.message.validation.ISensorValidator;

/**
 * @Author: LingRJ
 * @Description: 湿度
 * @DateTime: 2025/6/30 15:39
 **/
public class HumValidator implements ISensorValidator {
    private final double HUM_VAL_1;
    private final double HUM_VAL_2;

    public HumValidator(double humVal1, double humVal2) {
        HUM_VAL_1 = humVal1;
        HUM_VAL_2 = humVal2;
    }

    @Override
    public Integer validateStatus(double value) {
        if (value <= HUM_VAL_1) {return 0;}
        if (value <= HUM_VAL_2) {return 1;}
        return 2;
    }

    @Override
    public Double getThreshold(double value) {
        if (value >= HUM_VAL_2) {return HUM_VAL_2;}
        if (value >= HUM_VAL_1) {return HUM_VAL_1;}
        return 0.0;
    }
}
