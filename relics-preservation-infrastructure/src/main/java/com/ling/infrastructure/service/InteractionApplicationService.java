package com.ling.infrastructure.service;

import com.ling.domain.interaction.service.IInteractionStatisticsService;
import com.ling.domain.interaction.service.impl.InteractionStatisticsService;
import com.ling.domain.interaction.model.valobj.InteractionStatistics;
import com.ling.domain.user.model.valobj.Username;
import com.ling.infrastructure.dao.IUserCommentDao;
import com.ling.infrastructure.dao.IUserFavoriteDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 交互应用服务 - 协调领域服务和基础设施
 * @Author: LingRJ
 * @Description: 按照DDD架构，协调领域层和基础设施层，不包含业务逻辑
 * @DateTime: 2025/7/11
 */
@Slf4j
@Service
public class InteractionApplicationService {
    
    @Autowired
    private IUserFavoriteDao userFavoriteDao;
    
    @Autowired
    private IUserCommentDao userCommentDao;
    
    @Autowired
    private InteractionStatisticsService statisticsService; // 领域服务
    
    /**
     * 获取热门文物列表 - 协调数据获取和业务处理
     */
    public List<IInteractionStatisticsService.RelicsInteractionSummary> getPopularRelics(int limit) {
        try {
            log.debug("获取热门文物列表，限制数量: {}", limit);
            
            // 从基础设施层获取数据
            var favoriteData = userFavoriteDao.selectPopularRelics(limit * 2);
            var commentData = userCommentDao.selectPopularRelicsByComments(limit * 2);
            
            // 转换为领域对象
            var favoriteDomainData = favoriteData.stream()
                    .map(f -> new InteractionStatisticsService.FavoriteData(
                            f.relicsId(), f.relicsName(), f.favoriteCount(), f.lastFavoriteTime()))
                    .toList();
            
            var commentDomainData = commentData.stream()
                    .map(c -> new InteractionStatisticsService.CommentData(
                            c.relicsId(), c.relicsName(), c.commentCount(), c.lastCommentTime()))
                    .toList();
            
            // 调用领域服务处理业务逻辑
            return statisticsService.mergePopularRelics(favoriteDomainData, commentDomainData, limit);
                    
        } catch (Exception e) {
            log.error("获取热门文物失败: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * 获取最近交互的文物列表 - 协调数据获取和业务处理
     */
    public List<IInteractionStatisticsService.RelicsInteractionSummary> getRecentlyInteractedRelics(int limit) {
        try {
            log.debug("获取最近交互文物列表，限制数量: {}", limit);
            
            // 从基础设施层获取数据
            var favoriteData = userFavoriteDao.selectRecentlyFavoritedRelics(limit * 2);
            var commentData = userCommentDao.selectRecentlyCommentedRelics(limit * 2);
            
            // 转换为领域对象
            var favoriteDomainData = favoriteData.stream()
                    .map(f -> new InteractionStatisticsService.FavoriteData(
                            f.relicsId(), f.relicsName(), f.favoriteCount(), f.lastFavoriteTime()))
                    .toList();
            
            var commentDomainData = commentData.stream()
                    .map(c -> new InteractionStatisticsService.CommentData(
                            c.relicsId(), c.relicsName(), c.commentCount(), c.lastCommentTime()))
                    .toList();
            
            // 调用领域服务处理业务逻辑
            return statisticsService.mergeRecentRelics(favoriteDomainData, commentDomainData, limit);
                    
        } catch (Exception e) {
            log.error("获取最近交互文物失败: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * 获取活跃用户列表 - 协调数据获取和业务处理
     */
    public List<String> getActiveUsers(int limit) {
        try {
            log.debug("获取活跃用户列表，限制数量: {}", limit);
            
            // 从基础设施层获取数据
            var favoriteUsers = userFavoriteDao.selectActiveUsers(limit * 2);
            var commentUsers = userCommentDao.selectActiveCommenters(limit * 2);
            
            // 转换为领域对象
            var favoriteDomainData = favoriteUsers.stream()
                    .map(u -> new InteractionStatisticsService.UserFavoriteData(
                            u.username(), u.favoriteCount(), u.lastFavoriteTime()))
                    .toList();
            
            var commentDomainData = commentUsers.stream()
                    .map(u -> new InteractionStatisticsService.UserCommentData(
                            u.username(), u.commentCount(), u.lastCommentTime()))
                    .toList();
            
            // 调用领域服务处理业务逻辑
            return statisticsService.mergeActiveUsers(favoriteDomainData, commentDomainData, limit);
                    
        } catch (Exception e) {
            log.error("获取活跃用户失败: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * 获取用户交互统计 - 协调数据获取和业务处理
     */
    public InteractionStatistics getUserStatistics(Username username) {
        try {
            long favoriteCount = userFavoriteDao.countByUsername(username.getValue());
            long commentCount = userCommentDao.countByUsername(username.getValue(), null);
            LocalDateTime lastActiveTime = calculateUserLastActiveTime(username);
            
            return InteractionStatistics.builder()
                    .username(username.getValue())
                    .favoriteCount(favoriteCount)
                    .commentCount(commentCount)
                    .totalInteractions(favoriteCount + commentCount)
                    .lastActiveTime(lastActiveTime)
                    .build();
        } catch (Exception e) {
            log.error("获取用户统计失败: {} - {}", username.getValue(), e.getMessage(), e);
            return InteractionStatistics.builder()
                    .username(username.getValue())
                    .favoriteCount(0)
                    .commentCount(0)
                    .totalInteractions(0)
                    .build();
        }
    }
    
    /**
     * 获取文物交互统计 - 协调数据获取和业务处理
     */
    public IInteractionStatisticsService.RelicsInteractionStatistics getRelicsStatistics(Long relicsId) {
        try {
            long favoriteCount = userFavoriteDao.countByRelicsId(relicsId);
            long commentCount = userCommentDao.countByRelicsId(relicsId);
            LocalDateTime lastInteractionTime = calculateRelicsLastInteractionTime(relicsId);
            
            // 调用领域服务计算热度分数
            double popularityScore = statisticsService.calculatePopularityScore(favoriteCount, commentCount);
            
            return new IInteractionStatisticsService.RelicsInteractionStatistics(
                    relicsId,
                    favoriteCount,
                    commentCount,
                    favoriteCount + commentCount,
                    lastInteractionTime,
                    popularityScore
            );
        } catch (Exception e) {
            log.error("获取文物统计失败: {} - {}", relicsId, e.getMessage(), e);
            return new IInteractionStatisticsService.RelicsInteractionStatistics(relicsId, 0, 0, 0, null, 0.0);
        }
    }
    
    /**
     * 计算用户最后活跃时间 - 基础设施层协调逻辑
     */
    private LocalDateTime calculateUserLastActiveTime(Username username) {
        try {
            LocalDateTime lastFavoriteTime = null;
            LocalDateTime lastCommentTime = null;
            
            // 获取最近的收藏时间
            var recentFavorites = userFavoriteDao.selectByUsername(username.getValue(), 0, 1);
            if (!recentFavorites.isEmpty()) {
                lastFavoriteTime = recentFavorites.get(0).getCreateTime();
            }
            
            // 获取最近的评论时间
            var recentComments = userCommentDao.selectByUsername(username.getValue(), null, 0, 1);
            if (!recentComments.isEmpty()) {
                lastCommentTime = recentComments.get(0).getCreateTime();
            }
            
            // 返回最新的时间
            if (lastFavoriteTime == null && lastCommentTime == null) {
                return null;
            } else if (lastFavoriteTime == null) {
                return lastCommentTime;
            } else if (lastCommentTime == null) {
                return lastFavoriteTime;
            } else {
                return lastFavoriteTime.isAfter(lastCommentTime) ? lastFavoriteTime : lastCommentTime;
            }
            
        } catch (Exception e) {
            log.error("计算用户最后活跃时间失败: {} - {}", username.getValue(), e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 计算文物最后交互时间 - 基础设施层协调逻辑
     */
    private LocalDateTime calculateRelicsLastInteractionTime(Long relicsId) {
        try {
            LocalDateTime lastFavoriteTime = null;
            LocalDateTime lastCommentTime = null;
            
            // 获取最近的收藏时间
            var recentFavorites = userFavoriteDao.selectByRelicsId(relicsId, 0, 1);
            if (!recentFavorites.isEmpty()) {
                lastFavoriteTime = recentFavorites.get(0).getCreateTime();
            }
            
            // 获取最近的评论时间
            var recentComments = userCommentDao.selectByRelicsId(relicsId, 0, 1);
            if (!recentComments.isEmpty()) {
                lastCommentTime = recentComments.get(0).getCreateTime();
            }
            
            // 返回最新的时间
            if (lastFavoriteTime == null && lastCommentTime == null) {
                return null;
            } else if (lastFavoriteTime == null) {
                return lastCommentTime;
            } else if (lastCommentTime == null) {
                return lastFavoriteTime;
            } else {
                return lastFavoriteTime.isAfter(lastCommentTime) ? lastFavoriteTime : lastCommentTime;
            }
            
        } catch (Exception e) {
            log.error("计算文物最后交互时间失败: {} - {}", relicsId, e.getMessage(), e);
            return null;
        }
    }
}
