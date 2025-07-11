package com.ling.trigger.http;

import com.ling.api.dto.request.FavoriteRequestDTO;
import com.ling.api.dto.request.NewCommentRequestDTO;
import com.ling.api.dto.response.*;
import com.ling.domain.interaction.model.valobj.*;
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
import java.util.List;
import java.util.Map;

/**
 * 用户交互控制器
 * @Author: LingRJ
 * @Description: 处理用户与文物的交互操作（收藏、评论等）
 * @DateTime: 2025/7/11
 */
@RestController
@RequestMapping("/api/v1/interactions")
@Tag(name = "用户交互", description = "用户与文物的交互操作API")
@Slf4j
public class InteractionController {
    
    @Autowired
    private IUserInteractionService userInteractionService;
    
    // ==================== 收藏相关 ====================
    
    @Operation(summary = "收藏/取消收藏文物", description = "用户收藏或取消收藏指定文物")
    @PostMapping("/favorites")
    public Response<InteractionResponseDTO> toggleFavorite(
            @Valid @RequestBody FavoriteRequestDTO request) {
        
        String currentUsername = getCurrentUsername();
        log.info("用户 {} 尝试{}文物 {}", currentUsername, 
                request.getFavorite() ? "收藏" : "取消收藏", request.getRelicsId());
        
        try {
            InteractionResult result;
            if (request.getFavorite()) {
                result = userInteractionService.addFavorite(currentUsername, request.getRelicsId());
            } else {
                result = userInteractionService.removeFavorite(currentUsername, request.getRelicsId());
            }
            
            if (result.isSuccess()) {
                InteractionResponseDTO responseDTO = InteractionResponseDTO.success(
                        result.getMessage(), request.getRelicsId(), currentUsername, result.getData());
                
                return Response.<InteractionResponseDTO>builder()
                        .code(ResponseCode.SUCCESS.getCode())
                        .info("操作成功")
                        .data(responseDTO)
                        .build();
            } else {
                InteractionResponseDTO responseDTO = InteractionResponseDTO.failure(
                        result.getMessage(), request.getRelicsId(), currentUsername);
                
                return Response.<InteractionResponseDTO>builder()
                        .code(ResponseCode.UN_ERROR.getCode())
                        .info(result.getMessage())
                        .data(responseDTO)
                        .build();
            }
            
        } catch (Exception e) {
            log.error("收藏操作失败: {} - {}", currentUsername, e.getMessage(), e);
            InteractionResponseDTO responseDTO = InteractionResponseDTO.failure(
                    "操作失败", request.getRelicsId(), currentUsername);
            
            return Response.<InteractionResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("操作失败")
                    .data(responseDTO)
                    .build();
        }
    }
    
    @Operation(summary = "检查收藏状态", description = "检查用户是否已收藏指定文物")
    @GetMapping("/favorites/status")
    public Response<Boolean> checkFavoriteStatus(
            @Parameter(description = "文物ID") @RequestParam Long relicsId) {
        
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
    
    @Operation(summary = "批量检查收藏状态", description = "批量检查用户对多个文物的收藏状态")
    @PostMapping("/favorites/batch-status")
    public Response<Map<Long, Boolean>> batchCheckFavoriteStatus(
            @RequestBody List<Long> relicsIds) {
        
        String currentUsername = getCurrentUsername();
        
        try {
            Map<Long, Boolean> statusMap = userInteractionService.batchCheckFavoriteStatus(
                    currentUsername, relicsIds);
            
            return Response.<Map<Long, Boolean>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("查询成功")
                    .data(statusMap)
                    .build();
                    
        } catch (Exception e) {
            log.error("批量检查收藏状态失败: {} - {}", currentUsername, e.getMessage(), e);
            return Response.<Map<Long, Boolean>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("查询失败")
                    .build();
        }
    }
    
    @Operation(summary = "获取用户收藏列表", description = "分页获取用户的收藏文物列表")
    @GetMapping("/favorites")
    public Response<Map<String, Object>> getUserFavorites(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        
        String currentUsername = getCurrentUsername();
        
        try {
            IUserInteractionService.FavoriteListResult result = 
                    userInteractionService.getUserFavorites(currentUsername, page, size);
            
            Map<String, Object> responseData = Map.of(
                    "favorites", result.favorites(),
                    "total", result.total(),
                    "page", result.page(),
                    "size", result.size(),
                    "hasNext", result.hasNext()
            );
            
            return Response.<Map<String, Object>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("查询成功")
                    .data(responseData)
                    .build();
                    
        } catch (Exception e) {
            log.error("获取用户收藏列表失败: {} - {}", currentUsername, e.getMessage(), e);
            return Response.<Map<String, Object>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("查询失败")
                    .build();
        }
    }
    
    // ==================== 评论相关 ====================
    
    @Operation(summary = "添加评论", description = "用户对指定文物添加评论")
    @PostMapping("/comments")
    public Response<NewCommentResponseDTO> addComment(
            @Valid @RequestBody NewCommentRequestDTO request) {
        
        String currentUsername = getCurrentUsername();
        log.info("用户 {} 尝试评论文物 {}: {}", currentUsername, request.getRelicsId(), 
                request.getContent().length() > 50 ? request.getContent().substring(0, 50) + "..." : request.getContent());
        
        try {
            InteractionResult result = userInteractionService.addComment(
                    currentUsername, request.getRelicsId(), request.getContent());
            
            if (result.isSuccess()) {
                Long commentId = result.getData(Long.class);
                
                NewCommentResponseDTO responseDTO = NewCommentResponseDTO.builder()
                        .commentId(commentId)
                        .relicsId(request.getRelicsId())
                        .username(currentUsername)
                        .content(request.getContent())
                        .status("PENDING_REVIEW")
                        .statusDescription("待审核")
                        .createTime(java.time.LocalDateTime.now())
                        .needsReview(true)
                        .isOwner(true)
                        .build();
                
                return Response.<NewCommentResponseDTO>builder()
                        .code(ResponseCode.SUCCESS.getCode())
                        .info("评论成功")
                        .data(responseDTO)
                        .build();
            } else {
                return Response.<NewCommentResponseDTO>builder()
                        .code(ResponseCode.UN_ERROR.getCode())
                        .info(result.getMessage())
                        .build();
            }
            
        } catch (Exception e) {
            log.error("添加评论失败: {} - {}", currentUsername, e.getMessage(), e);
            return Response.<NewCommentResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("评论失败")
                    .build();
        }
    }
    
    @Operation(summary = "删除评论", description = "用户删除自己的评论")
    @DeleteMapping("/comments/{commentId}")
    public Response<InteractionResponseDTO> deleteComment(
            @Parameter(description = "评论ID") @PathVariable Long commentId) {
        
        String currentUsername = getCurrentUsername();
        log.info("用户 {} 尝试删除评论 {}", currentUsername, commentId);
        
        try {
            InteractionResult result = userInteractionService.deleteComment(currentUsername, commentId);
            
            if (result.isSuccess()) {
                InteractionResponseDTO responseDTO = InteractionResponseDTO.success(
                        result.getMessage(), null, currentUsername);
                
                return Response.<InteractionResponseDTO>builder()
                        .code(ResponseCode.SUCCESS.getCode())
                        .info("删除成功")
                        .data(responseDTO)
                        .build();
            } else {
                InteractionResponseDTO responseDTO = InteractionResponseDTO.failure(
                        result.getMessage(), null, currentUsername);
                
                return Response.<InteractionResponseDTO>builder()
                        .code(ResponseCode.UN_ERROR.getCode())
                        .info(result.getMessage())
                        .data(responseDTO)
                        .build();
            }
            
        } catch (Exception e) {
            log.error("删除评论失败: {} - {}", currentUsername, e.getMessage(), e);
            InteractionResponseDTO responseDTO = InteractionResponseDTO.failure(
                    "删除失败", null, currentUsername);
            
            return Response.<InteractionResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("删除失败")
                    .data(responseDTO)
                    .build();
        }
    }
    
    @Operation(summary = "获取用户评论列表", description = "分页获取用户的评论列表")
    @GetMapping("/comments")
    public Response<Map<String, Object>> getUserComments(
            @Parameter(description = "文物ID（可选）") @RequestParam(required = false) Long relicsId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        
        String currentUsername = getCurrentUsername();
        
        try {
            IUserInteractionService.CommentListResult result = 
                    userInteractionService.getUserComments(currentUsername, relicsId, page, size);
            
            List<CommentResponseDTO> commentDTOs = result.comments().stream()
                    .map(comment -> convertToCommentResponseDTO(comment, currentUsername))
                    .toList();
            
            Map<String, Object> responseData = Map.of(
                    "comments", commentDTOs,
                    "total", result.total(),
                    "page", result.page(),
                    "size", result.size(),
                    "hasNext", result.hasNext()
            );
            
            return Response.<Map<String, Object>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("查询成功")
                    .data(responseData)
                    .build();
                    
        } catch (Exception e) {
            log.error("获取用户评论列表失败: {} - {}", currentUsername, e.getMessage(), e);
            return Response.<Map<String, Object>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("查询失败")
                    .build();
        }
    }
    
    // ==================== 统计相关 ====================
    
    @Operation(summary = "获取用户交互统计", description = "获取当前用户的交互统计信息")
    @GetMapping("/statistics")
    public Response<InteractionStatisticsResponseDTO> getUserStatistics() {
        String currentUsername = getCurrentUsername();
        
        try {
            InteractionStatistics statistics = userInteractionService.getUserStatistics(currentUsername);
            
            InteractionStatisticsResponseDTO responseDTO = InteractionStatisticsResponseDTO.builder()
                    .username(statistics.getUsername())
                    .favoriteCount(statistics.getFavoriteCount())
                    .commentCount(statistics.getCommentCount())
                    .totalInteractions(statistics.getTotalInteractions())
                    .lastActiveTime(statistics.getLastActiveTime())
                    .firstInteractionTime(statistics.getFirstInteractionTime())
                    .activityLevel(statistics.getActivityLevel().name())
                    .activityLevelDescription(statistics.getActivityLevel().getDescription())
                    .isActiveUser(statistics.isActiveUser())
                    .isNewUser(statistics.isNewUser())
                    .favoriteCommentRatio(statistics.getFavoriteCommentRatio())
                    .build();
            
            return Response.<InteractionStatisticsResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("查询成功")
                    .data(responseDTO)
                    .build();
                    
        } catch (Exception e) {
            log.error("获取用户统计失败: {} - {}", currentUsername, e.getMessage(), e);
            return Response.<InteractionStatisticsResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("查询失败")
                    .build();
        }
    }
    
    // ==================== 私有辅助方法 ====================
    
    /**
     * 获取当前登录用户名
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "anonymous";
    }
    
    /**
     * 转换为评论响应DTO
     */
    private CommentResponseDTO convertToCommentResponseDTO(CommentAction comment, String currentUsername) {
        return CommentResponseDTO.builder()
                .id(comment.getId())
                .relicsId(comment.getRelicsId())
                .username(currentUsername)
                .content(comment.getFullContent())
                .createTime(comment.getCreateTime())
                .isOwner(true)
                .build();
    }
}
