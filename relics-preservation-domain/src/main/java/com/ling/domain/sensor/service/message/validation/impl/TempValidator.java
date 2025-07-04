package com.ling.domain.sensor.service.message.validation.impl;

import com.ling.domain.sensor.service.message.validation.ISensorValidator;

/**
 * @Author: LingRJ
 * @Description: 温度校验
 * @DateTime: 2025/6/30 10:56
 **/
public class TempValidator implements ISensorValidator {
    private final double TEMP_VAL_STAGE1;
    private final double TEMP_VAL_STAGE2;

    public TempValidator(double tempValStage1, double tempValStage2) {
        TEMP_VAL_STAGE1 = tempValStage1;
        TEMP_VAL_STAGE2 = tempValStage2;
    }

    @Override
    public Integer validateStatus(double value) {
        if (value <= TEMP_VAL_STAGE1) {return 0;}
        if (value <= TEMP_VAL_STAGE2) {return 1;}
        return 2;
    }
}

