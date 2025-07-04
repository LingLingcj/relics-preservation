package com.ling.domain.sensor.service.message.validation;

import com.ling.domain.sensor.service.message.validation.impl.GasValidator;
import com.ling.domain.sensor.service.message.validation.impl.HumValidator;
import com.ling.domain.sensor.service.message.validation.impl.IntensityValidator;
import com.ling.domain.sensor.service.message.validation.impl.TempValidator;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Author: LingRJ
 * @Description: 验证工厂
 * @DateTime: 2025/6/30 11:10
 **/
@Component
public class ValidatorFactory {
    private static final Map<String, ISensorValidator> VALIDATORS = Map.of(
            "gas", new GasValidator(500,600),
            "temp", new TempValidator(35, 40),
            "hum", new HumValidator(50,60),
            "intensity", new IntensityValidator(1000.0)
    );

    public static ISensorValidator getValidator(String sensorType) {
        return VALIDATORS.get(sensorType);
    }

}

