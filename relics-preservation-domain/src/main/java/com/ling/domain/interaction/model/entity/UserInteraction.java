package com.ling.domain.interaction.model.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.ling.domain.interaction.event.CommentDeletedEvent;
import com.ling.domain.interaction.event.UserCommentedOnRelicsEvent;
import com.ling.domain.interaction.event.UserFavoritedRelicsEvent;
import com.ling.domain.interaction.event.UserUnfavoritedRelicsEvent;
import com.ling.domain.interaction.model.valobj.CommentAction;
import com.ling.domain.interaction.model.valobj.CommentContent;
import com.ling.domain.interaction.model.valobj.FavoriteAction;
import com.ling.domain.interaction.model.valobj.InteractionResult;
import com.ling.domain.interaction.model.valobj.InteractionStatistics;
import com.ling.domain.user.event.DomainEventPublisher;
import com.ling.domain.user.model.valobj.Username;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户交互聚合根
 * @Author: LingRJ
 * @Description: 管理单个用户与文物的所有交互行为
 * @DateTime: 2025/7/11
 */
@Getter
@Builder
@Slf4j
public class UserInteraction {
    
    private Username username;
    private Set<FavoriteAction> favorites;
    private List<CommentAction> comments;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    /**
     * 创建用户交互聚合根
     */
    public static UserInteraction create(Username username) {
        LocalDateTime now = LocalDateTime.now();
        return UserInteraction.builder()
                .username(username)
                .favorites(new HashSet<>())
                .comments(new ArrayList<>())
                .createTime(now)
                .updateTime(now)
                .build();
    }

    /**
     * 从数据库记录重建用户交互聚合根
     * @param username 用户名
     * @param favorites 收藏行为集合
     * @param comments 评论行为列表
     * @param createTime 创建时间
     * @param updateTime 更新时间
     * @return 用户交互聚合根
     */
    public static UserInteraction fromDatabase(Username username,
                                             Set<FavoriteAction> favorites,
                                             List<CommentAction> comments,
                                             LocalDateTime createTime,
                                             LocalDateTime updateTime) {
        return UserInteraction.builder()
                .username(username)
                .favorites(favorites != null ? favorites : new HashSet<>())
                .comments(comments != null ? comments : new ArrayList<>())
                .createTime(createTime != null ? createTime : LocalDateTime.now())
                .updateTime(updateTime != null ? updateTime : LocalDateTime.now())
                .build();
    }
    
    /**
     * 添加收藏
     * @param relicsId 文物ID
     * @return 操作结果
     */
    public InteractionResult addFavorite(Long relicsId) {
        try {
            FavoriteAction favorite = FavoriteAction.create(relicsId);
            
            // 检查是否已收藏
            if (favorites.contains(favorite)) {
                return InteractionResult.failure("已经收藏过该文物");
            }
            
            favorites.add(favorite);
            updateTime = LocalDateTime.now();
            
            // 发布收藏事件
            DomainEventPublisher.publish(new UserFavoritedRelicsEvent(
                    username.getValue(), relicsId));
            
            log.info("用户 {} 收藏文物 {}", username.getValue(), relicsId);
            return InteractionResult.success("收藏成功");
            
        } catch (Exception e) {
            log.error("添加收藏失败: {} - {}", username.getValue(), e.getMessage(), e);
            return InteractionResult.failure("收藏失败: " + e.getMessage());
        }
    }
    
    /**
     * 取消收藏
     * @param relicsId 文物ID
     * @return 操作结果
     */
    public InteractionResult removeFavorite(Long relicsId) {
        try {
            FavoriteAction favorite = FavoriteAction.create(relicsId);
            
            if (!favorites.contains(favorite)) {
                return InteractionResult.failure("未收藏该文物");
            }
            
            favorites.remove(favorite);
            updateTime = LocalDateTime.now();
            
            // 发布取消收藏事件
            DomainEventPublisher.publish(new UserUnfavoritedRelicsEvent(
                    username.getValue(), relicsId));
            
            log.info("用户 {} 取消收藏文物 {}", username.getValue(), relicsId);
            return InteractionResult.success("取消收藏成功");
            
        } catch (Exception e) {
            log.error("取消收藏失败: {} - {}", username.getValue(), e.getMessage(), e);
            return InteractionResult.failure("取消收藏失败: " + e.getMessage());
        }
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
     * 检查是否已收藏文物
     * @param relicsId 文物ID
     * @return 是否已收藏
     */
    public boolean isFavorited(Long relicsId) {
        return favorites.contains(FavoriteAction.create(relicsId));
    }
    
    /**
     * 获取收藏的文物ID列表
     * @return 文物ID列表
     */
    public List<Long> getFavoritedRelicsIds() {
        return favorites.stream()
                .filter(f -> !f.isDeleted())
                .map(FavoriteAction::getRelicsId)
                .sorted()
                .toList();
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
     * 获取用户的交互统计
     * @return 交互统计信息
     */
    public InteractionStatistics getStatistics() {
        long favoriteCount = favorites.stream()
                .filter(f -> !f.isDeleted())
                .count();
        
        long commentCount = comments.stream()
                .filter(c -> !c.isDeleted())
                .count();
        
        return InteractionStatistics.builder()
                .username(username.getValue())
                .favoriteCount(favoriteCount)
                .commentCount(commentCount)
                .lastActiveTime(updateTime)
                .build();
    }
    
    /**
     * 获取显示名称
     * @return 显示名称
     */
    public String getDisplayName() {
        return username.getValue();
    }
}
