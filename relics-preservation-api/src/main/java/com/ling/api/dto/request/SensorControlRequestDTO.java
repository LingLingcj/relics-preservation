package com.ling.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: LingRJ
 * @Description: 传感器控制
 * @DateTime: 2025/7/5 10:51
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SensorControlRequestDTO {
    private String sensorType;
    private Integer value;
}
