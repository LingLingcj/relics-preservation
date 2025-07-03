package com.ling.trigger.http;

import com.ling.api.dto.request.FavoriteAddDTO;
import com.ling.api.dto.request.FavoriteQueryDTO;
import com.ling.api.dto.response.FavoriteResponseDTO;
import com.ling.domain.favorite.model.entity.FavoriteEntity;
import com.ling.domain.favorite.service.IFavoriteService;
import com.ling.domain.relics.adapter.IRelicsRepository;
import com.ling.domain.relics.model.entity.RelicsEntity;
import com.ling.types.common.Response;
import com.ling.types.common.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: LingRJ
 * 收藏控制器
 *
 */
@Tag(name = "文物收藏", description = "文物收藏相关接口")
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private IFavoriteService favoriteService;
    
    @Autowired
    private IRelicsRepository relicsRepository;

    /**
     * 添加收藏
     */
    @Operation(summary = "添加收藏", description = "添加文物到收藏夹")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Response<Void> addFavorite(
            @Parameter(description = "收藏信息", required = true)
            @Valid @RequestBody FavoriteAddDTO favoriteAddDTO) {
        String username = getCurrentUsername();
        FavoriteEntity result = favoriteService.addFavorite(favoriteAddDTO.getRelicsId(), username);
        
        if (result.isSuccess()) {
            return Response.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("收藏成功")
                    .build();
        } else {
            return Response.<Void>builder()
                    .code(getErrorCodeByMessage(result.getMessage()))
                    .info(result.getMessage())
                    .build();
        }
    }

    /**
     * 取消收藏
     */
    @Operation(summary = "取消收藏", description = "从收藏夹中移除文物")
    @DeleteMapping("/{relicsId}")
    @PreAuthorize("isAuthenticated()")
    public Response<Void> cancelFavorite(
            @Parameter(description = "文物ID", required = true)
            @PathVariable Long relicsId) {
        String username = getCurrentUsername();
        FavoriteEntity result = favoriteService.cancelFavorite(relicsId, username);
        
        if (result.isSuccess()) {
            return Response.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("取消收藏成功")
                    .build();
        } else {
            return Response.<Void>builder()
                    .code(getErrorCodeByMessage(result.getMessage()))
                    .info(result.getMessage())
                    .build();
        }
    }

    /**
     * 判断是否已收藏
     */
    @Operation(summary = "检查收藏状态", description = "检查文物是否已被当前用户收藏")
    @GetMapping("/check/{relicsId}")
    @PreAuthorize("isAuthenticated()")
    public Response<Boolean> checkFavorite(
            @Parameter(description = "文物ID", required = true)
            @PathVariable Long relicsId) {
        String username = getCurrentUsername();
        boolean isFavorite = favoriteService.isFavorite(relicsId, username);
        
        return Response.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("查询成功")
                .data(isFavorite)
                .build();
    }

    /**
     * 获取用户收藏列表
     */
    @Operation(summary = "获取收藏列表", description = "分页获取用户的收藏文物列表")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Response<Map<String, Object>> getUserFavorites() {
        String username = getCurrentUsername();
        
        // 获取收藏列表
        List<FavoriteEntity> favorites = favoriteService.getUserFavorites(
                username, 1, 100);
        
        // 获取总数
        int total = favoriteService.countUserFavorites(username);
        
        // 转换为响应DTO
        List<FavoriteResponseDTO> responseDTOs = favorites.stream()
                .map(entity -> {
                    FavoriteResponseDTO dto = new FavoriteResponseDTO();
                    BeanUtils.copyProperties(entity, dto);
                    
                    // 获取文物信息
                    RelicsEntity relicsEntity = relicsRepository.findById(entity.getRelicsId());
                    if (relicsEntity != null) {
                        dto.setRelicsName(relicsEntity.getName());
                        dto.setRelicsImageUrl(relicsEntity.getImageUrl());
                        dto.setRelicsDescription(relicsEntity.getDescription());
                    }
                    
                    return dto;
                }).collect(Collectors.toList());
        
        // 构造返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("items", responseDTOs);
        result.put("total", total);
        
        return Response.<Map<String, Object>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("查询成功")
                .data(result)
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
    
    /**
     * 根据错误信息获取对应的错误码
     * @param message 错误信息
     * @return 错误码
     */
    private String getErrorCodeByMessage(String message) {
        if ("文物不存在".equals(message)) {
            return ResponseCode.RELICS_NOT_FOUND.getCode();
        }
        for (ResponseCode code : ResponseCode.values()) {
            if (code.getInfo().equals(message)) {
                return code.getCode();
            }
        }
        return ResponseCode.SYSTEM_ERROR.getCode();
    }
} 