package com.ling.domain.sensor.service.validation.impl;

import com.ling.domain.sensor.service.validation.ISensorValidator;

/**
 * @Author: LingRJ
 * @Description: 温度校验
 * @DateTime: 2025/6/30 10:56
 **/
public class TempValidator implements ISensorValidator {
    private final double MIN_TEMP_VAL;
    private final double MAX_TEMP_VAL;

    public TempValidator(double minTempVal, double maxTempVal) {
        MIN_TEMP_VAL = minTempVal;
        MAX_TEMP_VAL = maxTempVal;
    }

    @Override
    public Integer validateStatus(double value) {
        return 0;
    }
}

