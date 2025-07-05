package com.ling.trigger;

import com.ling.api.dto.request.SensorControlRequestDTO;
import com.ling.trigger.http.SensorAnalysisController;
import com.ling.trigger.http.SensorController;
import com.ling.types.common.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Author: LingRJ
 * @Description: TODO
 * @DateTime: 2025/7/5 11:01
 **/
@SpringBootTest
@Slf4j
@RunWith(SpringRunner.class)
public class Sensor {

    @Autowired
    private SensorController sensorController;
    @Autowired
    private SensorAnalysisController sensorAnalysisController;

    @Test
    public void test() {
        SensorControlRequestDTO requestDTO = new SensorControlRequestDTO();
        requestDTO.setSensorType("fan");
        requestDTO.setValue(10);
        sensorController.sendSensorValue(requestDTO);
    }

    @Test
    public void analysis() {
        Response<String> sensorAnalysisReport = sensorAnalysisController.getSensorAnalysisReport();
        log.info(sensorAnalysisReport.getData());
    }
}
