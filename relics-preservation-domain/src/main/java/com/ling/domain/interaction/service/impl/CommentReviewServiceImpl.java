package com.ling.domain.interaction.service.impl;

import com.ling.domain.interaction.adapter.IUserInteractionRepository;
import com.ling.domain.interaction.event.CommentReviewedEvent;
import com.ling.domain.interaction.model.valobj.*;
import com.ling.domain.interaction.service.ICommentReviewService;
import com.ling.domain.user.model.entity.User;
import com.ling.domain.user.model.valobj.UserRole;
import com.ling.domain.user.model.valobj.Username;
import com.ling.domain.user.service.IUserManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 评论审核服务实现
 * @Author: LingRJ
 * @Description: 实现评论审核相关的业务逻辑
 * @DateTime: 2025/7/11
 */
@Service
@Slf4j
public class CommentReviewServiceImpl implements ICommentReviewService {
    
    @Autowired
    private IUserInteractionRepository userInteractionRepository;
    
    @Autowired
    private IUserManagementService userManagementService;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    // ==================== 单个审核 ====================
    
    @Override
    @Transactional
    public CommentReviewResult reviewComment(Long commentId, ReviewAction action, String reviewer, String reason) {
        try {
            log.info("审核评论: commentId={}, action={}, reviewer={}", commentId, action, reviewer);
            
            // 权限检查
            if (!hasReviewPermission(reviewer)) {
                return CommentReviewResult.failure(commentId, action, reviewer, "没有审核权限");
            }
            
            // 查找评论（需要获取包含用户信息的评论）
            CommentWithUser comment = findCommentWithUserById(commentId);
            if (comment == null) {
                return CommentReviewResult.failure(commentId, action, reviewer, "评论不存在");
            }

            // 检查评论状态
            if (!comment.getStatus().canBeReviewed()) {
                return CommentReviewResult.failure(commentId, action, reviewer,
                        "评论状态不允许审核: " + comment.getStatus().getName());
            }

            // 执行审核
            CommentStatus beforeStatus = comment.getStatus();
            CommentStatus afterStatus = action.toCommentStatus();

            // 更新评论状态
            boolean updated = updateCommentStatus(commentId, afterStatus);
            if (!updated) {
                return CommentReviewResult.failure(commentId, action, reviewer, "更新评论状态失败");
            }

            // 创建审核结果
            CommentReviewResult result = CommentReviewResult.success(
                    commentId, action, reviewer, reason, beforeStatus, afterStatus);

            // 记录审核日志
            recordReviewLog(result, comment);

            // 发布领域事件
            CommentReviewedEvent event = new CommentReviewedEvent(result,
                    comment.getUsername(), comment.getRelicsId());
            eventPublisher.publishEvent(event);
            
            log.info("评论审核完成: {}", result.getResultDescription());
            return result;
            
        } catch (Exception e) {
            log.error("审核评论失败: commentId={}, action={}, reviewer={} - {}", 
                    commentId, action, reviewer, e.getMessage(), e);
            return CommentReviewResult.failure(commentId, action, reviewer, "审核失败: " + e.getMessage());
        }
    }
    
    @Override
    public CommentReviewResult approveComment(Long commentId, String reviewer, String reason) {
        return reviewComment(commentId, ReviewAction.APPROVE, reviewer, reason);
    }
    
    @Override
    public CommentReviewResult rejectComment(Long commentId, String reviewer, String reason) {
        return reviewComment(commentId, ReviewAction.REJECT, reviewer, reason);
    }
    
    // ==================== 批量审核 ====================
    
    @Override
    @Transactional
    public BatchReviewResult batchReviewComments(List<Long> commentIds, ReviewAction action, 
                                               String reviewer, String reason) {
        log.info("批量审核评论: count={}, action={}, reviewer={}", commentIds.size(), action, reviewer);
        
        List<CommentReviewResult> results = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        
        for (Long commentId : commentIds) {
            try {
                CommentReviewResult result = reviewComment(commentId, action, reviewer, reason);
                results.add(result);
                
                if (result.isSuccess()) {
                    successCount++;
                } else {
                    errors.add(String.format("评论 %d: %s", commentId, result.getErrorMessage()));
                }
                
            } catch (Exception e) {
                String error = String.format("评论 %d: %s", commentId, e.getMessage());
                errors.add(error);
                log.error("批量审核单个评论失败: {}", error, e);
            }
        }
        
        int failureCount = commentIds.size() - successCount;
        
        log.info("批量审核完成: total={}, success={}, failure={}", 
                commentIds.size(), successCount, failureCount);
        
        return new BatchReviewResult(commentIds.size(), successCount, failureCount, results, errors);
    }
    
    @Override
    public BatchReviewResult batchApproveComments(List<Long> commentIds, String reviewer, String reason) {
        return batchReviewComments(commentIds, ReviewAction.APPROVE, reviewer, reason);
    }
    
    @Override
    public BatchReviewResult batchRejectComments(List<Long> commentIds, String reviewer, String reason) {
        return batchReviewComments(commentIds, ReviewAction.REJECT, reviewer, reason);
    }
    
    // ==================== 查询功能 ====================
    
    @Override
    public PendingCommentListResult getPendingComments(Long relicsId, int page, int size) {
        try {
            List<CommentWithUser> comments = userInteractionRepository.getPendingComments(relicsId, page, size);
            long total = countPendingComments(relicsId);
            boolean hasNext = (page * size) < total;

            return new PendingCommentListResult(comments, total, page, size, hasNext);

        } catch (Exception e) {
            log.error("获取待审核评论失败: relicsId={} - {}", relicsId, e.getMessage(), e);
            return new PendingCommentListResult(List.of(), 0, page, size, false);
        }
    }
    
    @Override
    public ReviewHistoryListResult getReviewHistory(Long commentId, String reviewer, 
                                                  LocalDateTime startTime, LocalDateTime endTime, 
                                                  int page, int size) {
        try {
            // TODO: 实现审核历史查询
            // 这里需要从审核日志表中查询数据
            List<CommentReviewRecord> records = List.of();
            long total = 0;
            boolean hasNext = false;
            
            return new ReviewHistoryListResult(records, total, page, size, hasNext);
            
        } catch (Exception e) {
            log.error("获取审核历史失败: reviewer={} - {}", reviewer, e.getMessage(), e);
            return new ReviewHistoryListResult(List.of(), 0, page, size, false);
        }
    }
    
    @Override
    public ReviewStatistics getReviewStatistics(String reviewer, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            // TODO: 实现审核统计查询
            // 这里需要从审核日志表中统计数据
            return new ReviewStatistics(reviewer, startTime, endTime, 0, 0, 0, 0.0, 0.0, Map.of());
            
        } catch (Exception e) {
            log.error("获取审核统计失败: reviewer={} - {}", reviewer, e.getMessage(), e);
            return new ReviewStatistics(reviewer, startTime, endTime, 0, 0, 0, 0.0, 0.0, Map.of());
        }
    }
    
    // ==================== 权限检查 ====================
    
    @Override
    public boolean hasReviewPermission(String username) {
        try {
            // 通过用户管理服务获取用户信息
            Optional<User> userOpt = userManagementService.getUserInfo(username);
            if (userOpt.isEmpty()) {
                log.warn("用户不存在: {}", username);
                return false;
            }

            User user = userOpt.get();
            UserRole role = user.getRole();

            // 检查是否为专家或管理员角色
            boolean hasPermission = role.isExpert() || role.isAdmin();
            log.debug("用户 {} 的角色为 {}，审核权限: {}", username, role.getCode(), hasPermission);

            return hasPermission;

        } catch (Exception e) {
            log.error("检查审核权限失败: username={} - {}", username, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean canReviewComment(String username, Long commentId) {
        // 基本权限检查
        if (!hasReviewPermission(username)) {
            return false;
        }
        
        try {
            // 检查评论是否存在且可以审核
            Optional<CommentAction> commentOpt = userInteractionRepository.findCommentById(commentId);
            if (commentOpt.isEmpty()) {
                return false;
            }
            
            CommentAction comment = commentOpt.get();
            return comment.needsReview(); // 使用 CommentAction 的业务方法
            
        } catch (Exception e) {
            log.error("检查评论审核权限失败: username={}, commentId={} - {}", 
                    username, commentId, e.getMessage(), e);
            return false;
        }
    }
    
    // ==================== 私有辅助方法 ====================
    
    /**
     * 更新评论状态
     */
    private boolean updateCommentStatus(Long commentId, CommentStatus status) {
        try {
            // TODO: 在仓储层实现评论状态更新方法
            // userInteractionRepository.updateCommentStatus(commentId, status);
            return true;
        } catch (Exception e) {
            log.error("更新评论状态失败: commentId={}, status={} - {}", 
                    commentId, status, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 查找包含用户信息的评论
     */
    private CommentWithUser findCommentWithUserById(Long commentId) {
        try {
            // 从待审核评论中查找（这是一个简化实现）
            List<CommentWithUser> pendingComments = userInteractionRepository.getPendingComments(null, 1, 1000);
            return pendingComments.stream()
                    .filter(comment -> comment.getCommentId().equals(commentId))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            log.error("查找评论失败: commentId={} - {}", commentId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 记录审核日志
     */
    private void recordReviewLog(CommentReviewResult result, CommentWithUser comment) {
        try {
            // TODO: 实现审核日志记录
            log.info("记录审核日志: {}", result.getResultDescription());
        } catch (Exception e) {
            log.error("记录审核日志失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 统计待审核评论数量
     */
    private long countPendingComments(Long relicsId) {
        try {
            // TODO: 实现待审核评论计数
            return 0;
        } catch (Exception e) {
            log.error("统计待审核评论失败: relicsId={} - {}", relicsId, e.getMessage(), e);
            return 0;
        }
    }
}
