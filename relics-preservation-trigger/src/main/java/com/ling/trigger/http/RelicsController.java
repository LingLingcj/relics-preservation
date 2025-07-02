package com.ling.trigger.http;

import com.ling.api.dto.request.RelicsUploadDTO;
import com.ling.api.dto.response.CommentResponseDTO;
import com.ling.api.dto.response.RelicsResponseDTO;
import com.ling.api.dto.response.RelicsUploadResponseDTO;
import com.ling.domain.comment.model.entity.CommentEntity;
import com.ling.domain.comment.service.ICommentService;
import com.ling.domain.relics.model.entity.RelicsEntity;
import com.ling.domain.relics.model.valobj.RelicsVO;
import com.ling.domain.relics.service.IRelicsService;
import com.ling.types.common.Response;
import com.ling.types.common.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: LingRJ
 * @Description: 文物基本信息
 * @DateTime: 2025/6/28 0:01
 **/
@Slf4j
@Tag(name = "文物管理", description = "文物基本信息管理接口")
@RestController
@RequestMapping("/api/relics")
public class RelicsController {
    @Autowired
    private IRelicsService relicsService;
    
    @Autowired
    private ICommentService commentService;

    @Operation(summary = "添加文物", description = "添加文物信息，返回文物ID和上传结果")
    @PostMapping
    public Response<RelicsUploadResponseDTO> addRelics(@Parameter(description = "文物上传信息", required = true)
                                        @RequestBody RelicsUploadDTO relicsUploadDTO) {
        // DTO转VO
        RelicsVO vo = new RelicsVO();
        org.springframework.beans.BeanUtils.copyProperties(relicsUploadDTO, vo);
        RelicsEntity result = relicsService.uploadRelics(vo);
        
        // 构建响应DTO
        RelicsUploadResponseDTO responseDTO = RelicsUploadResponseDTO.builder()
                .success(result.isSuccess())
                .message(result.getMessage())
                .build();
        
        return Response.<RelicsUploadResponseDTO>builder()
                .code(result.isSuccess() ? ResponseCode.SUCCESS.getCode() : ResponseCode.SYSTEM_ERROR.getCode())
                .info(result.getMessage())
                .data(responseDTO)
                .build();
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

    @Operation(summary = "按Id搜索文物", description = "根据Id搜索文物信息")
    @GetMapping("/id")
    public Response<RelicsResponseDTO> getRelics(@RequestParam Long id) {
        RelicsEntity relicsEntity = relicsService.getRelicsById(id);
        RelicsResponseDTO relicsResponseDTO = new RelicsResponseDTO();
        BeanUtils.copyProperties(relicsEntity, relicsResponseDTO);

        return Response.<RelicsResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取成功")
                .data(relicsResponseDTO)
                .build();
    }

    @Operation(summary = "获取文物评论", description = "获取指定文物的评论列表")
    @GetMapping("/{relicsId}/comments")
    public Response<List<CommentResponseDTO>> getRelicsComments(
            @Parameter(description = "文物ID", required = true) @PathVariable Long relicsId) {
        
        // 获取当前用户名
        String currentUsername = getCurrentUsername();
        
        // 获取评论列表
        List<CommentEntity> comments = commentService.getCommentsByRelicsId(relicsId);
        
        // 转换为DTO
        List<CommentResponseDTO> commentDTOs = comments.stream()
                .map(entity -> {
                    CommentResponseDTO dto = new CommentResponseDTO();
                    BeanUtils.copyProperties(entity, dto);
                    dto.setIsOwner(entity.getUsername().equals(currentUsername));
                    return dto;
                })
                .collect(Collectors.toList());
        
        return Response.<List<CommentResponseDTO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("查询成功")
                .data(commentDTOs)
                .build();
    }
    
    /**
     * 获取当前登录用户名
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return principal.toString();
    }

//    // 获取文物详情
//    @GetMapping("/{id}")
//    public Response<RelicsInfo> getRelicsById(@PathVariable Long id);
//
//    // 文物列表查询
//    @GetMapping
//    public Response<PageResult<RelicsInfo>> listRelics(RelicsQueryParam param);
//
//    // 更新文物信息
//    @PutMapping("/{id}")
//    public Response<Boolean> updateRelics(@PathVariable Long id, @RequestBody RelicsUpdateVO updateVO);
}
