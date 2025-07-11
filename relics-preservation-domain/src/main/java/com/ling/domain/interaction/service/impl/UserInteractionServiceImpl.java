package com.ling.domain.interaction.service.impl;

import com.ling.domain.interaction.adapter.IUserInteractionRepository;
import com.ling.domain.interaction.model.entity.UserInteraction;
import com.ling.domain.interaction.model.valobj.*;
import com.ling.domain.interaction.service.IUserInteractionService;
import com.ling.domain.user.model.valobj.Username;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户交互服务实现
 * @Author: LingRJ
 * @Description: 实现用户与文物的交互业务逻辑
 * @DateTime: 2025/7/11
 */
@Service
@Slf4j
public class UserInteractionServiceImpl implements IUserInteractionService {
    
    @Autowired
    private IUserInteractionRepository userInteractionRepository;
    
    // ==================== 收藏相关 ====================
    
    @Override
    @Transactional
    public InteractionResult addFavorite(String username, Long relicsId) {
        try {
            log.info("用户 {} 尝试收藏文物 {}", username, relicsId);
            
            // 获取或创建用户交互聚合根
            UserInteraction userInteraction = getUserInteractionOrCreate(username);
            
            // 执行收藏操作
            InteractionResult result = userInteraction.addFavorite(relicsId);
            
            if (result.isSuccess()) {
                // 保存聚合根
                boolean saved = userInteractionRepository.save(userInteraction);
                if (!saved) {
                    log.error("保存用户交互失败: {}", username);
                    return InteractionResult.failure("保存失败");
                }
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("添加收藏失败: {} - {}", username, e.getMessage(), e);
            return InteractionResult.failure("添加收藏失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public InteractionResult removeFavorite(String username, Long relicsId) {
        try {
            log.info("用户 {} 尝试取消收藏文物 {}", username, relicsId);
            
            Optional<UserInteraction> userInteractionOpt = getUserInteraction(username);
            if (userInteractionOpt.isEmpty()) {
                return InteractionResult.failure("用户交互记录不存在");
            }
            
            UserInteraction userInteraction = userInteractionOpt.get();
            InteractionResult result = userInteraction.removeFavorite(relicsId);
            
            if (result.isSuccess()) {
                boolean saved = userInteractionRepository.save(userInteraction);
                if (!saved) {
                    log.error("保存用户交互失败: {}", username);
                    return InteractionResult.failure("保存失败");
                }
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("取消收藏失败: {} - {}", username, e.getMessage(), e);
            return InteractionResult.failure("取消收藏失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean isFavorited(String username, Long relicsId) {
        try {
            Optional<UserInteraction> userInteractionOpt = getUserInteraction(username);
            return userInteractionOpt.map(ui -> ui.isFavorited(relicsId)).orElse(false);
        } catch (Exception e) {
            log.error("检查收藏状态失败: {} - {}", username, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public FavoriteListResult getUserFavorites(String username, int page, int size) {
        try {
            Optional<UserInteraction> userInteractionOpt = getUserInteraction(username);
            if (userInteractionOpt.isEmpty()) {
                return new FavoriteListResult(List.of(), 0, page, size, false);
            }
            
            UserInteraction userInteraction = userInteractionOpt.get();
            List<Long> favoritedRelicsIds = userInteraction.getFavoritedRelicsIds();
            
            // 简单分页（实际应该在仓储层实现）
            int start = (page - 1) * size;
            int end = Math.min(start + size, favoritedRelicsIds.size());
            
            List<FavoriteAction> favorites = favoritedRelicsIds.subList(start, end).stream()
                    .map(FavoriteAction::create)
                    .collect(Collectors.toList());
            
            boolean hasNext = end < favoritedRelicsIds.size();
            
            return new FavoriteListResult(favorites, favoritedRelicsIds.size(), page, size, hasNext);
            
        } catch (Exception e) {
            log.error("获取用户收藏列表失败: {} - {}", username, e.getMessage(), e);
            return new FavoriteListResult(List.of(), 0, page, size, false);
        }
    }
    
    // ==================== 评论相关 ====================
    
    @Override
    @Transactional
    public InteractionResult addComment(String username, Long relicsId, String content) {
        try {
            log.info("用户 {} 尝试评论文物 {}: {}", username, relicsId, 
                    content.length() > 50 ? content.substring(0, 50) + "..." : content);
            
            // 获取或创建用户交互聚合根
            UserInteraction userInteraction = getUserInteractionOrCreate(username);
            
            // 执行评论操作
            InteractionResult result = userInteraction.addComment(relicsId, content);
            
            if (result.isSuccess()) {
                // 保存聚合根
                boolean saved = userInteractionRepository.save(userInteraction);
                if (!saved) {
                    log.error("保存用户交互失败: {}", username);
                    return InteractionResult.failure("保存失败");
                }
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("添加评论失败: {} - {}", username, e.getMessage(), e);
            return InteractionResult.failure("添加评论失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public InteractionResult deleteComment(String username, Long commentId) {
        try {
            log.info("用户 {} 尝试删除评论 {}", username, commentId);
            
            Optional<UserInteraction> userInteractionOpt = getUserInteraction(username);
            if (userInteractionOpt.isEmpty()) {
                return InteractionResult.failure("用户交互记录不存在");
            }
            
            UserInteraction userInteraction = userInteractionOpt.get();
            InteractionResult result = userInteraction.deleteComment(commentId);
            
            if (result.isSuccess()) {
                boolean saved = userInteractionRepository.save(userInteraction);
                if (!saved) {
                    log.error("保存用户交互失败: {}", username);
                    return InteractionResult.failure("保存失败");
                }
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("删除评论失败: {} - {}", username, e.getMessage(), e);
            return InteractionResult.failure("删除评论失败: " + e.getMessage());
        }
    }
    
    @Override
    public CommentListResult getUserComments(String username, Long relicsId, int page, int size) {
        try {
            Optional<UserInteraction> userInteractionOpt = getUserInteraction(username);
            if (userInteractionOpt.isEmpty()) {
                return new CommentListResult(List.of(), 0, page, size, false);
            }
            
            UserInteraction userInteraction = userInteractionOpt.get();
            List<CommentAction> comments = userInteraction.getComments(relicsId);
            
            // 简单分页（实际应该在仓储层实现）
            int start = (page - 1) * size;
            int end = Math.min(start + size, comments.size());
            
            List<CommentAction> pageComments = comments.subList(start, end);
            boolean hasNext = end < comments.size();
            
            return new CommentListResult(pageComments, comments.size(), page, size, hasNext);
            
        } catch (Exception e) {
            log.error("获取用户评论列表失败: {} - {}", username, e.getMessage(), e);
            return new CommentListResult(List.of(), 0, page, size, false);
        }
    }

    @Override
    public RelicsCommentListResult getRelicsComments(Long relicsId, int page, int size) {
        try {
            // 参数验证
            if (relicsId == null || relicsId <= 0) {
                log.warn("文物ID无效: {}", relicsId);
                return RelicsCommentListResult.empty(relicsId, page, size);
            }

            if (page < 1) page = 1;
            if (size < 1) size = 10;
            if (size > 100) size = 100; // 限制最大页面大小

            // 获取已通过审核的评论列表
            List<RelicsComment> comments = userInteractionRepository.getApprovedCommentsByRelicsId(relicsId, page, size);

            // 获取总数
            Long totalCount = userInteractionRepository.countApprovedCommentsByRelicsId(relicsId);

            log.debug("获取文物评论成功: relicsId={}, page={}, size={}, totalCount={}",
                     relicsId, page, size, totalCount);

            return RelicsCommentListResult.success(comments, totalCount, page, size, relicsId);

        } catch (Exception e) {
            log.error("获取文物评论列表失败: relicsId={}, page={}, size={} - {}",
                     relicsId, page, size, e.getMessage(), e);
            return RelicsCommentListResult.empty(relicsId, page, size);
        }
    }

    // ==================== 交互管理 ====================
    
    @Override
    public Optional<UserInteraction> getUserInteraction(String username) {
        try {
            return userInteractionRepository.findByUsername(Username.of(username));
        } catch (Exception e) {
            log.error("获取用户交互失败: {} - {}", username, e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    @Override
    @Transactional
    public UserInteraction createUserInteraction(String username) {
        try {
            UserInteraction userInteraction = UserInteraction.create(Username.of(username));
            boolean saved = userInteractionRepository.save(userInteraction);
            if (!saved) {
                throw new RuntimeException("创建用户交互失败");
            }
            return userInteraction;
        } catch (Exception e) {
            log.error("创建用户交互失败: {} - {}", username, e.getMessage(), e);
            throw new RuntimeException("创建用户交互失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean saveUserInteraction(UserInteraction userInteraction) {
        try {
            return userInteractionRepository.save(userInteraction);
        } catch (Exception e) {
            log.error("保存用户交互失败: {} - {}", userInteraction.getDisplayName(), e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public InteractionStatistics getUserStatistics(String username) {
        try {
            Optional<UserInteraction> userInteractionOpt = getUserInteraction(username);
            return userInteractionOpt.map(UserInteraction::getStatistics)
                    .orElse(InteractionStatistics.builder()
                            .username(username)
                            .favoriteCount(0)
                            .commentCount(0)
                            .build());
        } catch (Exception e) {
            log.error("获取用户统计失败: {} - {}", username, e.getMessage(), e);
            return InteractionStatistics.builder()
                    .username(username)
                    .favoriteCount(0)
                    .commentCount(0)
                    .build();
        }
    }
    
    // ==================== 批量操作 ====================
    
    @Override
    public Map<Long, Boolean> batchCheckFavoriteStatus(String username, List<Long> relicsIds) {
        try {
            Optional<UserInteraction> userInteractionOpt = getUserInteraction(username);
            if (userInteractionOpt.isEmpty()) {
                return relicsIds.stream()
                        .collect(Collectors.toMap(id -> id, id -> false));
            }
            
            UserInteraction userInteraction = userInteractionOpt.get();
            return relicsIds.stream()
                    .collect(Collectors.toMap(id -> id, userInteraction::isFavorited));
                    
        } catch (Exception e) {
            log.error("批量检查收藏状态失败: {} - {}", username, e.getMessage(), e);
            return relicsIds.stream()
                    .collect(Collectors.toMap(id -> id, id -> false));
        }
    }
    
    @Override
    public List<InteractionActivity> getRecentActivities(String username, int limit) {
        try {
            // TODO: 实现最近活动获取逻辑
            // 这里应该从活动记录表或事件存储中获取
            return List.of();
        } catch (Exception e) {
            log.error("获取最近活动失败: {} - {}", username, e.getMessage(), e);
            return List.of();
        }
    }
    
    // ==================== 私有辅助方法 ====================
    
    /**
     * 获取用户交互聚合根，如果不存在则创建
     */
    private UserInteraction getUserInteractionOrCreate(String username) {
        Optional<UserInteraction> userInteractionOpt = getUserInteraction(username);
        return userInteractionOpt.orElseGet(() -> createUserInteraction(username));
    }
}
