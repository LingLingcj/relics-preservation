package com.ling.trigger.http;

import com.ling.domain.sensor.model.valobj.SensorMessageVO;
import com.ling.domain.sensor.service.core.ISensorDataService;
import com.ling.types.common.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: LingRJ
 * @Description: 传感器数据控制器
 * @DateTime: 2025/7/8
 **/
@RestController
@RequestMapping("/api/sensor/data")
@Slf4j
public class SensorDataController {

    @Autowired
    private ISensorDataService sensorDataService;

    /**
     * 获取各种传感器类型的最新数据
     * 每种传感器类型返回5条最新数据
     * @return 各类型传感器数据
     */
    @GetMapping("/recent")
    public Response<Map<String, List<SensorMessageVO>>> getRecentSensorData() {
        try {
            // 定义要查询的传感器类型列表
            String[] sensorTypes = {"gas", "temp", "hum", "intensity"};
            
            // 创建结果集合
            Map<String, List<SensorMessageVO>> result = new HashMap<>();
            
            // 对每种传感器类型执行查询
            for (String type : sensorTypes) {
                // 直接查询指定类型的5条最新传感器数据，不使用时间范围过滤
                // 数据库查询已按时间戳降序排序，所以会返回最新的数据
                List<SensorMessageVO> data = sensorDataService.querySensorData(
                        null,  // 不限制传感器ID
                        type,  // 传感器类型
                        null,  // 不限制开始时间
                        null,  // 不限制结束时间
                        5      // 限制5条数据
                );
                
                // 将查询结果添加到结果集合
                result.put(type, data);
            }
            
            return Response.success(result);
        } catch (Exception e) {
            log.error("获取最近传感器数据失败: {}", e.getMessage(), e);
            return Response.error(null);
        }
    }
} 