package com.ling.trigger.http;

import com.ling.api.dto.request.RelicsUploadDTO;
import com.ling.api.dto.response.RelicsResponseDTO;
import com.ling.api.dto.response.RelicsUploadResponseDTO;
import com.ling.domain.relics.model.entity.RelicsEntity;
import com.ling.domain.relics.model.valobj.RelicsVO;
import com.ling.domain.relics.service.IRelicsService;
import com.ling.types.common.Response;
import com.ling.types.common.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: LingRJ
 * @Description: 文物基本信息
 * @DateTime: 2025/6/28 0:01
 **/
@Tag(name = "文物管理", description = "文物基本信息管理接口")
@RestController
@RequestMapping("/api/relics")
public class RelicsController {
    @Autowired
    private IRelicsService relicsService;

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
    public Response<List<RelicsResponseDTO>> getRelicsByEra(@Parameter(description = "朝代名称", required = true) @RequestBody Map<String, String> body) {
        String era = body.get("era");
        List<RelicsEntity> relicsEntities = relicsService.getRelicsByEra(era);
        if (relicsEntities.isEmpty()) {
            return Response.<List<RelicsResponseDTO>>builder()
                    .code(ResponseCode.RELICS_NOT_FOUND.getCode())
                    .info("未找到指定朝代的文物")
                    .build();
        }
        
        // 转换为DTO
        List<RelicsResponseDTO> relicsDTOs = relicsEntities.stream().map(entity -> {
            RelicsResponseDTO dto = RelicsResponseDTO.builder()
                    .name(entity.getName())
                    .description(entity.getDescription())
                    .preservation(entity.getPreservation())
                    .category(entity.getCategory())
                    .era(entity.getEra())
                    .material(entity.getMaterial())
                    .imageUrl(entity.getImageUrl())
                    .status(entity.getStatus())
                    .locationId(entity.getLocationId())
                    .build();
            return dto;
        }).collect(Collectors.toList());
        
        return Response.<List<RelicsResponseDTO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("查询成功")
                .data(relicsDTOs)
                .build();
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
