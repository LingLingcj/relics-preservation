package com.ling.trigger.http;

import com.ling.api.dto.response.RelicsResponseDTO;
import com.ling.domain.relics.model.entity.RelicsEntity;
import com.ling.domain.relics.service.IRelicsSearchService;
import com.ling.types.common.Response;
import com.ling.types.common.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文物搜索控制器
 */
@Slf4j
@Tag(name = "文物搜索", description = "文物搜索接口")
@RestController
@RequestMapping("/api/relics/search")
public class RelicsSearchController {

    @Autowired
    private IRelicsSearchService relicsSearchService;

    @Operation(summary = "按名称搜索文物", description = "根据名称关键词搜索文物信息")
    @GetMapping("/name")
    public Response<Map<String, Object>> searchRelicsByName(
            @Parameter(description = "文物名称关键词", required = true) @RequestParam String keyword) {
        log.info("按名称搜索文物, 关键词: {}", keyword);
        
        // 参数验证
        if (keyword == null || keyword.trim().isEmpty()) {
            return Response.<Map<String, Object>>builder()
                    .code(ResponseCode.INVALID_PARAM.getCode())
                    .info("搜索关键词不能为空")
                    .build();
        }
        
        // 调用搜索服务
        List<RelicsEntity> relicsEntities = relicsSearchService.searchRelicsByName(keyword);
        
        if (relicsEntities.isEmpty()) {
            return Response.<Map<String, Object>>builder()
                    .code(ResponseCode.RELICS_NOT_FOUND.getCode())
                    .info("未找到匹配的文物")
                    .build();
        }
        
        // 构建响应数据
        Map<String, Object> result = new HashMap<>();
        
        List<RelicsResponseDTO> relicsDTOs = new ArrayList<>();

        for (RelicsEntity relicsEntity : relicsEntities) {
            relicsDTOs.add(
                    RelicsResponseDTO.builder()
                            .relicsId(relicsEntity.getRelicsId())
                            .name(relicsEntity.getName())
                            .era(relicsEntity.getEra())
                            .category(relicsEntity.getCategory())
                            .description(relicsEntity.getDescription())
                            .imageUrl(relicsEntity.getImageUrl())
                            .locationId(relicsEntity.getLocationId())
                            .preservation(relicsEntity.getPreservation())
                            .material(relicsEntity.getMaterial())
                            .status(relicsEntity.getStatus())
                            .build()
            );
        }
        
        result.put("total", relicsDTOs.size());
        result.put("list", relicsDTOs);
        
        log.info("搜索成功，关键词: {}, 找到{}条记录", keyword, relicsDTOs.size());
        return Response.<Map<String, Object>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("搜索成功")
                .data(result)
                .build();
    }
    
    @Operation(summary = "多字段搜索文物", description = "根据关键词搜索文物名称、朝代、类别、描述、材质等字段")
    @GetMapping("/keyword")
    public Response<Map<String, Object>> searchRelicsByKeyword(
            @Parameter(description = "搜索关键词", required = true) @RequestParam String keyword) {
        log.info("多字段搜索文物, 关键词: {}", keyword);
        
        // 参数验证
        if (keyword == null || keyword.trim().isEmpty()) {
            return Response.<Map<String, Object>>builder()
                    .code(ResponseCode.INVALID_PARAM.getCode())
                    .info("搜索关键词不能为空")
                    .build();
        }
        
        // 调用搜索服务
        List<RelicsEntity> relicsEntities = relicsSearchService.searchRelicsByKeyword(keyword);
        
        if (relicsEntities.isEmpty()) {
            return Response.<Map<String, Object>>builder()
                    .code(ResponseCode.RELICS_NOT_FOUND.getCode())
                    .info("未找到匹配的文物")
                    .build();
        }
        
        // 构建响应数据
        Map<String, Object> result = new HashMap<>();
        
        List<RelicsResponseDTO> relicsDTOs = new ArrayList<>();

        for (RelicsEntity relicsEntity : relicsEntities) {
            relicsDTOs.add(
                    RelicsResponseDTO.builder()
                            .relicsId(relicsEntity.getRelicsId())
                            .name(relicsEntity.getName())
                            .era(relicsEntity.getEra())
                            .category(relicsEntity.getCategory())
                            .description(relicsEntity.getDescription())
                            .imageUrl(relicsEntity.getImageUrl())
                            .locationId(relicsEntity.getLocationId())
                            .preservation(relicsEntity.getPreservation())
                            .material(relicsEntity.getMaterial())
                            .status(relicsEntity.getStatus())
                            .build()
            );
        }
        
        result.put("total", relicsDTOs.size());
        result.put("list", relicsDTOs);
        
        log.info("多字段搜索成功，关键词: {}, 找到{}条记录", keyword, relicsDTOs.size());
        return Response.<Map<String, Object>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("搜索成功")
                .data(result)
                .build();
    }
    
    @Operation(summary = "同步文物到ES", description = "同步所有文物数据到Elasticsearch")
    @PostMapping("/sync")
    public Response<Boolean> syncRelicsToEs() {
        log.info("开始同步文物数据到Elasticsearch");
        
        boolean result = relicsSearchService.syncAllRelicsToEs();
        
        if (result) {
            log.info("文物数据同步成功");
            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("同步成功")
                    .data(true)
                    .build();
        } else {
            log.error("文物数据同步失败");
            return Response.<Boolean>builder()
                    .code(ResponseCode.SYSTEM_ERROR.getCode())
                    .info("同步失败")
                    .data(false)
                    .build();
        }
    }
} 