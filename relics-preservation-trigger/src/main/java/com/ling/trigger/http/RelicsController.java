package com.ling.trigger.http;

import com.ling.api.dto.RelicsUploadDTO;
import com.ling.domain.relics.model.entity.RelicsEntity;
import com.ling.domain.relics.model.valobj.RelicsVO;
import com.ling.domain.relics.service.IRelicsService;
import com.ling.types.common.Response;
import com.ling.types.common.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public Response<String> addRelics(@Parameter(description = "文物上传信息", required = true)
                                        @RequestBody RelicsUploadDTO relicsUploadDTO) {
        // DTO转VO
        RelicsVO vo = new RelicsVO();
        org.springframework.beans.BeanUtils.copyProperties(relicsUploadDTO, vo);
        RelicsEntity result = relicsService.uploadRelics(vo);
        return Response.<String>builder()
                .code(result.isSuccess() ? ResponseCode.SUCCESS.getCode() : ResponseCode.SYSTEM_ERROR.getCode())
                .info(result.getMessage())
                .data("上传数据库失败")
                .build();
    }

    @Operation(summary = "按朝代搜索文物", description = "根据朝代名称搜索文物信息")
    @GetMapping("/era")
    public Response<List<RelicsVO>> getRelicsByEra(@Parameter(description = "朝代名称", required = true) @RequestBody String era) {
        List<RelicsEntity> relicsEntities = relicsService.getRelicsByEra(era);
        if (relicsEntities.isEmpty()) {
            return Response.<List<RelicsVO>>builder()
                    .code(ResponseCode.RELICS_NOT_FOUND.getCode())
                    .info("未找到指定朝代的文物")
                    .build();
        }
        List<RelicsVO> relicsVOs = relicsEntities.stream().map(entity -> {
            RelicsVO vo = new RelicsVO();
            org.springframework.beans.BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return Response.<List<RelicsVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("查询成功")
                .data(relicsVOs)
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
