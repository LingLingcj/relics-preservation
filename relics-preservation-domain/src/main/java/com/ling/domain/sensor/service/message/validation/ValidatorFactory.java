package com.ling.domain.sensor.service.message.validation;

import com.ling.domain.sensor.service.message.validation.impl.GasValidator;
import com.ling.domain.sensor.service.message.validation.impl.HumValidator;
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
            "gas", new GasValidator(1000,1200),
            "temp", new TempValidator(10, 30),
            "hum", new HumValidator(40,60)
    );

    public static ISensorValidator getValidator(String sensorType) {
        return VALIDATORS.get(sensorType);
    }

}

