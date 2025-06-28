package com.ling.trigger.http.test;

import com.alibaba.fastjson2.JSON;
import com.ling.domain.sensor.model.valobj.AlertMessageVO;
import com.ling.domain.sensor.model.valobj.SensorMessageVO;
import com.ling.trigger.gateway.MqttGateway;
import com.ling.types.common.Response;
import com.ling.types.common.ResponseCode;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @Author: LingRJ
 * @Description: MQTT测试控制器
 * @DateTime: 2025/6/28
 **/
@RestController
@RequestMapping("/api/mqtt/test")
@Slf4j
public class MqttTestController {

    @Resource
    private MqttGateway mqttGateway;
    
    /**
     * 创建并发送测试传感器数据
     * @param request 测试请求
     * @return 响应
     */
    @PostMapping("/sensor")
    public Response<SensorMessageVO> createTestSensorData(@RequestBody TestSensorRequest request) {
        log.info("创建测试传感器数据: {}", request);
        
        // 构建传感器测试数据
        SensorMessageVO sensorMessage = new SensorMessageVO();
        sensorMessage.setSensorId(request.getSensorId() != null ? request.getSensorId() : UUID.randomUUID().toString());
        sensorMessage.setSensorType(request.getSensorType());
        sensorMessage.setValue(request.getValue());
        sensorMessage.setUnit(request.getUnit());
        sensorMessage.setTimestamp(LocalDateTime.now());
        sensorMessage.setLocationId(request.getLocationId());
        sensorMessage.setRelicsId(request.getRelicsId());
        
        log.info("生成测试传感器数据: {}", sensorMessage);
        
        // 发送MQTT消息
        try {
            String topic = "sensor/" + sensorMessage.getSensorId() + "/data";
            String payload = JSON.toJSONString(sensorMessage);
            mqttGateway.sendToMqtt(payload, topic);
            log.info("MQTT传感器消息发送成功，主题: {}", topic);
        } catch (Exception e) {
            log.error("MQTT传感器消息发送失败: {}", e.getMessage(), e);
            return Response.<SensorMessageVO>builder()
                    .code(ResponseCode.SYSTEM_ERROR.getCode())
                    .info("MQTT消息发送失败: " + e.getMessage())
                    .data(sensorMessage)
                    .build();
        }
        
        return Response.<SensorMessageVO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(sensorMessage)
                .build();
    }
    
    /**
     * 创建并发送测试告警数据
     * @param request 测试请求
     * @return 响应
     */
    @PostMapping("/alert")
    public Response<AlertMessageVO> createTestAlertData(@RequestBody TestAlertRequest request) {
        log.info("创建测试告警数据: {}", request);
        
        // 构建告警测试数据
        AlertMessageVO alertMessage = new AlertMessageVO();
        alertMessage.setAlertId(UUID.randomUUID().toString());
        alertMessage.setAlertType(request.getAlertType());
        alertMessage.setSeverity(request.getSeverity());
        alertMessage.setMessage(request.getMessage());
        alertMessage.setTimestamp(LocalDateTime.now());
        alertMessage.setSensorId(request.getSensorId());
        alertMessage.setSensorType(request.getSensorType());
        alertMessage.setLocationId(request.getLocationId());
        alertMessage.setRelicsId(request.getRelicsId());
        alertMessage.setCurrentReading(request.getCurrentReading());
        alertMessage.setThreshold(request.getThreshold());
        
        log.info("生成测试告警数据: {}", alertMessage);
        
        // 发送MQTT消息
        try {
            String topic = "alert/" + alertMessage.getAlertType() + "/" + alertMessage.getSensorId();
            String payload = JSON.toJSONString(alertMessage);
            mqttGateway.sendToMqtt(payload, topic);
            log.info("MQTT告警消息发送成功，主题: {}", topic);
        } catch (Exception e) {
            log.error("MQTT告警消息发送失败: {}", e.getMessage(), e);
            return Response.<AlertMessageVO>builder()
                    .code(ResponseCode.SYSTEM_ERROR.getCode())
                    .info("MQTT消息发送失败: " + e.getMessage())
                    .data(alertMessage)
                    .build();
        }
        
        return Response.<AlertMessageVO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(alertMessage)
                .build();
    }
    
    @Data
    public static class TestSensorRequest {
        private String sensorId;
        private String sensorType;
        private Double value;
        private String unit;
        private String locationId;
        private String relicsId;
    }
    
    @Data
    public static class TestAlertRequest {
        private String alertType;
        private String severity;
        private String message;
        private String sensorId;
        private String sensorType;
        private String locationId;
        private String relicsId;
        private Double currentReading;
        private Double threshold;
    }
} 