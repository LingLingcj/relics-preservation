package com.ling.infrastructure.repository.converter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.ling.domain.interaction.model.entity.UserComments;
import com.ling.domain.interaction.model.valobj.CommentAction;
import com.ling.domain.interaction.model.valobj.CommentContent;
import com.ling.domain.interaction.model.valobj.CommentStatus;
import com.ling.domain.user.model.valobj.Username;
import com.ling.infrastructure.dao.po.UserComment;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户评论数据转换器
 * @Author: LingRJ
 * @Description: 负责UserComments聚合根与数据库记录之间的转换
 * @DateTime: 2025/7/13
 */
@Slf4j
@Component
public class UserCommentsConverter {

    /**
     * 构建用户评论聚合根
     * @param username 用户名
     * @param comments 评论记录列表
     * @return 用户评论聚合根
     */
    public UserComments buildUserComments(Username username, List<UserComment> comments) {
        try {
            log.debug("构建用户评论聚合根: {}, 评论数: {}", username.getValue(), comments.size());

            // 转换评论数据
            List<CommentAction> commentActions = convertComments(comments);

            // 计算时间信息
            LocalDateTime earliestTime = calculateEarliestTime(comments);
            LocalDateTime latestTime = calculateLatestTime(comments);

            // 使用工厂方法重建聚合根
            return UserComments.fromDatabase(username, commentActions, earliestTime, latestTime);

        } catch (Exception e) {
            log.error("构建用户评论聚合根失败: {} - {}", username.getValue(), e.getMessage(), e);
            throw new RuntimeException("构建用户评论聚合根失败", e);
        }
    }

    /**
     * 转换评论记录列表
     * @param comments 数据库评论记录
     * @return 评论行为值对象列表
     */
    public List<CommentAction> convertComments(List<UserComment> comments) {
        List<CommentAction> actions = new ArrayList<>();
        for (UserComment comment : comments) {
            try {
                CommentAction action = convertToCommentAction(comment);
                actions.add(action);
            } catch (Exception e) {
                log.warn("转换评论记录失败: {} - {}", comment.getCommentId(), e.getMessage());
            }
        }
        return actions;
    }

    /**
     * 从数据库记录转换为评论行为
     * @param comment 数据库评论记录
     * @return 评论行为值对象
     */
    public CommentAction convertToCommentAction(UserComment comment) {
        CommentContent content = CommentContent.of(comment.getContent());

        return CommentAction.fromDatabase(
                comment.getCommentId(),
                comment.getRelicsId(),
                content,
                comment.getCreateTime(),
                comment.getUpdateTime(),
                CommentStatus.fromCode(comment.getCommentStatus()),
                comment.getStatus() == 1 // status=1表示已删除
        );
    }

    /**
     * 将聚合根转换为数据库记录列表
     * @param userComments 用户评论聚合根
     * @return 数据库评论记录列表
     */
    public List<UserComment> convertToUserComments(UserComments userComments) {
        List<UserComment> comments = new ArrayList<>();
        
        for (CommentAction commentAction : userComments.getComments(null)) {
            try {
                UserComment userComment = convertToUserComment(userComments.getUsername(), commentAction);
                comments.add(userComment);
            } catch (Exception e) {
                log.warn("转换评论行为失败: {} - {}", 
                        commentAction.getId(), e.getMessage());
            }
        }
        
        return comments;
    }

    /**
     * 将评论行为转换为数据库记录
     * @param username 用户名
     * @param commentAction 评论行为
     * @return 数据库评论记录
     */
    public UserComment convertToUserComment(Username username, CommentAction commentAction) {
        LocalDateTime now = LocalDateTime.now();
        
        return UserComment.builder()
                .commentId(commentAction.getId())
                .username(username.getValue())
                .relicsId(commentAction.getRelicsId())
                .content(commentAction.getContent().getContent())
                .commentStatus(commentAction.getStatus().getCode())
                .createTime(commentAction.getCreateTime())
                .updateTime(commentAction.getUpdateTime() != null ? commentAction.getUpdateTime() : now)
                .status(commentAction.isDeleted() ? 1 : 0)
                .build();
    }

    /**
     * 计算最早时间
     * @param comments 评论记录列表
     * @return 最早时间
     */
    private LocalDateTime calculateEarliestTime(List<UserComment> comments) {
        LocalDateTime earliest = null;

        for (UserComment comment : comments) {
            if (earliest == null || comment.getCreateTime().isBefore(earliest)) {
                earliest = comment.getCreateTime();
            }
        }

        return earliest != null ? earliest : LocalDateTime.now();
    }

    /**
     * 计算最晚时间
     * @param comments 评论记录列表
     * @return 最晚时间
     */
    private LocalDateTime calculateLatestTime(List<UserComment> comments) {
        LocalDateTime latest = null;

        for (UserComment comment : comments) {
            LocalDateTime time = comment.getUpdateTime() != null ? 
                    comment.getUpdateTime() : comment.getCreateTime();
            if (latest == null || time.isAfter(latest)) {
                latest = time;
            }
        }

        return latest != null ? latest : LocalDateTime.now();
    }

    /**
     * 构建评论统计信息
     * @param username 用户名
     * @param comments 评论记录列表
     * @return 评论统计
     */
    public UserComments.CommentStatistics buildCommentStatistics(Username username, List<UserComment> comments) {
        long totalComments = comments.stream()
                .filter(c -> c.getStatus() == 0) // 只统计正常状态的评论
                .count();

        long approvedComments = comments.stream()
                .filter(c -> c.getStatus() == 0)
                .filter(c -> c.getCommentStatus() == 1) // 已通过审核
                .count();

        long pendingComments = comments.stream()
                .filter(c -> c.getStatus() == 0)
                .filter(c -> c.getCommentStatus() == 0) // 待审核
                .count();

        LocalDateTime lastCommentTime = calculateLatestTime(comments);

        return UserComments.CommentStatistics.builder()
                .username(username.getValue())
                .totalComments(totalComments)
                .approvedComments(approvedComments)
                .pendingComments(pendingComments)
                .lastCommentTime(lastCommentTime)
                .build();
    }

    /**
     * 验证评论记录的有效性
     * @param comment 评论记录
     * @return 是否有效
     */
    public boolean isValidComment(UserComment comment) {
        return comment != null 
                && comment.getCommentId() != null
                && comment.getUsername() != null 
                && !comment.getUsername().trim().isEmpty()
                && comment.getRelicsId() != null 
                && comment.getRelicsId() > 0
                && comment.getContent() != null
                && !comment.getContent().trim().isEmpty()
                && comment.getCreateTime() != null;
    }

    /**
     * 过滤有效的评论记录
     * @param comments 评论记录列表
     * @return 有效的评论记录列表
     */
    public List<UserComment> filterValidComments(List<UserComment> comments) {
        return comments.stream()
                .filter(this::isValidComment)
                .toList();
    }

    /**
     * 构建评论活动描述
     * @param comment 评论记录
     * @return 活动描述
     */
    public String buildCommentActivityDescription(UserComment comment) {
        if (comment.getStatus() == 1) {
            return "删除了评论";
        } else if (comment.getCommentStatus() == 1) {
            return "评论通过审核: " + getCommentSummary(comment.getContent());
        } else if (comment.getCommentStatus() == 2) {
            return "评论被拒绝: " + getCommentSummary(comment.getContent());
        } else {
            return "评论了文物: " + getCommentSummary(comment.getContent());
        }
    }

    /**
     * 获取评论摘要
     * @param content 评论内容
     * @return 评论摘要
     */
    private String getCommentSummary(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "[空评论]";
        }
        
        String trimmed = content.trim();
        return trimmed.length() <= 30 ? trimmed : trimmed.substring(0, 30) + "...";
    }

    /**
     * 检查评论记录是否需要更新
     * @param existing 现有记录
     * @param commentAction 评论行为
     * @return 是否需要更新
     */
    public boolean needsUpdate(UserComment existing, CommentAction commentAction) {
        if (existing == null || commentAction == null) {
            return false;
        }

        // 检查状态是否需要更新
        int expectedStatus = commentAction.isDeleted() ? 1 : 0;
        int expectedCommentStatus = commentAction.getStatus().getCode();
        
        return existing.getStatus() != expectedStatus 
                || existing.getCommentStatus() != expectedCommentStatus;
    }

    /**
     * 判断是否为新评论（24小时内）
     * @param createTime 创建时间
     * @return 是否为新评论
     */
    public boolean isNewComment(LocalDateTime createTime) {
        if (createTime == null) {
            return false;
        }
        
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        return createTime.isAfter(oneDayAgo);
    }

    /**
     * 获取评论状态描述
     * @param commentStatus 评论状态码
     * @return 状态描述
     */
    public String getCommentStatusDescription(Integer commentStatus) {
        if (commentStatus == null) {
            return "未知状态";
        }
        
        return switch (commentStatus) {
            case 0 -> "待审核";
            case 1 -> "已通过";
            case 2 -> "已拒绝";
            default -> "未知状态";
        };
    }
}
