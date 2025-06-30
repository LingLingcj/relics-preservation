package com.ling.domain.sensor.service.message.validation.impl;

import com.ling.domain.sensor.service.message.validation.ISensorValidator;

/**
 * @Author: LingRJ
 * @Description: 气体浓度检测
 * @DateTime: 2025/6/30 10:50
 **/
public class GasValidator implements ISensorValidator {
    private final double thresholdStage1;
    private final double thresholdStage2;

    public GasValidator(double thresholdStage1, double thresholdStage2) {
        this.thresholdStage1 = thresholdStage1;
        this.thresholdStage2 = thresholdStage2;
    }

    @Override
    public Integer validateStatus(double value) {
        if (value >= this.thresholdStage1 && value <= this.thresholdStage2) {
            return 1;
        }
        if (value >= this.thresholdStage2) {
            return 2;
        }
        return 0;
    }
}
