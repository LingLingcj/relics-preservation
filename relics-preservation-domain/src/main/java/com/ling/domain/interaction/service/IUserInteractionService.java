package com.ling.domain.interaction.service;

import com.ling.domain.interaction.model.valobj.*;

import java.util.List;

/**
 * 用户交互服务接口
 * @Author: LingRJ
 * @Description: 处理用户与文物的交互业务逻辑
 * @DateTime: 2025/7/11
 */
public interface IUserInteractionService {
    
    // ==================== 收藏相关 ====================
    
    /**
     * 添加收藏
     * @param username 用户名
     * @param relicsId 文物ID
     * @return 操作结果
     */
    InteractionResult addFavorite(String username, Long relicsId);
    
    /**
     * 取消收藏
     * @param username 用户名
     * @param relicsId 文物ID
     * @return 操作结果
     */
    InteractionResult removeFavorite(String username, Long relicsId);
    
    /**
     * 检查收藏状态
     * @param username 用户名
     * @param relicsId 文物ID
     * @return 是否已收藏
     */
    boolean isFavorited(String username, Long relicsId);
    
    /**
     * 获取用户收藏列表
     * @param username 用户名
     * @param page 页码
     * @param size 每页大小
     * @return 收藏列表结果
     */
    FavoriteListResult getUserFavorites(String username, int page, int size);
    
    // ==================== 评论相关 ====================
    
    /**
     * 添加评论
     * @param username 用户名
     * @param relicsId 文物ID
     * @param content 评论内容
     * @return 操作结果
     */
    InteractionResult addComment(String username, Long relicsId, String content);
    
    /**
     * 删除评论
     * @param username 用户名
     * @param commentId 评论ID
     * @return 操作结果
     */
    InteractionResult deleteComment(String username, Long commentId);
    
    /**
     * 获取用户评论列表
     * @param username 用户名
     * @param relicsId 文物ID（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 评论列表结果
     */
    CommentListResult getUserComments(String username, Long relicsId, int page, int size);

    /**
     * 获取文物的所有评论列表（公开接口）
     * @param relicsId 文物ID
     * @param page 页码
     * @param size 每页大小
     * @return 文物评论列表结果
     */
    RelicsCommentListResult getRelicsComments(Long relicsId, int page, int size);

    // ==================== 结果对象 ====================
    
    /**
     * 收藏列表结果
     */
    record FavoriteListResult(
            List<FavoriteAction> favorites,
            long total,
            int page,
            int size,
            boolean hasNext
    ) {}
    
    /**
     * 评论列表结果
     */
    record CommentListResult(
            List<CommentAction> comments,
            long total,
            int page,
            int size,
            boolean hasNext
    ) {}
}
