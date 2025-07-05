package com.ling.trigger;

import com.ling.trigger.http.SensorAnalysisController;
import com.ling.types.common.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author: LingRJ
 * @Description: TODO
 * @DateTime: 2025/7/5 10:24
 **/
@SpringBootTest
@Slf4j
public class Analsy {

    @Autowired
    private SensorAnalysisController controller;

    @Test
    public void test_1() {
        Response<String> sensorAnalysisReport = controller.getSensorAnalysisReport();
        log.info(sensorAnalysisReport.getData());
    }
}
