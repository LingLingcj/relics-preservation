package com.ling.infrastructure.dao;

import com.ling.infrastructure.dao.po.UserComment;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户评论DAO接口
 * @Author: LingRJ
 * @Description: 用户评论数据访问接口
 * @DateTime: 2025/7/11
 */
@Repository
public interface IUserCommentDao {
    
    // ==================== 基础CRUD ====================
    
    /**
     * 插入评论记录
     * @param userComment 评论记录
     * @return 影响行数
     */
    int insert(UserComment userComment);
    
    /**
     * 根据ID查询评论记录
     * @param id 主键ID
     * @return 评论记录
     */
    UserComment selectById(Long id);
    
    /**
     * 根据评论业务ID查询评论记录
     * @param commentId 评论业务ID
     * @return 评论记录
     */
    UserComment selectByCommentId(Long commentId);
    
    /**
     * 更新评论记录
     * @param userComment 评论记录
     * @return 影响行数
     */
    int update(UserComment userComment);
    
    /**
     * 逻辑删除评论记录
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(Long id);
    
    /**
     * 根据评论业务ID逻辑删除
     * @param commentId 评论业务ID
     * @return 影响行数
     */
    int deleteByCommentId(Long commentId);

    /**
     * 根据评论业务Id更新状态
     * @param commentId
     * @param Status
     * @return
     */
    boolean updateStatusByCommentId(@Param("commentId")Long commentId, @Param("commentStatus") Integer Status);
    
    // ==================== 业务查询 ====================
    
    /**
     * 获取用户评论列表
     * @param username 用户名
     * @param relicsId 文物ID（可选）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 评论列表
     */
    List<UserComment> selectByUsername(@Param("username") String username, 
                                     @Param("relicsId") Long relicsId,
                                     @Param("offset") int offset, 
                                     @Param("limit") int limit);
    
    /**
     * 获取用户评论总数
     * @param username 用户名
     * @param relicsId 文物ID（可选）
     * @return 评论总数
     */
    long countByUsername(@Param("username") String username, 
                        @Param("relicsId") Long relicsId);
    
    /**
     * 获取文物评论列表
     * @param relicsId 文物ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 评论列表
     */
    List<UserComment> selectByRelicsId(@Param("relicsId") Long relicsId, 
                                     @Param("offset") int offset, 
                                     @Param("limit") int limit);
    
    /**
     * 获取文物评论总数
     * @param relicsId 文物ID
     * @return 评论总数
     */
    long countByRelicsId(@Param("relicsId") Long relicsId);
    
    /**
     * 获取待审核评论列表
     * @param relicsId 文物ID（可选）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 待审核评论列表
     */
    List<UserComment> selectPendingComments(@Param("relicsId") Long relicsId,
                                          @Param("offset") int offset, 
                                          @Param("limit") int limit);
    
    /**
     * 获取待审核评论总数
     * @param relicsId 文物ID（可选）
     * @return 待审核评论总数
     */
    long countPendingComments(@Param("relicsId") Long relicsId);
    
    // ==================== 批量操作 ====================
    
    /**
     * 批量插入评论记录
     * @param userComments 评论记录列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<UserComment> userComments);
    
    /**
     * 批量获取文物评论数量
     * @param relicsIds 文物ID列表
     * @return 评论统计列表
     */
    List<RelicsCommentCount> batchCountByRelicsIds(@Param("relicsIds") List<Long> relicsIds);
    
    /**
     * 批量更新评论状态
     * @param commentIds 评论ID列表
     * @param commentStatus 新状态
     * @return 影响行数
     */
    int batchUpdateCommentStatus(@Param("commentIds") List<Long> commentIds, 
                               @Param("commentStatus") Integer commentStatus);
    
    // ==================== 统计查询 ====================
    
    /**
     * 获取热门文物（按评论数排序）
     * @param limit 限制数量
     * @return 热门文物统计
     */
    List<RelicsCommentCount> selectPopularRelicsByComments(@Param("limit") int limit);
    
    /**
     * 获取最近有评论的文物
     * @param limit 限制数量
     * @return 最近评论统计
     */
    List<RelicsCommentCount> selectRecentlyCommentedRelics(@Param("limit") int limit);
    
    /**
     * 获取活跃用户（按评论数排序）
     * @param limit 限制数量
     * @return 活跃用户统计
     */
    List<UserCommentCount> selectActiveCommenters(@Param("limit") int limit);
    
    /**
     * 获取指定时间范围内的评论记录
     * @param username 用户名（可选）
     * @param relicsId 文物ID（可选）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 评论记录列表
     */
    List<UserComment> selectByTimeRange(@Param("username") String username,
                                      @Param("relicsId") Long relicsId,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 查询文物的已通过审核评论列表（用于公开展示）
     * @param relicsId 文物ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 已通过审核的评论列表
     */
    List<UserComment> selectApprovedCommentsByRelicsId(@Param("relicsId") Long relicsId,
                                                      @Param("offset") int offset,
                                                      @Param("limit") int limit);

    /**
     * 统计文物的已通过审核评论数量
     * @param relicsId 文物ID
     * @return 评论数量
     */
    Long countApprovedCommentsByRelicsId(@Param("relicsId") Long relicsId);

    // ==================== 结果对象 ====================
    
    /**
     * 文物评论统计
     */
    record RelicsCommentCount(
            Long relicsId,
            String relicsName,
            long commentCount,
            LocalDateTime lastCommentTime
    ) {}
    
    /**
     * 用户评论统计
     */
    record UserCommentCount(
            String username,
            long commentCount,
            LocalDateTime lastCommentTime
    ) {}
}
