package com.ling.trigger.http;

import com.ling.api.dto.response.RelicsResponseDTO;
import com.ling.domain.relics.model.entity.RelicsEntity;
import com.ling.domain.relics.service.IRelicsService;
import com.ling.types.common.Response;
import com.ling.types.common.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: LingRJ
 * @Description: 首页接口
 * @DateTime: 2025/6/30 18:00
 **/
@Tag(name = "首页", description = "首页展示相关接口")
@RestController
@RequestMapping("/api/index")
public class IndexController {

    @Autowired
    private IRelicsService relicsService;

    @Operation(summary = "随机获取文物", description = "为首页随机获取指定数量的文物信息")
    @GetMapping("/random-relics")
    public Response<Map<String, Object>> getRandomRelics(
            @Parameter(description = "获取数量", required = false) 
            @RequestParam(defaultValue = "6") Integer count) {

        Map<String, Object> result = new HashMap<>();
        
        // 调用服务获取随机文物
        List<RelicsEntity> relicsEntities = relicsService.getRandomRelics(count);

        // 转换为DTO
        List<RelicsResponseDTO> relicsResponseDTOs = relicsEntities.stream()
                .map(entity -> {
                    RelicsResponseDTO dto = new RelicsResponseDTO();
                    BeanUtils.copyProperties(entity, dto);
                    return dto;
                })
                .collect(Collectors.toList());

        result.put("total", relicsResponseDTOs.size());
        result.put("list", relicsResponseDTOs);
        
        return Response.<Map<String, Object>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取成功")
                .data(result)
                .build();
    }
    
    @Operation(summary = "传感器数据监控页面", description = "提供传感器数据实时监控页面")
    @GetMapping(value = "/sensor-charts", produces = MediaType.TEXT_HTML_VALUE)
    public String getSensorChartsPage() throws IOException {
        // 从classpath加载静态页面
        Resource resource = new ClassPathResource("static/sensor-charts.html");
        if (resource.exists()) {
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } else {
            return "<html><body><h1>页面不存在</h1></body></html>";
        }
    }
    
    @Operation(summary = "传感器报警监控页面", description = "提供传感器报警实时监控页面")
    @GetMapping(value = "/sensor-alerts", produces = MediaType.TEXT_HTML_VALUE)
    public String getSensorAlertsPage() throws IOException {
        // 从classpath加载静态页面
        Resource resource = new ClassPathResource("static/alerts.html");
        if (resource.exists()) {
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } else {
            return "<html><body><h1>页面不存在</h1></body></html>";
        }
    }
}
