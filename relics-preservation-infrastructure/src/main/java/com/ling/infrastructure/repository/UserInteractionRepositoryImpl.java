package com.ling.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ling.domain.interaction.adapter.IUserInteractionRepository;
import com.ling.domain.interaction.model.valobj.CommentAction;
import com.ling.domain.interaction.model.valobj.CommentContent;
import com.ling.domain.interaction.model.valobj.CommentStatus;
import com.ling.domain.interaction.model.valobj.CommentWithUser;
import com.ling.domain.interaction.model.valobj.RelicsComment;
import com.ling.infrastructure.dao.IUserCommentDao;
import com.ling.infrastructure.dao.IUserFavoriteDao;
import com.ling.infrastructure.dao.po.UserComment;
import com.ling.infrastructure.repository.converter.UserInteractionConverter;
import com.ling.infrastructure.cache.service.UserInteractionCacheService;

import lombok.extern.slf4j.Slf4j;

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

    @Autowired
    private UserInteractionConverter converter;

    @Autowired
    private UserInteractionCacheService cacheService;
    
    // ==================== 聚合根操作 ====================

    
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

    @Override
    public List<RelicsComment> getApprovedCommentsByRelicsId(Long relicsId, int page, int size) {
        try {
            // 先尝试从缓存获取
            Optional<List<RelicsComment>> cachedComments = cacheService.getRelicsApprovedComments(relicsId, page, size);
            if (cachedComments.isPresent()) {
                log.debug("缓存命中 - 文物已审核评论列表: {} - page:{}, size:{}, count:{}",
                         relicsId, page, size, cachedComments.get().size());
                return cachedComments.get();
            }

            // 缓存未命中，从数据库查询
            log.debug("缓存未命中，从数据库查询文物已审核评论列表: {} - page:{}, size:{}",
                     relicsId, page, size);
            int offset = (page - 1) * size;
            // 查询已通过审核的评论（comment_status = 1）
            List<UserComment> comments = userCommentDao.selectApprovedCommentsByRelicsId(
                    relicsId, offset, size);
            List<RelicsComment> relicsComments = comments.stream()
                    .map(this::convertToRelicsComment)
                    .collect(Collectors.toList());

            // 缓存结果
            cacheService.cacheRelicsApprovedComments(relicsId, page, size, relicsComments);
            log.debug("缓存文物已审核评论列表: {} - page:{}, size:{}, count:{}",
                     relicsId, page, size, relicsComments.size());

            return relicsComments;
        } catch (Exception e) {
            log.error("获取文物已通过审核评论失败: relicsId={}, page={}, size={} - {}",
                     relicsId, page, size, e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public Long countApprovedCommentsByRelicsId(Long relicsId) {
        try {
            // 统计已通过审核的评论数量
            return userCommentDao.countApprovedCommentsByRelicsId(relicsId);
        } catch (Exception e) {
            log.error("统计文物已通过审核评论数量失败: relicsId={} - {}", relicsId, e.getMessage(), e);
            return 0L;
        }
    }

    @Override
    public boolean updateCommentStatus(Long commentId, CommentStatus status) {
        return userCommentDao.updateStatusByCommentId(commentId, status.getCode());
    }

    // 统计查询、批量操作、时间范围查询方法已移至对应的应用服务
    
    // ==================== 私有辅助方法 ====================

    
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
     * 转换为文物评论值对象（用于公开展示）
     */
    private RelicsComment convertToRelicsComment(UserComment comment) {
        return RelicsComment.builder()
                .commentId(comment.getCommentId())
                .relicsId(comment.getRelicsId())
                .username(comment.getUsername())
                .content(comment.getContent())
                .createTime(comment.getCreateTime())
                .likeCount(0) // 预留字段，暂时设为0
                .featured(false) // 预留字段，暂时设为false
                .build();
    }




}
