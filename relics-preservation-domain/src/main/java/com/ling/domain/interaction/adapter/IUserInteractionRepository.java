package com.ling.domain.interaction.adapter;

import java.util.List;
import java.util.Optional;

import com.ling.domain.interaction.model.valobj.CommentAction;
import com.ling.domain.interaction.model.valobj.CommentStatus;
import com.ling.domain.interaction.model.valobj.CommentWithUser;
import com.ling.domain.interaction.model.valobj.RelicsComment;

/**
 * 用户交互仓储接口
 * @Author: LingRJ
 * @Description: 提供用户交互数据访问接口
 * @DateTime: 2025/7/11
 */
public interface IUserInteractionRepository {

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

    boolean updateCommentStatus(Long commentId, CommentStatus status);

}
