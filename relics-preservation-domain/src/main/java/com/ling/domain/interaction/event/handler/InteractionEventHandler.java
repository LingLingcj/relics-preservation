package com.ling.domain.interaction.event.handler;

import com.ling.domain.interaction.event.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 交互事件处理器
 * @Author: LingRJ
 * @Description: 处理交互相关的领域事件
 * @DateTime: 2025/7/11
 */
@Component
@Slf4j
public class InteractionEventHandler {
    
    /**
     * 处理用户收藏文物事件
     * @param event 收藏事件
     */
    @EventListener
    @Async
    public void handleUserFavoritedRelics(UserFavoritedRelicsEvent event) {
        log.info("处理用户收藏事件: 用户={}, 文物ID={}, 时间={}", 
                event.getUsername(), event.getRelicsId(), event.getOccurredOn());
        
        try {
            // 1. 更新文物收藏统计
            updateRelicsFavoriteCount(event.getRelicsId(), 1);
            
            // 2. 更新用户活跃度
            updateUserActivity(event.getUsername(), "FAVORITE");
            
            // 3. 推荐相关文物
            generateRelicsRecommendations(event.getUsername(), event.getRelicsId());
            
            // 4. 记录用户行为日志
            recordUserBehavior(event.getUsername(), "FAVORITE", event.getRelicsId());
            
            log.info("用户收藏事件处理完成: {}", event.getUsername());
            
        } catch (Exception e) {
            log.error("处理用户收藏事件失败: {} - {}", event.getUsername(), e.getMessage(), e);
        }
    }
    
    /**
     * 处理用户取消收藏事件
     * @param event 取消收藏事件
     */
    @EventListener
    @Async
    public void handleUserUnfavoritedRelics(UserUnfavoritedRelicsEvent event) {
        log.info("处理用户取消收藏事件: 用户={}, 文物ID={}, 时间={}", 
                event.getUsername(), event.getRelicsId(), event.getOccurredOn());
        
        try {
            // 1. 更新文物收藏统计
            updateRelicsFavoriteCount(event.getRelicsId(), -1);
            
            // 2. 更新用户活跃度
            updateUserActivity(event.getUsername(), "UNFAVORITE");
            
            // 3. 记录用户行为日志
            recordUserBehavior(event.getUsername(), "UNFAVORITE", event.getRelicsId());
            
            log.info("用户取消收藏事件处理完成: {}", event.getUsername());
            
        } catch (Exception e) {
            log.error("处理用户取消收藏事件失败: {} - {}", event.getUsername(), e.getMessage(), e);
        }
    }
    
    /**
     * 处理用户评论文物事件
     * @param event 评论事件
     */
    @EventListener
    @Async
    public void handleUserCommentedOnRelics(UserCommentedOnRelicsEvent event) {
        log.info("处理用户评论事件: 用户={}, 文物ID={}, 评论ID={}, 时间={}", 
                event.getUsername(), event.getRelicsId(), event.getCommentId(), event.getOccurredOn());
        
        try {
            // 1. 更新文物评论统计
            updateRelicsCommentCount(event.getRelicsId(), 1);
            
            // 2. 更新用户活跃度
            updateUserActivity(event.getUsername(), "COMMENT");
            
            // 3. 内容审核（如果需要）
            if (needsContentReview(event.getContent())) {
                triggerContentReview(event.getCommentId(), event.getContent());
            }
            
            // 4. 通知相关用户（如文物专家）
            notifyRelicsExperts(event.getRelicsId(), event.getCommentId());
            
            // 5. 记录用户行为日志
            recordUserBehavior(event.getUsername(), "COMMENT", event.getRelicsId());
            
            log.info("用户评论事件处理完成: {}", event.getUsername());
            
        } catch (Exception e) {
            log.error("处理用户评论事件失败: {} - {}", event.getUsername(), e.getMessage(), e);
        }
    }
    
    /**
     * 处理评论删除事件
     * @param event 评论删除事件
     */
    @EventListener
    @Async
    public void handleCommentDeleted(CommentDeletedEvent event) {
        log.info("处理评论删除事件: 用户={}, 文物ID={}, 评论ID={}, 时间={}", 
                event.getUsername(), event.getRelicsId(), event.getCommentId(), event.getOccurredOn());
        
        try {
            // 1. 更新文物评论统计
            updateRelicsCommentCount(event.getRelicsId(), -1);
            
            // 2. 清理相关缓存
            clearCommentCache(event.getCommentId());
            
            // 3. 记录删除日志
            recordUserBehavior(event.getUsername(), "DELETE_COMMENT", event.getRelicsId());
            
            log.info("评论删除事件处理完成: {}", event.getUsername());
            
        } catch (Exception e) {
            log.error("处理评论删除事件失败: {} - {}", event.getUsername(), e.getMessage(), e);
        }
    }
    
    // ==================== 私有辅助方法 ====================
    
    /**
     * 更新文物收藏统计
     */
    private void updateRelicsFavoriteCount(Long relicsId, int delta) {
        log.debug("更新文物收藏统计: relicsId={}, delta={}", relicsId, delta);
        // TODO: 实现文物收藏统计更新逻辑
    }
    
    /**
     * 更新文物评论统计
     */
    private void updateRelicsCommentCount(Long relicsId, int delta) {
        log.debug("更新文物评论统计: relicsId={}, delta={}", relicsId, delta);
        // TODO: 实现文物评论统计更新逻辑
    }
    
    /**
     * 更新用户活跃度
     */
    private void updateUserActivity(String username, String activityType) {
        log.debug("更新用户活跃度: username={}, activityType={}", username, activityType);
        // TODO: 实现用户活跃度更新逻辑
    }
    
    /**
     * 生成文物推荐
     */
    private void generateRelicsRecommendations(String username, Long relicsId) {
        log.debug("生成文物推荐: username={}, relicsId={}", username, relicsId);
        // TODO: 实现文物推荐逻辑
    }
    
    /**
     * 记录用户行为
     */
    private void recordUserBehavior(String username, String action, Long relicsId) {
        log.debug("记录用户行为: username={}, action={}, relicsId={}", username, action, relicsId);
        // TODO: 实现用户行为记录逻辑
    }
    
    /**
     * 检查是否需要内容审核
     */
    private boolean needsContentReview(String content) {
        // 简单的敏感词检测
        return content != null && (content.length() > 200 || 
                content.toLowerCase().contains("敏感词"));
    }
    
    /**
     * 触发内容审核
     */
    private void triggerContentReview(Long commentId, String content) {
        log.debug("触发内容审核: commentId={}", commentId);
        // TODO: 实现内容审核逻辑
    }
    
    /**
     * 通知文物专家
     */
    private void notifyRelicsExperts(Long relicsId, Long commentId) {
        log.debug("通知文物专家: relicsId={}, commentId={}", relicsId, commentId);
        // TODO: 实现专家通知逻辑
    }
    
    /**
     * 清理评论缓存
     */
    private void clearCommentCache(Long commentId) {
        log.debug("清理评论缓存: commentId={}", commentId);
        // TODO: 实现缓存清理逻辑
    }
}
