package com.ling.trigger.http;

import com.ling.domain.interaction.model.valobj.*;
import com.ling.domain.interaction.security.CommentReviewPermission;
import com.ling.domain.interaction.service.ICommentReviewService;
import com.ling.types.common.Response;
import com.ling.types.common.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 评论审核控制器
 * @Author: LingRJ
 * @Description: 提供专家和管理员的评论审核功能
 * @DateTime: 2025/7/11
 */
@RestController
@RequestMapping("/api/v1/admin/comments")
@Tag(name = "评论审核管理", description = "专家和管理员评论审核功能API")
@SecurityRequirement(name = "JWT")
@Slf4j
@PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
@CommentReviewPermission("评论审核管理")
public class CommentReviewController {
    
    @Autowired
    private ICommentReviewService commentReviewService;
    
    // ==================== 单个审核 ====================
    
    @Operation(summary = "审核单个评论", description = "专家或管理员审核指定评论")
    @PostMapping("/{commentId}/review")
    public Response<CommentReviewResponseDTO> reviewComment(
            @Parameter(description = "评论ID") @PathVariable Long commentId,
            @Valid @RequestBody CommentReviewRequestDTO request) {
        
        String currentUsername = getCurrentUsername();
        log.info("用户 {} 尝试审核评论 {}: {}", currentUsername, commentId, request.getAction());
        
        try {
            // 权限检查
            if (!commentReviewService.canReviewComment(currentUsername, commentId)) {
                return Response.<CommentReviewResponseDTO>builder()
                        .code(ResponseCode.FORBIDDEN.getCode())
                        .info("没有权限审核此评论")
                        .build();
            }
            
            // 执行审核
            ReviewAction action = ReviewAction.fromName(request.getAction());
            CommentReviewResult result = commentReviewService.reviewComment(
                    commentId, action, currentUsername, request.getReason());
            
            if (result.isSuccess()) {
                CommentReviewResponseDTO responseDTO = CommentReviewResponseDTO.builder()
                        .commentId(result.getCommentId())
                        .action(result.getAction().getName())
                        .reviewer(result.getReviewer())
                        .reason(result.getReason())
                        .reviewTime(result.getReviewTime())
                        .beforeStatus(result.getBeforeStatus().getName())
                        .afterStatus(result.getAfterStatus().getName())
                        .success(true)
                        .message(result.getResultDescription())
                        .build();
                
                return Response.<CommentReviewResponseDTO>builder()
                        .code(ResponseCode.SUCCESS.getCode())
                        .info("审核成功")
                        .data(responseDTO)
                        .build();
            } else {
                CommentReviewResponseDTO responseDTO = CommentReviewResponseDTO.builder()
                        .commentId(commentId)
                        .action(request.getAction())
                        .reviewer(currentUsername)
                        .success(false)
                        .message(result.getErrorMessage())
                        .build();
                
                return Response.<CommentReviewResponseDTO>builder()
                        .code(ResponseCode.UN_ERROR.getCode())
                        .info(result.getErrorMessage())
                        .data(responseDTO)
                        .build();
            }
            
        } catch (Exception e) {
            log.error("审核评论失败: commentId={}, user={} - {}", commentId, currentUsername, e.getMessage(), e);
            return Response.<CommentReviewResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("审核失败")
                    .build();
        }
    }
    
    @Operation(summary = "通过评论", description = "快速通过指定评论")
    @PostMapping("/{commentId}/approve")
    public Response<CommentReviewResponseDTO> approveComment(
            @Parameter(description = "评论ID") @PathVariable Long commentId,
            @RequestBody(required = false) Map<String, String> body) {
        
        String reason = body != null ? body.get("reason") : "符合社区规范";
        CommentReviewRequestDTO request = new CommentReviewRequestDTO("通过", reason);
        return reviewComment(commentId, request);
    }
    
    @Operation(summary = "拒绝评论", description = "拒绝指定评论")
    @PostMapping("/{commentId}/reject")
    public Response<CommentReviewResponseDTO> rejectComment(
            @Parameter(description = "评论ID") @PathVariable Long commentId,
            @Valid @RequestBody RejectCommentRequestDTO request) {
        
        CommentReviewRequestDTO reviewRequest = new CommentReviewRequestDTO("拒绝", request.getReason());
        return reviewComment(commentId, reviewRequest);
    }
    
    // ==================== 批量审核 ====================
    
    @Operation(summary = "批量审核评论", description = "批量审核多个评论")
    @PostMapping("/batch-review")
    public Response<BatchReviewResponseDTO> batchReviewComments(
            @Valid @RequestBody BatchCommentReviewRequestDTO request) {
        
        String currentUsername = getCurrentUsername();
        log.info("用户 {} 尝试批量审核评论: count={}, action={}", 
                currentUsername, request.getCommentIds().size(), request.getAction());
        
        try {
            ReviewAction action = ReviewAction.fromName(request.getAction());
            ICommentReviewService.BatchReviewResult result = commentReviewService.batchReviewComments(
                    request.getCommentIds(), action, currentUsername, request.getReason());
            
            BatchReviewResponseDTO responseDTO = BatchReviewResponseDTO.builder()
                    .totalCount(result.totalCount())
                    .successCount(result.successCount())
                    .failureCount(result.failureCount())
                    .successRate(result.getSuccessRate())
                    .errors(result.errors())
                    .allSuccess(result.isAllSuccess())
                    .build();
            
            return Response.<BatchReviewResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(String.format("批量审核完成: 成功 %d/%d", result.successCount(), result.totalCount()))
                    .data(responseDTO)
                    .build();
                    
        } catch (Exception e) {
            log.error("批量审核评论失败: user={} - {}", currentUsername, e.getMessage(), e);
            return Response.<BatchReviewResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("批量审核失败")
                    .build();
        }
    }
    
    // ==================== 查询功能 ====================
    
    @Operation(summary = "获取待审核评论列表", description = "分页获取待审核的评论列表")
    @GetMapping("/pending")
    public Response<Map<String, Object>> getPendingComments(
            @Parameter(description = "文物ID（可选）") @RequestParam(required = false) Long relicsId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        
        String currentUsername = getCurrentUsername();
        
        try {
            ICommentReviewService.PendingCommentListResult result = 
                    commentReviewService.getPendingComments(relicsId, page, size);
            
            List<PendingCommentDTO> commentDTOs = result.comments().stream()
                    .map(this::convertToPendingCommentDTO)
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
            log.error("获取待审核评论失败: user={} - {}", currentUsername, e.getMessage(), e);
            return Response.<Map<String, Object>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("查询失败")
                    .build();
        }
    }
    
    @Operation(summary = "获取审核统计", description = "获取审核人员的统计信息")
    @GetMapping("/statistics")
    public Response<ReviewStatisticsDTO> getReviewStatistics(
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime) {
        
        String currentUsername = getCurrentUsername();
        
        try {
            LocalDateTime start = startTime != null ? LocalDateTime.parse(startTime) : null;
            LocalDateTime end = endTime != null ? LocalDateTime.parse(endTime) : null;
            
            ICommentReviewService.ReviewStatistics stats = 
                    commentReviewService.getReviewStatistics(currentUsername, start, end);
            
            ReviewStatisticsDTO responseDTO = ReviewStatisticsDTO.builder()
                    .reviewer(stats.reviewer())
                    .startTime(stats.startTime())
                    .endTime(stats.endTime())
                    .totalReviewed(stats.totalReviewed())
                    .approvedCount(stats.approvedCount())
                    .rejectedCount(stats.rejectedCount())
                    .approvalRate(stats.approvalRate())
                    .rejectionRate(stats.rejectionRate())
                    .dailyStats(stats.dailyStats())
                    .build();
            
            return Response.<ReviewStatisticsDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("查询成功")
                    .data(responseDTO)
                    .build();
                    
        } catch (Exception e) {
            log.error("获取审核统计失败: user={} - {}", currentUsername, e.getMessage(), e);
            return Response.<ReviewStatisticsDTO>builder()
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
     * 转换为待审核评论DTO
     */
    private PendingCommentDTO convertToPendingCommentDTO(CommentWithUser comment) {
        return PendingCommentDTO.builder()
                .id(comment.getCommentId())
                .relicsId(comment.getRelicsId())
                .username(comment.getUsername())
                .content(comment.getContent())
                .status(comment.getStatus().getName())
                .createTime(comment.getCreateTime())
                .waitingDays(comment.getWaitingDays())
                .urgent(comment.isUrgent())
                .build();
    }
    
    // ==================== 内部DTO类 ====================
    
    public static class CommentReviewRequestDTO {
        @NotBlank(message = "审核操作不能为空")
        private String action;
        
        @Size(max = 500, message = "审核理由不能超过500字符")
        private String reason;
        
        public CommentReviewRequestDTO() {}
        
        public CommentReviewRequestDTO(String action, String reason) {
            this.action = action;
            this.reason = reason;
        }
        
        // getters and setters
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
    
    public static class RejectCommentRequestDTO {
        @NotBlank(message = "拒绝理由不能为空")
        @Size(max = 500, message = "拒绝理由不能超过500字符")
        private String reason;
        
        // getters and setters
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
    
    public static class BatchCommentReviewRequestDTO {
        @NotEmpty(message = "评论ID列表不能为空")
        private List<Long> commentIds;

        @NotBlank(message = "审核操作不能为空")
        private String action;

        @Size(max = 500, message = "审核理由不能超过500字符")
        private String reason;

        // getters and setters
        public List<Long> getCommentIds() { return commentIds; }
        public void setCommentIds(List<Long> commentIds) { this.commentIds = commentIds; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    // ==================== 响应DTO类 ====================

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CommentReviewResponseDTO {
        private Long commentId;
        private String action;
        private String reviewer;
        private String reason;
        private LocalDateTime reviewTime;
        private String beforeStatus;
        private String afterStatus;
        private Boolean success;
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BatchReviewResponseDTO {
        private Integer totalCount;
        private Integer successCount;
        private Integer failureCount;
        private Double successRate;
        private Boolean allSuccess;
        private List<String> errors;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PendingCommentDTO {
        private Long id;
        private Long relicsId;
        private String relicsName;
        private String username;
        private String content;
        private String status;
        private LocalDateTime createTime;
        private Long waitingDays;
        private Boolean urgent;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReviewStatisticsDTO {
        private String reviewer;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long totalReviewed;
        private Long approvedCount;
        private Long rejectedCount;
        private Double approvalRate;
        private Double rejectionRate;
        private Map<String, Long> dailyStats;
        private Double avgReviewTime;
        private String efficiencyLevel;
    }
}
