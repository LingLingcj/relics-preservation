package com.ling.domain.sensor.service.validation;

import com.ling.domain.sensor.service.validation.impl.GasValidator;
import com.ling.domain.sensor.service.validation.impl.TempValidator;

import java.util.Map;

/**
 * @Author: LingRJ
 * @Description: 验证工厂
 * @DateTime: 2025/6/30 11:10
 **/
public class ValidatorFactory {
    private static final Map<String, ISensorValidator> validators = Map.of(
            "gas", new GasValidator(1000,1200),
            "temp", new TempValidator(10, 30)
    );

    public static ISensorValidator getValidator(String sensorType) {
        return validators.get(sensorType);
    }

}
