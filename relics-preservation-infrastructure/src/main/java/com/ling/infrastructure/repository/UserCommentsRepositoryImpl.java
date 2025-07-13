package com.ling.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ling.domain.interaction.adapter.IUserCommentsRepository;
import com.ling.domain.interaction.model.entity.UserComments;
import com.ling.domain.interaction.model.valobj.ChangeTracker;
import com.ling.domain.interaction.model.valobj.CommentAction;
import com.ling.domain.user.model.valobj.Username;
import com.ling.infrastructure.cache.service.UserCommentsCacheService;
import com.ling.infrastructure.dao.IUserCommentDao;
import com.ling.infrastructure.dao.po.UserComment;
import com.ling.infrastructure.repository.converter.UserCommentsConverter;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户评论仓储实现
 * @Author: LingRJ
 * @Description: 实现用户评论聚合根的数据访问逻辑
 * @DateTime: 2025/7/13
 */
@Repository
@Slf4j
public class UserCommentsRepositoryImpl implements IUserCommentsRepository {

    @Autowired
    private IUserCommentDao userCommentDao;

    @Autowired
    private UserCommentsConverter converter;

    @Autowired
    private UserCommentsCacheService cacheService;

    @Override
    public Optional<UserComments> findByUsername(Username username) {
        try {
            log.debug("查找用户评论聚合根: {}", username.getValue());

            // 先尝试从缓存获取
            Optional<UserComments> cachedResult = cacheService.getUserComments(username);
            if (cachedResult.isPresent()) {
                log.debug("缓存命中 - 用户评论: {}", username.getValue());
                return cachedResult;
            }

            // 缓存未命中，从数据库查询
            log.debug("缓存未命中，从数据库查询用户评论: {}", username.getValue());
            List<UserComment> comments = userCommentDao.selectByUsername(username.getValue(), null, 0, Integer.MAX_VALUE);

            if (comments.isEmpty()) {
                log.debug("用户评论记录不存在: {}", username.getValue());
                return Optional.empty();
            }

            // 转换为聚合根
            UserComments userComments = converter.buildUserComments(username, comments);

            // 缓存结果
            cacheService.cacheUserComments(userComments);
            log.debug("缓存用户评论: {} - 评论数: {}", username.getValue(), comments.size());

            return Optional.of(userComments);

        } catch (Exception e) {
            log.error("查找用户评论聚合根失败: {} - {}", username.getValue(), e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public boolean saveIncremental(UserComments userComments) {
        try {
            if (!userComments.hasChanges()) {
                log.debug("用户评论无变更，跳过保存: {}", userComments.getUsername().getValue());
                return true;
            }

            log.info("增量保存用户评论: {} - {}", 
                    userComments.getUsername().getValue(), userComments.getChangesSummary());

            boolean success = true;

            // 处理评论变更
            Set<ChangeTracker.ChangeRecord> commentChanges = userComments.getCommentChanges();
            for (ChangeTracker.ChangeRecord change : commentChanges) {
                success &= processCommentChange(userComments.getUsername(), change);
            }

            if (success) {
                // 清空变更记录
                userComments.clearChanges();
                
                // 更新缓存
                cacheService.cacheUserComments(userComments);
                
                // 清除相关缓存
                cacheService.evictRelatedCaches(userComments.getUsername());
                
                log.info("用户评论增量保存成功: {}", userComments.getUsername().getValue());
            } else {
                log.error("用户评论增量保存失败: {}", userComments.getUsername().getValue());
            }

            return success;

        } catch (Exception e) {
            log.error("用户评论增量保存异常: {} - {}", 
                    userComments.getUsername().getValue(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean save(UserComments userComments) {
        try {
            log.info("全量保存用户评论: {} - 评论数: {}", 
                    userComments.getUsername().getValue(), userComments.getComments(null).size());

            // 先删除现有记录
            deleteByUsername(userComments.getUsername());

            // 批量插入新记录
            List<UserComment> comments = converter.convertToUserComments(userComments);
            if (!comments.isEmpty()) {
                int insertCount = userCommentDao.batchInsert(comments);
                if (insertCount != comments.size()) {
                    log.error("批量插入评论记录数量不匹配: 期望={}, 实际={}", comments.size(), insertCount);
                    return false;
                }
            }

            // 清空变更记录
            userComments.clearChanges();
            
            // 更新缓存
            cacheService.cacheUserComments(userComments);
            
            // 清除相关缓存
            cacheService.evictRelatedCaches(userComments.getUsername());

            log.info("用户评论全量保存成功: {}", userComments.getUsername().getValue());
            return true;

        } catch (Exception e) {
            log.error("用户评论全量保存失败: {} - {}", 
                    userComments.getUsername().getValue(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteByUsername(Username username) {
        try {
            log.info("删除用户评论: {}", username.getValue());

            // 逻辑删除所有评论记录
            List<UserComment> comments = userCommentDao.selectByUsername(username.getValue(), null, 0, Integer.MAX_VALUE);
            for (UserComment comment : comments) {
                if (comment.getStatus() == 0) { // 只删除正常状态的记录
                    comment.setStatus(1);
                    comment.setUpdateTime(LocalDateTime.now());
                    userCommentDao.update(comment);
                }
            }

            // 清除缓存
            cacheService.evictUserComments(username);
            cacheService.evictRelatedCaches(username);

            log.info("删除用户评论成功: {}", username.getValue());
            return true;

        } catch (Exception e) {
            log.error("删除用户评论失败: {} - {}", username.getValue(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean existsByUsername(Username username) {
        try {
            long count = userCommentDao.countByUsername(username.getValue(), null);
            return count > 0;
        } catch (Exception e) {
            log.error("检查用户评论存在性失败: {} - {}", username.getValue(), e.getMessage(), e);
            return false;
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 处理评论变更
     */
    private boolean processCommentChange(Username username, ChangeTracker.ChangeRecord change) {
        try {
            CommentAction commentAction = (CommentAction) change.getEntity();
            Long commentId = (Long) change.getEntityId();

            switch (change.getType()) {
                case ADDED:
                    return addCommentRecord(username, commentAction);
                case DELETED:
                    return deleteCommentRecord(username, commentId);
                case MODIFIED:
                    return updateCommentRecord(username, commentAction);
                default:
                    log.warn("未知的变更类型: {}", change.getType());
                    return false;
            }
        } catch (Exception e) {
            log.error("处理评论变更失败: {} - {}", username.getValue(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 添加评论记录
     */
    private boolean addCommentRecord(Username username, CommentAction commentAction) {
        UserComment userComment = converter.convertToUserComment(username, commentAction);
        int result = userCommentDao.insert(userComment);
        
        if (result > 0) {
            // 清除相关缓存
            cacheService.evictRelicsCommentCaches(commentAction.getRelicsId());
            log.debug("添加评论记录成功: {} - {}", username.getValue(), commentAction.getId());
            return true;
        } else {
            log.error("添加评论记录失败: {} - {}", username.getValue(), commentAction.getId());
            return false;
        }
    }

    /**
     * 删除评论记录
     */
    private boolean deleteCommentRecord(Username username, Long commentId) {
        UserComment existing = userCommentDao.selectByCommentId(commentId);
        if (existing != null && existing.getStatus() == 0) {
            existing.setStatus(1);
            existing.setUpdateTime(LocalDateTime.now());
            int result = userCommentDao.update(existing);
            
            if (result > 0) {
                // 清除相关缓存
                cacheService.evictRelicsCommentCaches(existing.getRelicsId());
                log.debug("删除评论记录成功: {} - {}", username.getValue(), commentId);
                return true;
            }
        }
        
        log.error("删除评论记录失败: {} - {}", username.getValue(), commentId);
        return false;
    }

    /**
     * 更新评论记录
     */
    private boolean updateCommentRecord(Username username, CommentAction commentAction) {
        UserComment existing = userCommentDao.selectByCommentId(commentAction.getId());
        if (existing != null) {
            existing.setUpdateTime(LocalDateTime.now());
            existing.setStatus(commentAction.isDeleted() ? 1 : 0);
            existing.setCommentStatus(commentAction.getStatus().getCode());
            int result = userCommentDao.update(existing);
            
            if (result > 0) {
                // 清除相关缓存
                cacheService.evictRelicsCommentCaches(commentAction.getRelicsId());
                log.debug("更新评论记录成功: {} - {}", username.getValue(), commentAction.getId());
                return true;
            }
        }
        
        log.error("更新评论记录失败: {} - {}", username.getValue(), commentAction.getId());
        return false;
    }
}
