package com.ling.domain.interaction.service;

import java.time.LocalDateTime;
import java.util.List;

import com.ling.domain.interaction.model.valobj.InteractionStatistics;
import com.ling.domain.user.model.valobj.Username;

/**
 * 交互统计服务接口
 * @Author: LingRJ
 * @Description: 专门处理用户交互相关的统计查询
 * @DateTime: 2025/7/11
 */
public interface IInteractionStatisticsService {

    /**
     * 获取热门文物列表
     * @param limit 限制数量
     * @return 热门文物摘要列表
     */
    List<RelicsInteractionSummary> getPopularRelics(int limit);

    /**
     * 获取最近交互的文物列表
     * @param limit 限制数量
     * @return 最近交互文物摘要列表
     */
    List<RelicsInteractionSummary> getRecentlyInteractedRelics(int limit);

    /**
     * 获取活跃用户列表
     * @param limit 限制数量
     * @return 活跃用户名列表
     */
    List<String> getActiveUsers(int limit);

    /**
     * 获取用户交互统计
     * @param username 用户名
     * @return 用户交互统计
     */
    InteractionStatistics getUserStatistics(Username username);

    /**
     * 获取文物交互统计
     * @param relicsId 文物ID
     * @return 文物交互统计
     */
    RelicsInteractionStatistics getRelicsStatistics(Long relicsId);

    /**
     * 文物交互摘要记录
     */
    record RelicsInteractionSummary(
            Long relicsId,
            String relicsName,
            Long favoriteCount,
            Long commentCount,
            LocalDateTime lastInteractionTime
    ) {}

    /**
     * 文物交互统计记录
     */
    record RelicsInteractionStatistics(
            Long relicsId,
            long favoriteCount,
            long commentCount,
            long totalInteractions,
            LocalDateTime lastInteractionTime,
            double popularityScore
    ) {}
}
