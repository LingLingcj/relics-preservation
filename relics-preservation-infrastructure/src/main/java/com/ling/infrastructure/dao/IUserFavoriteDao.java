package com.ling.infrastructure.dao;

import com.ling.infrastructure.dao.po.UserFavorite;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户收藏DAO接口
 * @Author: LingRJ
 * @Description: 用户收藏数据访问接口
 * @DateTime: 2025/7/11
 */
@Repository
public interface IUserFavoriteDao {
    
    // ==================== 基础CRUD ====================
    
    /**
     * 插入收藏记录
     * @param userFavorite 收藏记录
     * @return 影响行数
     */
    int insert(UserFavorite userFavorite);
    
    /**
     * 根据ID查询收藏记录
     * @param id 主键ID
     * @return 收藏记录
     */
    UserFavorite selectById(Long id);
    
    /**
     * 更新收藏记录
     * @param userFavorite 收藏记录
     * @return 影响行数
     */
    int update(UserFavorite userFavorite);
    
    /**
     * 逻辑删除收藏记录
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(Long id);

    
    // ==================== 业务查询 ====================
    
    /**
     * 检查用户是否收藏了文物
     * @param username 用户名
     * @param relicsId 文物ID
     * @return 收藏记录
     */
    UserFavorite selectByUsernameAndRelicsId(@Param("username") String username, 
                                           @Param("relicsId") Long relicsId);
    
    /**
     * 获取用户收藏列表
     * @param username 用户名
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 收藏列表
     */
    List<UserFavorite> selectByUsername(@Param("username") String username, 
                                      @Param("offset") int offset, 
                                      @Param("limit") int limit);
    
    /**
     * 获取用户收藏总数
     * @param username 用户名
     * @return 收藏总数
     */
    long countByUsername(@Param("username") String username);
    
    /**
     * 获取文物收藏用户列表
     * @param relicsId 文物ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 收藏用户列表
     */
    List<UserFavorite> selectByRelicsId(@Param("relicsId") Long relicsId, 
                                      @Param("offset") int offset, 
                                      @Param("limit") int limit);
    
    /**
     * 获取文物收藏总数
     * @param relicsId 文物ID
     * @return 收藏总数
     */
    long countByRelicsId(@Param("relicsId") Long relicsId);
    
    // ==================== 批量操作 ====================
    
    /**
     * 批量插入收藏记录
     * @param userFavorites 收藏记录列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<UserFavorite> userFavorites);
    
    /**
     * 批量检查收藏状态
     * @param username 用户名
     * @param relicsIds 文物ID列表
     * @return 收藏记录列表
     */
    List<UserFavorite> batchSelectByUsernameAndRelicsIds(@Param("username") String username, 
                                                        @Param("relicsIds") List<Long> relicsIds);
    
    /**
     * 批量获取文物收藏数量
     * @param relicsIds 文物ID列表
     * @return 收藏统计列表
     */
    List<RelicsFavoriteCount> batchCountByRelicsIds(@Param("relicsIds") List<Long> relicsIds);
    
    // ==================== 统计查询 ====================
    
    /**
     * 获取热门文物（按收藏数排序）
     * @param limit 限制数量
     * @return 热门文物统计
     */
    List<RelicsFavoriteCount> selectPopularRelics(@Param("limit") int limit);
    
    /**
     * 获取最近收藏的文物
     * @param limit 限制数量
     * @return 最近收藏统计
     */
    List<RelicsFavoriteCount> selectRecentlyFavoritedRelics(@Param("limit") int limit);
    
    /**
     * 获取活跃用户（按收藏数排序）
     * @param limit 限制数量
     * @return 活跃用户统计
     */
    List<UserFavoriteCount> selectActiveUsers(@Param("limit") int limit);
    
    /**
     * 获取指定时间范围内的收藏记录
     * @param username 用户名（可选）
     * @param relicsId 文物ID（可选）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 收藏记录列表
     */
    List<UserFavorite> selectByTimeRange(@Param("username") String username,
                                       @Param("relicsId") Long relicsId,
                                       @Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime);
    
    // ==================== 结果对象 ====================
    
    /**
     * 文物收藏统计
     */
    record RelicsFavoriteCount(
            Long relicsId,
            String relicsName,
            long favoriteCount,
            LocalDateTime lastFavoriteTime
    ) {}
    
    /**
     * 用户收藏统计
     */
    record UserFavoriteCount(
            String username,
            long favoriteCount,
            LocalDateTime lastFavoriteTime
    ) {}
}
