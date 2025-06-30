package com.ling.domain.comment.adapter;

import com.ling.domain.comment.model.entity.CommentEntity;

import java.util.List;

/**
 * @Description: 评论仓储接口
 */
public interface ICommentRepository {
    
    /**
     * 保存评论
     * @param commentEntity 评论实体
     * @return 保存结果
     */
    CommentEntity saveComment(CommentEntity commentEntity);
    
    /**
     * 根据文物ID查询评论列表
     * @param relicsId 文物ID
     * @return 评论列表
     */
    List<CommentEntity> getCommentsByRelicsId(Long relicsId);
    
    /**
     * 根据评论ID删除评论
     * @param commentId 评论ID
     * @return 删除结果（影响行数）
     */
    int deleteComment(Long commentId);
    
    /**
     * 根据评论ID查询评论
     * @param commentId 评论ID
     * @return 评论实体
     */
    CommentEntity getCommentById(Long commentId);
    
    /**
     * 分页查询评论
     * @param relicsId 文物ID
     * @param page 页码
     * @param size 每页大小
     * @return 评论列表
     */
    List<CommentEntity> getCommentsByPage(Long relicsId, int page, int size);
    
    /**
     * 统计文物评论数量
     * @param relicsId 文物ID
     * @return 评论数量
     */
    int countCommentsByRelicsId(Long relicsId);
} 