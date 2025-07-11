package com.ling.infrastructure.repository;

import com.ling.domain.interaction.adapter.IUserInteractionRepository;
import com.ling.domain.interaction.model.entity.UserInteraction;
import com.ling.domain.interaction.model.valobj.*;
import com.ling.domain.user.model.valobj.Username;
import com.ling.infrastructure.dao.IUserFavoriteDao;
import com.ling.infrastructure.dao.IUserCommentDao;
import com.ling.infrastructure.dao.po.UserFavorite;
import com.ling.infrastructure.dao.po.UserComment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户交互仓储实现
 * @Author: LingRJ
 * @Description: 实现用户交互数据访问逻辑
 * @DateTime: 2025/7/11
 */
@Repository
@Slf4j
public class UserInteractionRepositoryImpl implements IUserInteractionRepository {
    
    @Autowired
    private IUserFavoriteDao userFavoriteDao;
    
    @Autowired
    private IUserCommentDao userCommentDao;
    
    // ==================== 聚合根操作 ====================
    
    @Override
    @Transactional
    public boolean save(UserInteraction userInteraction) {
        try {
            log.debug("保存用户交互: {}", userInteraction.getDisplayName());
            
            // 保存收藏数据
            saveFavorites(userInteraction);
            
            // 保存评论数据
            saveComments(userInteraction);
            
            return true;
            
        } catch (Exception e) {
            log.error("保存用户交互失败: {} - {}", userInteraction.getDisplayName(), e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public Optional<UserInteraction> findByUsername(Username username) {
        try {
            log.debug("查找用户交互: {}", username.getValue());
            
            // 查询收藏数据
            List<UserFavorite> favorites = userFavoriteDao.selectByUsername(
                    username.getValue(), 0, Integer.MAX_VALUE);
            
            // 查询评论数据
            List<UserComment> comments = userCommentDao.selectByUsername(
                    username.getValue(), null, 0, Integer.MAX_VALUE);
            
            if (favorites.isEmpty() && comments.isEmpty()) {
                return Optional.empty();
            }
            
            // 构建聚合根
            UserInteraction userInteraction = buildUserInteraction(username, favorites, comments);
            return Optional.of(userInteraction);
            
        } catch (Exception e) {
            log.error("查找用户交互失败: {} - {}", username.getValue(), e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    @Override
    @Transactional
    public boolean deleteByUsername(Username username) {
        try {
            log.info("删除用户交互: {}", username.getValue());
            
            // 逻辑删除收藏记录
            List<UserFavorite> favorites = userFavoriteDao.selectByUsername(
                    username.getValue(), 0, Integer.MAX_VALUE);
            for (UserFavorite favorite : favorites) {
                userFavoriteDao.deleteById(favorite.getId());
            }
            
            // 逻辑删除评论记录
            List<UserComment> comments = userCommentDao.selectByUsername(
                    username.getValue(), null, 0, Integer.MAX_VALUE);
            for (UserComment comment : comments) {
                userCommentDao.deleteById(comment.getId());
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("删除用户交互失败: {} - {}", username.getValue(), e.getMessage(), e);
            return false;
        }
    }
    
    // ==================== 收藏相关查询 ====================
    
    @Override
    public boolean isFavorited(Username username, Long relicsId) {
        try {
            UserFavorite favorite = userFavoriteDao.selectByUsernameAndRelicsId(
                    username.getValue(), relicsId);
            return favorite != null && favorite.getStatus() == 0;
        } catch (Exception e) {
            log.error("检查收藏状态失败: {} - {}", username.getValue(), e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public List<Long> getUserFavoriteRelicsIds(Username username, int page, int size) {
        try {
            int offset = (page - 1) * size;
            List<UserFavorite> favorites = userFavoriteDao.selectByUsername(
                    username.getValue(), offset, size);
            return favorites.stream()
                    .map(UserFavorite::getRelicsId)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取用户收藏列表失败: {} - {}", username.getValue(), e.getMessage(), e);
            return List.of();
        }
    }
    
    @Override
    public long getUserFavoriteCount(Username username) {
        try {
            return userFavoriteDao.countByUsername(username.getValue());
        } catch (Exception e) {
            log.error("获取用户收藏总数失败: {} - {}", username.getValue(), e.getMessage(), e);
            return 0;
        }
    }
    
    @Override
    public List<String> getRelicsFavoriteUsers(Long relicsId, int page, int size) {
        try {
            int offset = (page - 1) * size;
            List<UserFavorite> favorites = userFavoriteDao.selectByRelicsId(
                    relicsId, offset, size);
            return favorites.stream()
                    .map(UserFavorite::getUsername)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取文物收藏用户失败: {} - {}", relicsId, e.getMessage(), e);
            return List.of();
        }
    }
    
    @Override
    public long getRelicsFavoriteCount(Long relicsId) {
        try {
            return userFavoriteDao.countByRelicsId(relicsId);
        } catch (Exception e) {
            log.error("获取文物收藏总数失败: {} - {}", relicsId, e.getMessage(), e);
            return 0;
        }
    }
    
    // ==================== 评论相关查询 ====================
    
    @Override
    public List<CommentAction> getUserComments(Username username, Long relicsId, int page, int size) {
        try {
            int offset = (page - 1) * size;
            List<UserComment> comments = userCommentDao.selectByUsername(
                    username.getValue(), relicsId, offset, size);
            return comments.stream()
                    .map(this::convertToCommentAction)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取用户评论失败: {} - {}", username.getValue(), e.getMessage(), e);
            return List.of();
        }
    }
    
    @Override
    public long getUserCommentCount(Username username, Long relicsId) {
        try {
            return userCommentDao.countByUsername(username.getValue(), relicsId);
        } catch (Exception e) {
            log.error("获取用户评论总数失败: {} - {}", username.getValue(), e.getMessage(), e);
            return 0;
        }
    }
    
    @Override
    public List<CommentAction> getRelicsComments(Long relicsId, int page, int size) {
        try {
            int offset = (page - 1) * size;
            List<UserComment> comments = userCommentDao.selectByRelicsId(
                    relicsId, offset, size);
            return comments.stream()
                    .map(this::convertToCommentAction)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取文物评论失败: {} - {}", relicsId, e.getMessage(), e);
            return List.of();
        }
    }
    
    @Override
    public long getRelicsCommentCount(Long relicsId) {
        try {
            return userCommentDao.countByRelicsId(relicsId);
        } catch (Exception e) {
            log.error("获取文物评论总数失败: {} - {}", relicsId, e.getMessage(), e);
            return 0;
        }
    }
    
    @Override
    public Optional<CommentAction> findCommentById(Long commentId) {
        try {
            UserComment comment = userCommentDao.selectByCommentId(commentId);
            return comment != null ? Optional.of(convertToCommentAction(comment)) : Optional.empty();
        } catch (Exception e) {
            log.error("查找评论失败: {} - {}", commentId, e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    @Override
    public List<CommentWithUser> getPendingComments(Long relicsId, int page, int size) {
        try {
            int offset = (page - 1) * size;
            List<UserComment> comments = userCommentDao.selectPendingComments(
                    relicsId, offset, size);
            return comments.stream()
                    .map(this::convertToCommentWithUser)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取待审核评论失败: {} - {}", relicsId, e.getMessage(), e);
            return List.of();
        }
    }
    
    // ==================== 统计查询 ====================
    
    @Override
    public InteractionStatistics getUserStatistics(Username username) {
        try {
            long favoriteCount = getUserFavoriteCount(username);
            long commentCount = getUserCommentCount(username, null);
            
            return InteractionStatistics.builder()
                    .username(username.getValue())
                    .favoriteCount(favoriteCount)
                    .commentCount(commentCount)
                    .lastActiveTime(LocalDateTime.now()) // TODO: 实际计算最后活跃时间
                    .build();
        } catch (Exception e) {
            log.error("获取用户统计失败: {} - {}", username.getValue(), e.getMessage(), e);
            return InteractionStatistics.builder()
                    .username(username.getValue())
                    .favoriteCount(0)
                    .commentCount(0)
                    .build();
        }
    }
    
    @Override
    public RelicsInteractionStatistics getRelicsStatistics(Long relicsId) {
        try {
            long favoriteCount = getRelicsFavoriteCount(relicsId);
            long commentCount = getRelicsCommentCount(relicsId);
            
            return new RelicsInteractionStatistics(
                    relicsId,
                    favoriteCount,
                    commentCount,
                    favoriteCount + commentCount,
                    LocalDateTime.now(), // TODO: 实际计算最后交互时间
                    calculatePopularityScore(favoriteCount, commentCount)
            );
        } catch (Exception e) {
            log.error("获取文物统计失败: {} - {}", relicsId, e.getMessage(), e);
            return new RelicsInteractionStatistics(relicsId, 0, 0, 0, null, 0.0);
        }
    }
    
    @Override
    public List<RelicsInteractionSummary> getPopularRelics(int limit) {
        try {
            // TODO: 实现热门文物查询逻辑
            return List.of();
        } catch (Exception e) {
            log.error("获取热门文物失败: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    @Override
    public List<RelicsInteractionSummary> getRecentlyInteractedRelics(int limit) {
        try {
            // TODO: 实现最近交互文物查询逻辑
            return List.of();
        } catch (Exception e) {
            log.error("获取最近交互文物失败: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    @Override
    public List<String> getActiveUsers(int limit) {
        try {
            // TODO: 实现活跃用户查询逻辑
            return List.of();
        } catch (Exception e) {
            log.error("获取活跃用户失败: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    // ==================== 批量操作 ====================
    
    @Override
    public Map<Long, Boolean> batchCheckFavoriteStatus(Username username, List<Long> relicsIds) {
        try {
            List<UserFavorite> favorites = userFavoriteDao.batchSelectByUsernameAndRelicsIds(
                    username.getValue(), relicsIds);
            
            Set<Long> favoritedIds = favorites.stream()
                    .filter(f -> f.getStatus() == 0)
                    .map(UserFavorite::getRelicsId)
                    .collect(Collectors.toSet());
            
            return relicsIds.stream()
                    .collect(Collectors.toMap(id -> id, favoritedIds::contains));
        } catch (Exception e) {
            log.error("批量检查收藏状态失败: {} - {}", username.getValue(), e.getMessage(), e);
            return relicsIds.stream()
                    .collect(Collectors.toMap(id -> id, id -> false));
        }
    }
    
    @Override
    public Map<Long, Long> batchGetRelicsFavoriteCounts(List<Long> relicsIds) {
        try {
            List<IUserFavoriteDao.RelicsFavoriteCount> counts = 
                    userFavoriteDao.batchCountByRelicsIds(relicsIds);
            
            return counts.stream()
                    .collect(Collectors.toMap(
                            IUserFavoriteDao.RelicsFavoriteCount::relicsId,
                            IUserFavoriteDao.RelicsFavoriteCount::favoriteCount));
        } catch (Exception e) {
            log.error("批量获取文物收藏数量失败: {}", e.getMessage(), e);
            return relicsIds.stream()
                    .collect(Collectors.toMap(id -> id, id -> 0L));
        }
    }
    
    @Override
    public Map<Long, Long> batchGetRelicsCommentCounts(List<Long> relicsIds) {
        try {
            List<IUserCommentDao.RelicsCommentCount> counts = 
                    userCommentDao.batchCountByRelicsIds(relicsIds);
            
            return counts.stream()
                    .collect(Collectors.toMap(
                            IUserCommentDao.RelicsCommentCount::relicsId,
                            IUserCommentDao.RelicsCommentCount::commentCount));
        } catch (Exception e) {
            log.error("批量获取文物评论数量失败: {}", e.getMessage(), e);
            return relicsIds.stream()
                    .collect(Collectors.toMap(id -> id, id -> 0L));
        }
    }
    
    // ==================== 时间范围查询 ====================
    
    @Override
    public List<InteractionActivity> getUserActivitiesInTimeRange(Username username, 
                                                                LocalDateTime startTime, 
                                                                LocalDateTime endTime) {
        try {
            // TODO: 实现用户活动查询逻辑
            return List.of();
        } catch (Exception e) {
            log.error("获取用户活动失败: {} - {}", username.getValue(), e.getMessage(), e);
            return List.of();
        }
    }
    
    @Override
    public List<InteractionActivity> getRelicsActivitiesInTimeRange(Long relicsId, 
                                                                  LocalDateTime startTime, 
                                                                  LocalDateTime endTime) {
        try {
            // TODO: 实现文物活动查询逻辑
            return List.of();
        } catch (Exception e) {
            log.error("获取文物活动失败: {} - {}", relicsId, e.getMessage(), e);
            return List.of();
        }
    }
    
    // ==================== 私有辅助方法 ====================
    
    /**
     * 保存收藏数据
     */
    private void saveFavorites(UserInteraction userInteraction) {
        // TODO: 实现收藏数据保存逻辑
        // 需要比较现有数据和聚合根中的数据，进行增删改操作
    }
    
    /**
     * 保存评论数据
     */
    private void saveComments(UserInteraction userInteraction) {
        // TODO: 实现评论数据保存逻辑
        // 需要比较现有数据和聚合根中的数据，进行增删改操作
    }
    
    /**
     * 构建用户交互聚合根
     */
    private UserInteraction buildUserInteraction(Username username, 
                                               List<UserFavorite> favorites, 
                                               List<UserComment> comments) {
        // TODO: 实现聚合根构建逻辑
        return UserInteraction.create(username);
    }
    
    /**
     * 转换为评论行为值对象
     */
    private CommentAction convertToCommentAction(UserComment comment) {
        CommentContent content = CommentContent.of(comment.getContent());
        return CommentAction.create(comment.getRelicsId(), content);
    }

    /**
     * 转换为包含用户信息的评论值对象
     */
    private CommentWithUser convertToCommentWithUser(UserComment comment) {
        return CommentWithUser.builder()
                .commentId(comment.getCommentId())
                .relicsId(comment.getRelicsId())
                .username(comment.getUsername())
                .content(comment.getContent())
                .status(CommentStatus.fromCode(comment.getCommentStatus()))
                .createTime(comment.getCreateTime())
                .updateTime(comment.getUpdateTime())
                .deleted(comment.getStatus() == 1) // status=1表示已删除
                .build();
    }
    
    /**
     * 计算热度分数
     */
    private double calculatePopularityScore(long favoriteCount, long commentCount) {
        // 简单的热度计算公式：收藏数 * 2 + 评论数 * 1
        return favoriteCount * 2.0 + commentCount * 1.0;
    }
}
