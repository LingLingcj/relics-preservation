package com.ling.domain.comment.service;

import com.ling.domain.comment.model.entity.CommentEntity;
import com.ling.domain.comment.model.valobj.CommentVO;

import java.util.List;
import java.util.Map;

/**
 * @Description: 文物评论服务接口
 */
public interface ICommentService {
    
    /**
     * 添加评论
     * @param commentVO 评论值对象
     * @return 评论实体
     */
    CommentEntity addComment(CommentVO commentVO);
    
    /**
     * 根据文物ID获取评论列表
     * @param relicsId 文物ID
     * @return 评论列表
     */
    List<CommentEntity> getCommentsByRelicsId(Long relicsId);
    
    /**
     * 删除评论
     * @param commentId 评论ID
     * @param username 用户名（权限校验）
     * @return 删除结果
     */
    boolean deleteComment(Long commentId, String username);
    
    /**
     * 分页查询评论
     * @param relicsId 文物ID
     * @param page 页码
     * @param size 每页大小
     * @return 包含评论列表和总数的Map
     */
    Map<String, Object> getCommentsByPage(Long relicsId, int page, int size);
} 