package com.ling.trigger.http.adapter;

import com.ling.api.dto.request.FavoriteAddDTO;
import com.ling.api.dto.response.FavoriteResponseDTO;
import com.ling.domain.interaction.model.valobj.InteractionResult;
import com.ling.domain.interaction.service.IUserInteractionService;
import com.ling.types.common.Response;
import com.ling.types.common.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;

/**
 * 收藏控制器（向后兼容）
 * @Author: LingRJ
 * @Description: 保持与原有收藏API的兼容性，内部委托给新的交互服务
 * @DateTime: 2025/7/11
 */
@RestController
@RequestMapping("/api/v1/favorites")
@Tag(name = "收藏管理（兼容）", description = "文物收藏功能API（向后兼容版本）")
@Slf4j
public class LegacyFavoriteController {
    
    @Autowired
    private IUserInteractionService userInteractionService;
    
    @Operation(summary = "添加收藏", description = "用户收藏指定文物")
    @PostMapping
    public Response<FavoriteResponseDTO> addFavorite(@Valid @RequestBody FavoriteAddDTO favoriteAddDTO) {
        String currentUsername = getCurrentUsername();
        log.info("用户 {} 尝试收藏文物 {}", currentUsername, favoriteAddDTO.getRelicsId());
        
        try {
            InteractionResult result = userInteractionService.addFavorite(
                    currentUsername, favoriteAddDTO.getRelicsId());
            
            if (result.isSuccess()) {
                FavoriteResponseDTO responseDTO = FavoriteResponseDTO.builder()
                        .id(System.currentTimeMillis()) // 临时ID
                        .relicsId(favoriteAddDTO.getRelicsId())
                        .username(currentUsername)
                        .createTime(LocalDateTime.now())
                        .build();
                
                return Response.<FavoriteResponseDTO>builder()
                        .code(ResponseCode.SUCCESS.getCode())
                        .info("收藏成功")
                        .data(responseDTO)
                        .build();
            } else {
                return Response.<FavoriteResponseDTO>builder()
                        .code(ResponseCode.UN_ERROR.getCode())
                        .info(result.getMessage())
                        .build();
            }
            
        } catch (Exception e) {
            log.error("添加收藏失败: {} - {}", currentUsername, e.getMessage(), e);
            return Response.<FavoriteResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("收藏失败")
                    .build();
        }
    }
    
    @Operation(summary = "取消收藏", description = "用户取消收藏指定文物")
    @DeleteMapping
    public Response<String> removeFavorite(@Parameter(description = "文物ID") @RequestParam Long relicsId) {
        String currentUsername = getCurrentUsername();
        log.info("用户 {} 尝试取消收藏文物 {}", currentUsername, relicsId);
        
        try {
            InteractionResult result = userInteractionService.removeFavorite(currentUsername, relicsId);
            
            if (result.isSuccess()) {
                return Response.<String>builder()
                        .code(ResponseCode.SUCCESS.getCode())
                        .info("取消收藏成功")
                        .data("success")
                        .build();
            } else {
                return Response.<String>builder()
                        .code(ResponseCode.UN_ERROR.getCode())
                        .info(result.getMessage())
                        .data("failed")
                        .build();
            }
            
        } catch (Exception e) {
            log.error("取消收藏失败: {} - {}", currentUsername, e.getMessage(), e);
            return Response.<String>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("取消收藏失败")
                    .data("failed")
                    .build();
        }
    }
    
    @Operation(summary = "检查收藏状态", description = "检查用户是否已收藏指定文物")
    @GetMapping("/status")
    public Response<Boolean> checkFavoriteStatus(@Parameter(description = "文物ID") @RequestParam Long relicsId) {
        String currentUsername = getCurrentUsername();
        
        try {
            boolean isFavorited = userInteractionService.isFavorited(currentUsername, relicsId);
            
            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("查询成功")
                    .data(isFavorited)
                    .build();
                    
        } catch (Exception e) {
            log.error("检查收藏状态失败: {} - {}", currentUsername, e.getMessage(), e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("查询失败")
                    .data(false)
                    .build();
        }
    }
    
    @Operation(summary = "获取用户收藏列表", description = "分页获取用户的收藏文物列表")
    @GetMapping
    public Response<java.util.Map<String, Object>> getUserFavorites(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        
        String currentUsername = getCurrentUsername();
        
        try {
            IUserInteractionService.FavoriteListResult result = 
                    userInteractionService.getUserFavorites(currentUsername, page, size);
            
            // 转换为旧格式的响应
            java.util.List<FavoriteResponseDTO> favorites = result.favorites().stream()
                    .map(favorite -> FavoriteResponseDTO.builder()
                            .id(System.currentTimeMillis()) // 临时ID
                            .relicsId(favorite.getRelicsId())
                            .username(currentUsername)
                            .createTime(favorite.getCreateTime())
                            .build())
                    .collect(java.util.stream.Collectors.toList());
            
            java.util.Map<String, Object> responseData = java.util.Map.of(
                    "list", favorites,
                    "total", result.total(),
                    "page", result.page(),
                    "size", result.size(),
                    "hasNext", result.hasNext()
            );
            
            return Response.<java.util.Map<String, Object>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("查询成功")
                    .data(responseData)
                    .build();
                    
        } catch (Exception e) {
            log.error("获取用户收藏列表失败: {} - {}", currentUsername, e.getMessage(), e);
            return Response.<java.util.Map<String, Object>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("查询失败")
                    .build();
        }
    }
    
    /**
     * 获取当前登录用户名
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "anonymous";
    }
}
