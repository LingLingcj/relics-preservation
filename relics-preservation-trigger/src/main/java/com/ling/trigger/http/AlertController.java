package com.ling.trigger.http;

import com.ling.api.dto.request.AlertQueryDTO;
import com.ling.api.dto.response.AlertResponseDTO;
import com.ling.domain.sensor.service.alert.IAlertService;
import com.ling.domain.sensor.service.notification.model.AlertNotification;
import com.ling.types.common.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: LingRJ
 * @Description: 告警接口
 * @DateTime: 2025/6/28 0:03
 **/
@RestController
@RequestMapping("/api/alert")
public class AlertController {
    
    @Autowired
    private IAlertService alertService;

    @GetMapping("/list")
    public Response<List<AlertResponseDTO>> queryAlerts(AlertQueryDTO queryDTO) {
        List<AlertNotification> alerts = alertService.queryAlerts(
                queryDTO.getSensorId(),
                queryDTO.getAlertType(),
                queryDTO.getStatus(),
                queryDTO.getStartTime(),
                queryDTO.getEndTime(),
                100
        );
        
        List<AlertResponseDTO> result = alerts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return Response.success(result);
    }

    @PutMapping("/{alertId}/status")
    public Response<Boolean> updateAlertStatus(
            @PathVariable String alertId,
            @RequestParam String status) {
        boolean success = alertService.updateAlertStatus(alertId, status);
        return Response.success(success);
    }
    
    /**
     * 将告警通知转换为响应DTO
     * @param notification 告警通知
     * @return 告警响应DTO
     */
    private AlertResponseDTO convertToDTO(AlertNotification notification) {
        return AlertResponseDTO.builder()
                .sensorId(notification.getSensorId())
                .alertType(notification.getAlertType())
                .message(notification.getMessage())
                .relicsId(notification.getRelicsId())
                .currentValue(notification.getValue())
                .threshold(notification.getThreshold())
                .status(notification.getStatus())
                .timestamp(notification.getTimestamp())
                .build();
    }
}

