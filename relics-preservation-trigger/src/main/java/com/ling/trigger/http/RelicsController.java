package com.ling.trigger.http;

import com.ling.api.dto.request.RelicsUploadDTO;
import com.ling.api.dto.response.RelicsCommentListResponseDTO;
import com.ling.api.dto.response.RelicsResponseDTO;
import com.ling.api.dto.response.RelicsUploadResponseDTO;
import com.ling.domain.interaction.model.valobj.RelicsCommentListResult;
import com.ling.domain.interaction.service.IUserInteractionService;
import com.ling.domain.relics.model.entity.RelicsEntity;
import com.ling.domain.relics.model.valobj.RelicsVO;
import com.ling.domain.relics.service.IRelicsService;
import com.ling.trigger.converter.RelicsCommentConverter;
import com.ling.types.common.PaginationUtils;
import com.ling.types.common.Response;
import com.ling.types.common.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文物管理控制器
 * @Author: LingRJ
 * @Description: 提供文物基本信息管理和查询功能，包括文物上传、查询、分类获取等
 * @DateTime: 2025/6/28 0:01
 * @Version: 1.0
 */
@Slf4j
@Tag(name = "文物管理", description = "文物基本信息管理接口")
@RestController
@RequestMapping("/api/relics")
public class RelicsController {

    // ==================== 依赖注入 ====================

    @Autowired
    private IRelicsService relicsService;

    @Autowired
    private IUserInteractionService userInteractionService;

    @Autowired
    private RelicsCommentConverter relicsCommentConverter;

    @Operation(summary = "添加文物", description = "添加文物信息，返回文物ID和上传结果")
    @PostMapping
    public Response<RelicsUploadResponseDTO> addRelics(@Parameter(description = "文物上传信息", required = true)
                                        @RequestBody RelicsUploadDTO relicsUploadDTO) {
        try {
            // 参数验证
            if (relicsUploadDTO == null) {
                log.warn("文物上传信息不能为空");
                return Response.<RelicsUploadResponseDTO>builder()
                        .code(ResponseCode.INVALID_PARAM.getCode())
                        .info("文物上传信息不能为空")
                        .build();
            }

            log.info("开始上传文物: name={}", relicsUploadDTO.getName());

            // DTO转VO
            RelicsVO vo = new RelicsVO();
            BeanUtils.copyProperties(relicsUploadDTO, vo);
            RelicsEntity result = relicsService.uploadRelics(vo);

            // 构建响应DTO
            RelicsUploadResponseDTO responseDTO = RelicsUploadResponseDTO.builder()
                    .success(result.isSuccess())
                    .message(result.getMessage())
                    .build();

            log.info("文物上传完成: name={}, success={}", relicsUploadDTO.getName(), result.isSuccess());

            return Response.<RelicsUploadResponseDTO>builder()
                    .code(result.isSuccess() ? ResponseCode.SUCCESS.getCode() : ResponseCode.SYSTEM_ERROR.getCode())
                    .info(result.getMessage())
                    .data(responseDTO)
                    .build();

        } catch (Exception e) {
            log.error("文物上传失败: name={} - {}",
                     relicsUploadDTO != null ? relicsUploadDTO.getName() : "unknown", e.getMessage(), e);
            return Response.<RelicsUploadResponseDTO>builder()
                    .code(ResponseCode.SYSTEM_ERROR.getCode())
                    .info("文物上传失败")
                    .build();
        }
    }

    @Operation(summary = "按朝代搜索文物", description = "根据朝代名称搜索文物信息")
    @GetMapping("/era")
    public Response<Map<String, Object>> getRelicsByEra(@Parameter(description = "朝代名称", required = true )@RequestParam String era) {
        log.info(era);
        List<RelicsEntity> relicsEntities = relicsService.getRelicsByEra(era);
        if (relicsEntities.isEmpty()) {
            return Response.<Map<String, Object>>builder()
                    .code(ResponseCode.RELICS_NOT_FOUND.getCode())
                    .info("未找到指定朝代的文物")
                    .build();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", relicsEntities.size());

        // 转换为DTO
        List<RelicsResponseDTO> relicsDTOs = relicsEntities.stream().map(entity -> RelicsResponseDTO.builder()
            .name(entity.getName())
                .relicsId(entity.getRelicsId())
            .description(entity.getDescription())
            .preservation(entity.getPreservation())
            .category(entity.getCategory())
            .era(entity.getEra())
            .material(entity.getMaterial())
            .imageUrl(entity.getImageUrl())
            .status(entity.getStatus())
            .locationId(entity.getLocationId())
            .build()).collect(Collectors.toList());
        result.put("list", relicsDTOs);

        log.info("{}", result);
        
        return Response.<Map<String, Object>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("查询成功")
                .data(result)
                .build();
    }
    
    @Operation(summary = "按名称搜索文物", description = "根据文物名称关键词搜索文物信息")
    @GetMapping("/name")
    public Response<Map<String, Object>> getRelicsByName(
            @Parameter(description = "文物名称关键词", required = true) @RequestParam String name) {
        log.info("按名称搜索文物，关键词: {}", name);
        
        // 参数验证
        if (name == null || name.trim().isEmpty()) {
            return Response.<Map<String, Object>>builder()
                    .code(ResponseCode.INVALID_PARAM.getCode())
                    .info("搜索关键词不能为空")
                    .build();
        }
        
        List<RelicsEntity> relicsEntities = relicsService.getRelicsByName(name);
        if (relicsEntities.isEmpty()) {
            log.info("未找到符合条件的文物，关键词: {}", name);
            return Response.<Map<String, Object>>builder()
                    .code(ResponseCode.RELICS_NOT_FOUND.getCode())
                    .info("未找到匹配的文物")
                    .build();
        }
        
        // 构建响应数据
        Map<String, Object> result = new HashMap<>();
        result.put("total", relicsEntities.size());
        
        // 转换为DTO
        List<RelicsResponseDTO> relicsDTOs = relicsEntities.stream().map(entity -> {
            RelicsResponseDTO dto = new RelicsResponseDTO();
            BeanUtils.copyProperties(entity, dto);
            return dto;
        }).collect(Collectors.toList());
        result.put("list", relicsDTOs);
        
        log.info("按名称搜索成功，关键词: {}, 找到{}条记录", name, relicsEntities.size());
        return Response.<Map<String, Object>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("搜索成功")
                .data(result)
                .build();
    }

    @Operation(summary = "按Id搜索文物", description = "根据Id搜索文物信息")
    @GetMapping("/id")
    public Response<RelicsResponseDTO> getRelics(
            @Parameter(description = "文物ID", required = true) @RequestParam Long id) {
        log.info("查询文物，ID: {}", id);
        
        // 参数验证
        if (id == null || id <= 0) {
            log.warn("无效的文物ID: {}", id);
            return Response.<RelicsResponseDTO>builder()
                    .code(ResponseCode.SYSTEM_ERROR.getCode())
                    .info("无效的文物ID")
                    .build();
        }
        
        // 调用服务获取文物信息
        RelicsEntity relicsEntity = relicsService.getRelicsById(id);
        
        // 处理文物不存在的情况
        if (relicsEntity == null) {
            log.warn("文物不存在，ID: {}", id);
            return Response.<RelicsResponseDTO>builder()
                    .code(ResponseCode.RELICS_NOT_FOUND.getCode())
                    .info(ResponseCode.RELICS_NOT_FOUND.getInfo())
                    .build();
        }
        
        // 检查操作结果
        if (!relicsEntity.isSuccess()) {
            log.error("获取文物失败，ID: {}，错误信息: {}", id, relicsEntity.getMessage());
            return Response.<RelicsResponseDTO>builder()
                    .code(ResponseCode.SYSTEM_ERROR.getCode())
                    .info(relicsEntity.getMessage())
                    .build();
        }
        
        // 转换为DTO并返回
        RelicsResponseDTO relicsResponseDTO = new RelicsResponseDTO();
        BeanUtils.copyProperties(relicsEntity, relicsResponseDTO);
        
        log.info("文物查询成功，ID: {}, 名称: {}", id, relicsEntity.getName());
        return Response.<RelicsResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取成功")
                .data(relicsResponseDTO)
                .build();
    }

    @Operation(summary = "获取文物评论列表")
    @GetMapping("/{id}/comments")
    public Response<RelicsCommentListResponseDTO> getRelicsComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            if (!isValidRelicsId(id)) {
                return Response.<RelicsCommentListResponseDTO>builder()
                        .code(ResponseCode.INVALID_PARAM.getCode())
                        .info("文物ID无效")
                        .build();
            }

            PaginationUtils.PaginationParams params = PaginationUtils.validateAndNormalize(page, size);
            RelicsCommentListResult result = userInteractionService.getRelicsComments(
                    id, params.getPage(), params.getSize());

            RelicsCommentListResponseDTO responseDTO = relicsCommentConverter.toRelicsCommentListResponseDTO(result);
            String message = result.isEmpty() ? "暂无评论" : "查询成功";

            return Response.<RelicsCommentListResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(message)
                    .data(responseDTO)
                    .build();

        } catch (Exception e) {
            log.error("获取文物评论失败: relicsId={}", id, e);
            return Response.<RelicsCommentListResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取评论列表失败")
                    .build();
        }
    }

    // ==================== 私有工具方法 ====================

    /**
     * 验证文物ID是否有效
     * @param relicsId 文物ID
     * @return 是否有效
     */
    private boolean isValidRelicsId(Long relicsId) {
        return relicsId != null && relicsId > 0;
    }

}
