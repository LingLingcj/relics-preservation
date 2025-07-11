package com.ling.trigger.http.adapter;

import com.ling.api.dto.request.CommentAddDTO;
import com.ling.api.dto.response.CommentResponseDTO;
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
 * 评论控制器（向后兼容）
 * @Author: LingRJ
 * @Description: 保持与原有评论API的兼容性，内部委托给新的交互服务
 * @DateTime: 2025/7/11
 */
@RestController
@RequestMapping("/api/v1/comments")
@Tag(name = "评论管理（兼容）", description = "文物评论功能API（向后兼容版本）")
@Slf4j
public class LegacyCommentController {
    
    @Autowired
    private IUserInteractionService userInteractionService;
    
    @Operation(summary = "添加评论", description = "用户对指定文物添加评论")
    @PostMapping
    public Response<CommentResponseDTO> addComment(@Valid @RequestBody CommentAddDTO commentAddDTO) {
        String currentUsername = getCurrentUsername();
        log.info("用户 {} 尝试评论文物 {}: {}", currentUsername, commentAddDTO.getRelicsId(), 
                commentAddDTO.getContent().length() > 50 ? 
                        commentAddDTO.getContent().substring(0, 50) + "..." : commentAddDTO.getContent());
        
        try {
            InteractionResult result = userInteractionService.addComment(
                    currentUsername, commentAddDTO.getRelicsId(), commentAddDTO.getContent());
            
            if (result.isSuccess()) {
                Long commentId = result.getData(Long.class);
                
                CommentResponseDTO responseDTO = CommentResponseDTO.builder()
                        .id(commentId)
                        .relicsId(commentAddDTO.getRelicsId())
                        .username(currentUsername)
                        .content(commentAddDTO.getContent())
                        .createTime(LocalDateTime.now())
                        .isOwner(true)
                        .build();
                
                return Response.<CommentResponseDTO>builder()
                        .code(ResponseCode.SUCCESS.getCode())
                        .info("评论成功")
                        .data(responseDTO)
                        .build();
            } else {
                return Response.<CommentResponseDTO>builder()
                        .code(ResponseCode.UN_ERROR.getCode())
                        .info(result.getMessage())
                        .build();
            }
            
        } catch (Exception e) {
            log.error("添加评论失败: {} - {}", currentUsername, e.getMessage(), e);
            return Response.<CommentResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("评论失败")
                    .build();
        }
    }
    
    @Operation(summary = "删除评论", description = "用户删除自己的评论")
    @DeleteMapping("/{commentId}")
    public Response<String> deleteComment(@Parameter(description = "评论ID") @PathVariable Long commentId) {
        String currentUsername = getCurrentUsername();
        log.info("用户 {} 尝试删除评论 {}", currentUsername, commentId);
        
        try {
            InteractionResult result = userInteractionService.deleteComment(currentUsername, commentId);
            
            if (result.isSuccess()) {
                return Response.<String>builder()
                        .code(ResponseCode.SUCCESS.getCode())
                        .info("删除成功")
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
            log.error("删除评论失败: {} - {}", currentUsername, e.getMessage(), e);
            return Response.<String>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("删除失败")
                    .data("failed")
                    .build();
        }
    }
    
    @Operation(summary = "查询评论", description = "分页查询文物评论")
    @GetMapping
    public Response<java.util.Map<String, Object>> getComments(@RequestParam Long relicsId) {
        String currentUsername = getCurrentUsername();
        
        try {
            // 这里应该调用文物交互服务来获取文物的所有评论
            // 暂时使用用户交互服务的方法
            IUserInteractionService.CommentListResult result = 
                    userInteractionService.getUserComments(currentUsername, relicsId, 1, 100);
            
            java.util.List<CommentResponseDTO> commentDTOs = result.comments().stream()
                    .map(comment -> CommentResponseDTO.builder()
                            .id(comment.getId())
                            .relicsId(comment.getRelicsId())
                            .username(currentUsername)
                            .content(comment.getFullContent())
                            .createTime(comment.getCreateTime())
                            .isOwner(true)
                            .build())
                    .collect(java.util.stream.Collectors.toList());
            
            java.util.Map<String, Object> responseData = java.util.Map.of(
                    "list", commentDTOs,
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
            log.error("获取评论列表失败: {} - {}", currentUsername, e.getMessage(), e);
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
