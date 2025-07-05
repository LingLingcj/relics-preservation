package com.ling.trigger.http;

import com.ling.api.dto.request.SensorControlRequestDTO;
import com.ling.trigger.gateway.MqttGateway;
import com.ling.types.common.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: LingRJ
 * @Description: 传感器控制
 * @DateTime: 2025/7/5 10:48
 **/
@RestController
@RequestMapping("/api/sensor/value")
@Slf4j
public class SensorController {
    @Autowired
    private MqttGateway mqttGateway;

    @PostMapping
    public Response<Void> sendSensorValue(@RequestBody SensorControlRequestDTO requestDTO) {

        try {
            String type = requestDTO.getSensorType();
            if ("fan".equals(type)) {
                mqttGateway.sendToMqtt(String.valueOf(requestDTO.getValue()), "fan_1");
            }
            if ("led".equals(type)) {
                mqttGateway.sendToMqtt(String.valueOf(requestDTO.getValue()), "led_1");
            }
            return Response.success(null);
        } catch (Exception e) {
            return Response.error(null);

        }
    }
}
