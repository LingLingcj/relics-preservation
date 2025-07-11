package com.ling.domain.interaction.adapter;

import java.util.List;
import java.util.Optional;

import com.ling.domain.interaction.model.entity.UserInteraction;
import com.ling.domain.interaction.model.valobj.CommentAction;
import com.ling.domain.interaction.model.valobj.CommentWithUser;
import com.ling.domain.interaction.model.valobj.RelicsComment;
import com.ling.domain.user.model.valobj.Username;

/**
 * 用户交互仓储接口
 * @Author: LingRJ
 * @Description: 提供用户交互数据访问接口
 * @DateTime: 2025/7/11
 */
public interface IUserInteractionRepository {
    
    // ==================== 聚合根操作 ====================
    
    /**
     * 保存用户交互聚合根
     * @param userInteraction 用户交互聚合根
     * @return 保存结果
     */
    boolean save(UserInteraction userInteraction);
    
    /**
     * 根据用户名查找用户交互聚合根
     * @param username 用户名值对象
     * @return 用户交互聚合根
     */
    Optional<UserInteraction> findByUsername(Username username);
    
    /**
     * 删除用户交互聚合根
     * @param username 用户名值对象
     * @return 删除结果
     */
    boolean deleteByUsername(Username username);
    
    // ==================== 收藏相关查询 ====================
    
    /**
     * 检查用户是否收藏了文物
     * @param username 用户名
     * @param relicsId 文物ID
     * @return 是否收藏
     */
    boolean isFavorited(Username username, Long relicsId);
    
    /**
     * 获取用户收藏的文物ID列表
     * @param username 用户名
     * @param page 页码
     * @param size 每页大小
     * @return 文物ID列表
     */
    List<Long> getUserFavoriteRelicsIds(Username username, int page, int size);
    
    /**
     * 获取用户收藏总数
     * @param username 用户名
     * @return 收藏总数
     */
    long getUserFavoriteCount(Username username);
    
    /**
     * 获取收藏了指定文物的用户列表
     * @param relicsId 文物ID
     * @param page 页码
     * @param size 每页大小
     * @return 用户名列表
     */
    List<String> getRelicsFavoriteUsers(Long relicsId, int page, int size);
    
    /**
     * 获取文物收藏总数
     * @param relicsId 文物ID
     * @return 收藏总数
     */
    long getRelicsFavoriteCount(Long relicsId);
    
    // ==================== 评论相关查询 ====================
    
    /**
     * 获取用户评论列表
     * @param username 用户名
     * @param relicsId 文物ID（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 评论列表
     */
    List<CommentAction> getUserComments(Username username, Long relicsId, int page, int size);
    
    /**
     * 获取用户评论总数
     * @param username 用户名
     * @param relicsId 文物ID（可选）
     * @return 评论总数
     */
    long getUserCommentCount(Username username, Long relicsId);
    
    /**
     * 获取文物评论列表
     * @param relicsId 文物ID
     * @param page 页码
     * @param size 每页大小
     * @return 评论列表
     */
    List<CommentAction> getRelicsComments(Long relicsId, int page, int size);
    
    /**
     * 获取文物评论总数
     * @param relicsId 文物ID
     * @return 评论总数
     */
    long getRelicsCommentCount(Long relicsId);
    
    /**
     * 根据评论ID查找评论
     * @param commentId 评论ID
     * @return 评论行为
     */
    Optional<CommentAction> findCommentById(Long commentId);
    
    /**
     * 获取待审核评论列表
     * @param relicsId 文物ID（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 待审核评论列表
     */
    List<CommentWithUser> getPendingComments(Long relicsId, int page, int size);

    /**
     * 获取文物的已通过审核的评论列表（用于公开展示）
     * @param relicsId 文物ID
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 已通过审核的评论列表
     */
    List<RelicsComment> getApprovedCommentsByRelicsId(Long relicsId, int page, int size);

    /**
     * 统计文物的已通过审核评论总数
     * @param relicsId 文物ID
     * @return 评论总数
     */
    Long countApprovedCommentsByRelicsId(Long relicsId);
    
    // 统计查询方法已移至 IInteractionStatisticsService
    
    // 批量操作方法已移至 IInteractionBatchService
    
    // 时间范围查询方法已移至 IInteractionQueryService
    
    // 结果对象已移至对应的服务接口
}
