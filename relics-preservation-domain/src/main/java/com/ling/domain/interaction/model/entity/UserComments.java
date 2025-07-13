package com.ling.domain.interaction.model.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.ling.domain.interaction.event.CommentDeletedEvent;
import com.ling.domain.interaction.event.UserCommentedOnRelicsEvent;
import com.ling.domain.interaction.model.valobj.ChangeTracker;
import com.ling.domain.interaction.model.valobj.CommentAction;
import com.ling.domain.interaction.model.valobj.CommentContent;
import com.ling.domain.interaction.model.valobj.InteractionResult;
import com.ling.domain.user.event.DomainEventPublisher;
import com.ling.domain.user.model.valobj.Username;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户评论聚合根
 * @Author: LingRJ
 * @Description: 专门管理用户评论功能的聚合根，负责评论创建/删除、评论状态管理等
 * @DateTime: 2025/7/13
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Slf4j
public class UserComments {

    private final Username username;
    private final List<CommentAction> comments;
    private final LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 变更跟踪器，用于增量保存
    private final ChangeTracker changeTracker;

    /**
     * 创建用户评论聚合根
     */
    public static UserComments create(Username username) {
        LocalDateTime now = LocalDateTime.now();
        return UserComments.builder()
                .username(username)
                .comments(new ArrayList<>())
                .createTime(now)
                .updateTime(now)
                .changeTracker(new ChangeTracker())
                .build();
    }

    /**
     * 从数据库记录重建用户评论聚合根
     * @param username 用户名
     * @param comments 评论行为列表
     * @param createTime 创建时间
     * @param updateTime 更新时间
     * @return 用户评论聚合根
     */
    public static UserComments fromDatabase(Username username,
                                          List<CommentAction> comments,
                                          LocalDateTime createTime,
                                          LocalDateTime updateTime) {
        return UserComments.builder()
                .username(username)
                .comments(comments != null ? comments : new ArrayList<>())
                .createTime(createTime != null ? createTime : LocalDateTime.now())
                .updateTime(updateTime != null ? updateTime : LocalDateTime.now())
                .changeTracker(new ChangeTracker()) // 从数据库重建时创建新的变更跟踪器
                .build();
    }

    /**
     * 添加评论
     * @param relicsId 文物ID
     * @param content 评论内容
     * @return 操作结果
     */
    public InteractionResult addComment(Long relicsId, String content) {
        try {
            CommentContent commentContent = CommentContent.of(content);
            CommentAction comment = CommentAction.create(relicsId, commentContent);

            comments.add(comment);
            updateTime = LocalDateTime.now();

            // 记录变更
            changeTracker.recordAdd("COMMENT", comment.getId(), comment);

            // 发布评论事件
            DomainEventPublisher.publish(new UserCommentedOnRelicsEvent(
                    username.getValue(), relicsId, content, comment.getId()));

            log.info("用户 {} 评论文物 {}: {}", username.getValue(), relicsId,
                    content.length() > 50 ? content.substring(0, 50) + "..." : content);
            return InteractionResult.success("评论成功", comment.getId());

        } catch (Exception e) {
            log.error("添加评论失败: {} - {}", username.getValue(), e.getMessage(), e);
            return InteractionResult.failure("评论失败: " + e.getMessage());
        }
    }

    /**
     * 删除评论
     * @param commentId 评论ID
     * @return 操作结果
     */
    public InteractionResult deleteComment(Long commentId) {
        try {
            Optional<CommentAction> commentOpt = comments.stream()
                    .filter(c -> c.getId().equals(commentId))
                    .findFirst();

            if (commentOpt.isEmpty()) {
                return InteractionResult.failure("评论不存在");
            }

            CommentAction comment = commentOpt.get();
            comment.delete();
            updateTime = LocalDateTime.now();

            // 记录变更
            changeTracker.recordDelete("COMMENT", commentId, comment);

            // 发布评论删除事件
            DomainEventPublisher.publish(new CommentDeletedEvent(
                    username.getValue(), comment.getRelicsId(), commentId));

            log.info("用户 {} 删除评论 {}", username.getValue(), commentId);
            return InteractionResult.success("删除评论成功");

        } catch (Exception e) {
            log.error("删除评论失败: {} - {}", username.getValue(), e.getMessage(), e);
            return InteractionResult.failure("删除评论失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的评论列表
     * @param relicsId 文物ID（可选，为null时返回所有评论）
     * @return 评论列表
     */
    public List<CommentAction> getComments(Long relicsId) {
        return comments.stream()
                .filter(c -> !c.isDeleted())
                .filter(c -> relicsId == null || c.getRelicsId().equals(relicsId))
                .sorted((c1, c2) -> c2.getCreateTime().compareTo(c1.getCreateTime()))
                .toList();
    }

    /**
     * 获取评论统计信息
     * @return 评论统计
     */
    public CommentStatistics getStatistics() {
        long commentCount = comments.stream()
                .filter(c -> !c.isDeleted())
                .count();

        long approvedCount = comments.stream()
                .filter(c -> !c.isDeleted() && c.isVisible())
                .count();

        long pendingCount = comments.stream()
                .filter(c -> !c.isDeleted() && !c.isVisible())
                .count();

        return CommentStatistics.builder()
                .username(username.getValue())
                .totalComments(commentCount)
                .approvedComments(approvedCount)
                .pendingComments(pendingCount)
                .lastCommentTime(updateTime)
                .build();
    }

    /**
     * 获取显示名称
     * @return 显示名称
     */
    public String getDisplayName() {
        return username.getValue();
    }

    // ==================== 增量保存相关方法 ====================

    /**
     * 检查是否有变更
     * @return 是否有变更
     */
    public boolean hasChanges() {
        return changeTracker.isHasChanges();
    }

    /**
     * 获取评论变更记录
     * @return 评论变更记录集合
     */
    public Set<ChangeTracker.ChangeRecord> getCommentChanges() {
        return changeTracker.getChangesByType("COMMENT");
    }

    /**
     * 清空变更记录（保存成功后调用）
     */
    public void clearChanges() {
        changeTracker.clearChanges();
    }

    /**
     * 获取变更统计信息
     * @return 变更统计信息
     */
    public String getChangesSummary() {
        if (!hasChanges()) {
            return "无变更";
        }

        int commentChanges = changeTracker.getChangeCount("COMMENT");
        return String.format("评论变更: %d", commentChanges);
    }

    /**
     * 评论统计值对象
     */
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(force = true)
    public static class CommentStatistics {
        private final String username;
        private final long totalComments;
        private final long approvedComments;
        private final long pendingComments;
        private final LocalDateTime lastCommentTime;
    }
}
