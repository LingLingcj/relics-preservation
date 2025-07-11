package com.ling.infrastructure.repository.converter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.ling.domain.interaction.model.entity.UserInteraction;
import com.ling.domain.interaction.model.valobj.CommentAction;
import com.ling.domain.interaction.model.valobj.CommentContent;
import com.ling.domain.interaction.model.valobj.CommentStatus;
import com.ling.domain.interaction.model.valobj.FavoriteAction;
import com.ling.domain.interaction.model.valobj.InteractionActivity;
import com.ling.domain.user.model.valobj.Username;
import com.ling.infrastructure.dao.po.UserComment;
import com.ling.infrastructure.dao.po.UserFavorite;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户交互数据转换器
 * @Author: LingRJ
 * @Description: 负责数据库记录与领域对象之间的转换
 * @DateTime: 2025/7/11
 */
@Slf4j
@Component
public class UserInteractionConverter {

    /**
     * 构建用户交互聚合根
     */
    public UserInteraction buildUserInteraction(Username username,
                                               List<UserFavorite> favorites,
                                               List<UserComment> comments) {
        try {
            log.debug("构建用户交互聚合根: {}, 收藏数: {}, 评论数: {}",
                    username.getValue(), favorites.size(), comments.size());

            // 转换数据
            Set<FavoriteAction> favoriteActions = convertFavorites(favorites);
            List<CommentAction> commentActions = convertComments(comments);

            // 计算时间信息
            LocalDateTime earliestTime = calculateEarliestTime(favorites, comments);
            LocalDateTime latestTime = calculateLatestTime(favorites, comments);

            // 使用工厂方法重建聚合根
            return UserInteraction.fromDatabase(username, favoriteActions, commentActions, earliestTime, latestTime);

        } catch (Exception e) {
            log.error("构建用户交互聚合根失败: {} - {}", username.getValue(), e.getMessage(), e);
            return UserInteraction.create(username);
        }
    }

    /**
     * 转换收藏记录
     */
    public Set<FavoriteAction> convertFavorites(List<UserFavorite> favorites) {
        Set<FavoriteAction> actions = new HashSet<>();
        for (UserFavorite favorite : favorites) {
            try {
                FavoriteAction action = convertToFavoriteAction(favorite);
                actions.add(action);
            } catch (Exception e) {
                log.warn("转换收藏记录失败: {} - {}", favorite.getId(), e.getMessage());
            }
        }
        return actions;
    }

    /**
     * 转换评论记录
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
     * 从数据库记录转换为收藏行为
     */
    public FavoriteAction convertToFavoriteAction(UserFavorite favorite) {
        return FavoriteAction.fromDatabase(
                favorite.getRelicsId(),
                favorite.getCreateTime(),
                favorite.getStatus() == 1
        );
    }

    /**
     * 从数据库记录转换为评论行为
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
                comment.getStatus() == 1
        );
    }



    /**
     * 构建交互活动记录
     */
    public InteractionActivity buildFavoriteActivity(UserFavorite favorite, String relicsName) {
        return InteractionActivity.builder()
                .username(favorite.getUsername())
                .relicsId(favorite.getRelicsId())
                .relicsName(relicsName)
                .activityType(favorite.getStatus() == 0 ? 
                        InteractionActivity.ActivityType.FAVORITE_ADDED : 
                        InteractionActivity.ActivityType.FAVORITE_REMOVED)
                .activityDescription(buildFavoriteDescription(favorite))
                .activityTime(favorite.getCreateTime())
                .activityData(favorite.getId())
                .build();
    }

    /**
     * 构建评论活动记录
     */
    public InteractionActivity buildCommentActivity(UserComment comment, String relicsName) {
        return InteractionActivity.builder()
                .username(comment.getUsername())
                .relicsId(comment.getRelicsId())
                .relicsName(relicsName)
                .activityType(determineCommentActivityType(comment))
                .activityDescription(buildCommentDescription(comment))
                .activityTime(comment.getCreateTime())
                .activityData(comment.getCommentId())
                .build();
    }

    /**
     * 构建收藏描述
     */
    private String buildFavoriteDescription(UserFavorite favorite) {
        return favorite.getStatus() == 0 ? "收藏了文物" : "取消收藏文物";
    }

    /**
     * 构建评论描述
     */
    private String buildCommentDescription(UserComment comment) {
        String summary = getCommentSummary(comment.getContent());
        
        if (comment.getStatus() != 0) {
            return "删除了评论";
        } else if (comment.getCommentStatus() == 1) {
            return "评论通过审核: " + summary;
        } else if (comment.getCommentStatus() == 2) {
            return "评论被拒绝: " + summary;
        } else {
            return "评论了文物: " + summary;
        }
    }

    /**
     * 确定评论活动类型
     */
    private InteractionActivity.ActivityType determineCommentActivityType(UserComment comment) {
        if (comment.getStatus() != 0) {
            return InteractionActivity.ActivityType.COMMENT_DELETED;
        } else if (comment.getCommentStatus() == 1) {
            return InteractionActivity.ActivityType.COMMENT_APPROVED;
        } else if (comment.getCommentStatus() == 2) {
            return InteractionActivity.ActivityType.COMMENT_REJECTED;
        } else {
            return InteractionActivity.ActivityType.COMMENT_ADDED;
        }
    }

    /**
     * 获取评论摘要
     */
    private String getCommentSummary(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "[空评论]";
        }
        
        String trimmed = content.trim();
        return trimmed.length() <= 30 ? trimmed : trimmed.substring(0, 30) + "...";
    }

    /**
     * 计算最早时间
     */
    private LocalDateTime calculateEarliestTime(List<UserFavorite> favorites, List<UserComment> comments) {
        LocalDateTime earliest = null;

        for (UserFavorite favorite : favorites) {
            if (earliest == null || favorite.getCreateTime().isBefore(earliest)) {
                earliest = favorite.getCreateTime();
            }
        }

        for (UserComment comment : comments) {
            if (earliest == null || comment.getCreateTime().isBefore(earliest)) {
                earliest = comment.getCreateTime();
            }
        }

        return earliest;
    }

    /**
     * 计算最晚时间
     */
    private LocalDateTime calculateLatestTime(List<UserFavorite> favorites, List<UserComment> comments) {
        LocalDateTime latest = null;

        for (UserFavorite favorite : favorites) {
            LocalDateTime time = favorite.getUpdateTime() != null ? favorite.getUpdateTime() : favorite.getCreateTime();
            if (latest == null || time.isAfter(latest)) {
                latest = time;
            }
        }

        for (UserComment comment : comments) {
            LocalDateTime time = comment.getUpdateTime() != null ? comment.getUpdateTime() : comment.getCreateTime();
            if (latest == null || time.isAfter(latest)) {
                latest = time;
            }
        }

        return latest;
    }
}
