package com.ling.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ling.domain.interaction.adapter.IUserInteractionRepository;
import com.ling.domain.interaction.model.entity.UserInteraction;
import com.ling.domain.interaction.model.valobj.CommentAction;
import com.ling.domain.interaction.model.valobj.CommentContent;
import com.ling.domain.interaction.model.valobj.CommentStatus;
import com.ling.domain.interaction.model.valobj.CommentWithUser;
import com.ling.domain.interaction.model.valobj.FavoriteAction;
import com.ling.domain.interaction.model.valobj.RelicsComment;
import com.ling.domain.user.model.valobj.Username;
import com.ling.infrastructure.dao.IUserCommentDao;
import com.ling.infrastructure.dao.IUserFavoriteDao;
import com.ling.infrastructure.dao.po.UserComment;
import com.ling.infrastructure.dao.po.UserFavorite;
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
    public Optional<UserInteraction> findByUsername(Username username) {
        try {
            log.debug("查找用户交互: {}", username.getValue());

            // 先尝试从缓存获取
            Optional<UserInteraction> cachedResult = cacheService.getUserInteraction(username);
            if (cachedResult.isPresent()) {
                log.debug("缓存命中 - 用户交互: {}", username.getValue());
                return cachedResult;
            }

            // 缓存未命中，从数据库查询
            log.debug("缓存未命中，从数据库查询用户交互: {}", username.getValue());

            // 查询收藏数据
            List<UserFavorite> favorites = userFavoriteDao.selectByUsername(
                    username.getValue(), 0, Integer.MAX_VALUE);

            // 查询评论数据
            List<UserComment> comments = userCommentDao.selectByUsername(
                    username.getValue(), null, 0, Integer.MAX_VALUE);

            if (favorites.isEmpty() && comments.isEmpty()) {
                // 缓存空结果，防止缓存穿透
                log.debug("用户无交互数据，缓存空结果: {}", username.getValue());
                return Optional.empty();
            }

            // 构建聚合根
            UserInteraction userInteraction = buildUserInteraction(username, favorites, comments);

            // 缓存结果
            cacheService.cacheUserInteraction(userInteraction);
            log.debug("缓存用户交互数据: {}", username.getValue());

            return Optional.of(userInteraction);

        } catch (Exception e) {
            log.error("查找用户交互失败: {} - {}", username.getValue(), e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    @Override
    @Transactional
    public boolean deleteByUsername(Username username) {
        try {
            log.info("删除用户交互: {}", username.getValue());
            
            // 逻辑删除收藏记录
            List<UserFavorite> favorites = userFavoriteDao.selectByUsername(
                    username.getValue(), 0, Integer.MAX_VALUE);
            for (UserFavorite favorite : favorites) {
                userFavoriteDao.deleteById(favorite.getId());
            }
            
            // 逻辑删除评论记录
            List<UserComment> comments = userCommentDao.selectByUsername(
                    username.getValue(), null, 0, Integer.MAX_VALUE);
            for (UserComment comment : comments) {
                userCommentDao.deleteById(comment.getId());
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("删除用户交互失败: {} - {}", username.getValue(), e.getMessage(), e);
            return false;
        }
    }
    
    // ==================== 收藏相关查询 ====================
    
    @Override
    public boolean isFavorited(Username username, Long relicsId) {
        try {
            // 先尝试从缓存获取
            Optional<Boolean> cachedStatus = cacheService.getFavoriteStatus(username, relicsId);
            if (cachedStatus.isPresent()) {
                log.debug("缓存命中 - 收藏状态: {} - {}: {}",
                         username.getValue(), relicsId, cachedStatus.get());
                return cachedStatus.get();
            }

            // 缓存未命中，从数据库查询
            log.debug("缓存未命中，从数据库查询收藏状态: {} - {}", username.getValue(), relicsId);
            UserFavorite favorite = userFavoriteDao.selectByUsernameAndRelicsId(
                    username.getValue(), relicsId);
            boolean isFavorited = favorite != null && favorite.getStatus() == 0;

            // 缓存结果
            cacheService.cacheFavoriteStatus(username, relicsId, isFavorited);
            log.debug("缓存收藏状态: {} - {}: {}", username.getValue(), relicsId, isFavorited);

            return isFavorited;
        } catch (Exception e) {
            log.error("检查收藏状态失败: {} - {}", username.getValue(), e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public List<Long> getUserFavoriteRelicsIds(Username username, int page, int size) {
        try {
            // 先尝试从缓存获取
            Optional<List<Long>> cachedList = cacheService.getUserFavoritesList(username, page, size);
            if (cachedList.isPresent()) {
                log.debug("缓存命中 - 用户收藏列表: {} - page:{}, size:{}, count:{}",
                         username.getValue(), page, size, cachedList.get().size());
                return cachedList.get();
            }

            // 缓存未命中，从数据库查询
            log.debug("缓存未命中，从数据库查询用户收藏列表: {} - page:{}, size:{}",
                     username.getValue(), page, size);
            int offset = (page - 1) * size;
            List<UserFavorite> favorites = userFavoriteDao.selectByUsername(
                    username.getValue(), offset, size);
            List<Long> favoriteIds = favorites.stream()
                    .map(UserFavorite::getRelicsId)
                    .collect(Collectors.toList());

            // 缓存结果
            cacheService.cacheUserFavoritesList(username, page, size, favoriteIds);
            log.debug("缓存用户收藏列表: {} - page:{}, size:{}, count:{}",
                     username.getValue(), page, size, favoriteIds.size());

            return favoriteIds;
        } catch (Exception e) {
            log.error("获取用户收藏列表失败: {} - {}", username.getValue(), e.getMessage(), e);
            return List.of();
        }
    }
    
    @Override
    public long getUserFavoriteCount(Username username) {
        try {
            // 先尝试从缓存获取
            Optional<Long> cachedCount = cacheService.getUserFavoriteCount(username);
            if (cachedCount.isPresent()) {
                log.debug("缓存命中 - 用户收藏总数: {} - {}", username.getValue(), cachedCount.get());
                return cachedCount.get();
            }

            // 缓存未命中，从数据库查询
            log.debug("缓存未命中，从数据库查询用户收藏总数: {}", username.getValue());
            long count = userFavoriteDao.countByUsername(username.getValue());

            // 缓存结果
            cacheService.cacheUserFavoriteCount(username, count);
            log.debug("缓存用户收藏总数: {} - {}", username.getValue(), count);

            return count;
        } catch (Exception e) {
            log.error("获取用户收藏总数失败: {} - {}", username.getValue(), e.getMessage(), e);
            return 0;
        }
    }
    
    @Override
    public List<String> getRelicsFavoriteUsers(Long relicsId, int page, int size) {
        try {
            int offset = (page - 1) * size;
            List<UserFavorite> favorites = userFavoriteDao.selectByRelicsId(
                    relicsId, offset, size);
            return favorites.stream()
                    .map(UserFavorite::getUsername)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取文物收藏用户失败: {} - {}", relicsId, e.getMessage(), e);
            return List.of();
        }
    }
    
    @Override
    public long getRelicsFavoriteCount(Long relicsId) {
        try {
            // 先尝试从缓存获取
            Optional<Long> cachedCount = cacheService.getRelicsFavoriteCount(relicsId);
            if (cachedCount.isPresent()) {
                log.debug("缓存命中 - 文物收藏总数: {} - {}", relicsId, cachedCount.get());
                return cachedCount.get();
            }

            // 缓存未命中，从数据库查询
            log.debug("缓存未命中，从数据库查询文物收藏总数: {}", relicsId);
            long count = userFavoriteDao.countByRelicsId(relicsId);

            // 缓存结果
            cacheService.cacheRelicsFavoriteCount(relicsId, count);
            log.debug("缓存文物收藏总数: {} - {}", relicsId, count);

            return count;
        } catch (Exception e) {
            log.error("获取文物收藏总数失败: {} - {}", relicsId, e.getMessage(), e);
            return 0;
        }
    }
    
    // ==================== 评论相关查询 ====================
    
    @Override
    public List<CommentAction> getUserComments(Username username, Long relicsId, int page, int size) {
        try {
            int offset = (page - 1) * size;
            List<UserComment> comments = userCommentDao.selectByUsername(
                    username.getValue(), relicsId, offset, size);
            return comments.stream()
                    .map(this::convertToCommentAction)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取用户评论失败: {} - {}", username.getValue(), e.getMessage(), e);
            return List.of();
        }
    }
    
    @Override
    public long getUserCommentCount(Username username, Long relicsId) {
        try {
            return userCommentDao.countByUsername(username.getValue(), relicsId);
        } catch (Exception e) {
            log.error("获取用户评论总数失败: {} - {}", username.getValue(), e.getMessage(), e);
            return 0;
        }
    }
    
    @Override
    public List<CommentAction> getRelicsComments(Long relicsId, int page, int size) {
        try {
            int offset = (page - 1) * size;
            List<UserComment> comments = userCommentDao.selectByRelicsId(
                    relicsId, offset, size);
            return comments.stream()
                    .map(this::convertToCommentAction)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取文物评论失败: {} - {}", relicsId, e.getMessage(), e);
            return List.of();
        }
    }
    
    @Override
    public long getRelicsCommentCount(Long relicsId) {
        try {
            // 先尝试从缓存获取
            Optional<Long> cachedCount = cacheService.getRelicsCommentCount(relicsId);
            if (cachedCount.isPresent()) {
                log.debug("缓存命中 - 文物评论总数: {} - {}", relicsId, cachedCount.get());
                return cachedCount.get();
            }

            // 缓存未命中，从数据库查询
            log.debug("缓存未命中，从数据库查询文物评论总数: {}", relicsId);
            long count = userCommentDao.countByRelicsId(relicsId);

            // 缓存结果
            cacheService.cacheRelicsCommentCount(relicsId, count);
            log.debug("缓存文物评论总数: {} - {}", relicsId, count);

            return count;
        } catch (Exception e) {
            log.error("获取文物评论总数失败: {} - {}", relicsId, e.getMessage(), e);
            return 0;
        }
    }
    
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
     * 构建用户交互聚合根
     */
    private UserInteraction buildUserInteraction(Username username,
                                               List<UserFavorite> favorites,
                                               List<UserComment> comments) {
        return converter.buildUserInteraction(username, favorites, comments);
    }
    
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



    /**
     * 转换评论状态
     */
    private int convertCommentStatus(CommentStatus status) {
        if (status == null) {
            return 0; // 默认待审核
        }

        return switch (status) {
            case PENDING_REVIEW -> 0;  // 待审核
            case APPROVED -> 1;        // 已通过
            case REJECTED -> 2;        // 已拒绝
        };
    }

    // ==================== 增量保存实现 ====================

    /**
     * 保存收藏变更
     */
    private void saveFavoriteChanges(UserInteraction userInteraction) {
        try {
            var favoriteChanges = userInteraction.getFavoriteChanges();

            for (var change : favoriteChanges) {
                FavoriteAction favorite = (FavoriteAction) change.getEntity();
                String username = userInteraction.getUsername().getValue();

                switch (change.getType()) {
                    case ADDED -> {
                        UserFavorite existing = userFavoriteDao.selectByUsernameAndRelicsId(username, favorite.getRelicsId());
                        if (existing != null) {
                            userFavoriteDao.update(existing);
                        } else {
                            UserFavorite newFavorite = new UserFavorite();
                            newFavorite.setUsername(username);
                            newFavorite.setRelicsId(favorite.getRelicsId());
                            newFavorite.setStatus(0);
                            newFavorite.setCreateTime(favorite.getCreateTime());
                            newFavorite.setUpdateTime(LocalDateTime.now());
                            userFavoriteDao.insert(newFavorite);
                        }
                        log.debug("增量新增收藏: {} - {}", username, favorite.getRelicsId());
                    }
                    case DELETED -> {
                        UserFavorite existing = userFavoriteDao.selectByUsernameAndRelicsId(
                                username, favorite.getRelicsId());
                        if (existing != null && existing.getStatus() == 0) {
                            existing.setStatus(1);
                            existing.setUpdateTime(LocalDateTime.now());
                            userFavoriteDao.update(existing);
                            log.debug("增量删除收藏: {} - {}", username, favorite.getRelicsId());
                        }
                    }
                    case MODIFIED -> {
                        // 收藏一般不会有修改操作，如果有可以在这里处理
                        log.debug("收藏修改操作: {} - {}", username, favorite.getRelicsId());
                    }
                }
            }
        } catch (Exception e) {
            log.error("保存收藏变更失败: {} - {}", userInteraction.getUsername().getValue(), e.getMessage(), e);
            throw new RuntimeException("保存收藏变更失败", e);
        }
    }

    /**
     * 保存评论变更
     */
    private void saveCommentChanges(UserInteraction userInteraction) {
        try {
            var commentChanges = userInteraction.getCommentChanges();

            for (var change : commentChanges) {
                CommentAction comment = (CommentAction) change.getEntity();
                String username = userInteraction.getUsername().getValue();

                switch (change.getType()) {
                    case ADDED -> {
                        UserComment newComment = new UserComment();
                        newComment.setCommentId(comment.getId());
                        newComment.setUsername(username);
                        newComment.setRelicsId(comment.getRelicsId());
                        newComment.setContent(comment.getContent().getContent());
                        newComment.setCommentStatus(convertCommentStatus(comment.getStatus()));
                        newComment.setStatus(0);
                        newComment.setCreateTime(comment.getCreateTime());
                        newComment.setUpdateTime(LocalDateTime.now());
                        userCommentDao.insert(newComment);
                        log.debug("增量新增评论: {} - {}", username, comment.getId());
                    }
                    case DELETED -> {
                        UserComment existing = userCommentDao.selectByCommentId(comment.getId());
                        if (existing != null && existing.getStatus() == 0) {
                            existing.setStatus(1);
                            existing.setUpdateTime(LocalDateTime.now());
                            userCommentDao.update(existing);
                            log.debug("增量删除评论: {} - {}", username, comment.getId());
                        }
                    }
                    case MODIFIED -> {
                        UserComment existing = userCommentDao.selectByCommentId(comment.getId());
                        if (existing != null) {
                            existing.setContent(comment.getContent().getContent());
                            existing.setCommentStatus(convertCommentStatus(comment.getStatus()));
                            existing.setUpdateTime(LocalDateTime.now());
                            userCommentDao.update(existing);
                            log.debug("增量修改评论: {} - {}", username, comment.getId());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("保存评论变更失败: {} - {}", userInteraction.getUsername().getValue(), e.getMessage(), e);
            throw new RuntimeException("保存评论变更失败", e);
        }
    }

    @Override
    @Transactional
    public boolean saveIncremental(UserInteraction userInteraction) {
        try {
            log.debug("增量保存用户交互: {}", userInteraction.getDisplayName());

            if (!userInteraction.hasChanges()) {
                log.debug("用户交互无变更，跳过保存: {}", userInteraction.getDisplayName());
                return true;
            }

            // 保存收藏变更
            if (userInteraction.hasFavoriteChanges()) {
                saveFavoriteChanges(userInteraction);
                // 清除相关缓存
                evictFavoriteRelatedCaches(userInteraction);
            }

            // 保存评论变更
            if (userInteraction.hasCommentChanges()) {
                saveCommentChanges(userInteraction);
                // 清除相关缓存
                evictCommentRelatedCaches(userInteraction);
            }

            // 清空变更记录
            userInteraction.clearChanges();

            // 更新聚合根缓存
            cacheService.cacheUserInteraction(userInteraction);

            log.info("增量保存完成: {} - {}", userInteraction.getDisplayName(),
                    userInteraction.getChangesSummary());
            return true;

        } catch (Exception e) {
            log.error("增量保存用户交互失败: {} - {}", userInteraction.getDisplayName(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean saveFavorite(Username username, FavoriteAction favoriteAction) {
        try {
            log.debug("保存单个收藏: {} - {}", username.getValue(), favoriteAction.getRelicsId());

            UserFavorite userFavorite = new UserFavorite();
            userFavorite.setUsername(username.getValue());
            userFavorite.setRelicsId(favoriteAction.getRelicsId());
            userFavorite.setStatus(favoriteAction.isDeleted() ? 1 : 0);
            userFavorite.setCreateTime(favoriteAction.getCreateTime());
            userFavorite.setUpdateTime(LocalDateTime.now());

            int result = userFavoriteDao.insert(userFavorite);
            return result > 0;

        } catch (Exception e) {
            log.error("保存单个收藏失败: {} - {} - {}", username.getValue(),
                    favoriteAction.getRelicsId(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean saveComment(Username username, CommentAction commentAction) {
        try {
            log.debug("保存单个评论: {} - {}", username.getValue(), commentAction.getId());

            UserComment userComment = new UserComment();
            userComment.setCommentId(commentAction.getId());
            userComment.setUsername(username.getValue());
            userComment.setRelicsId(commentAction.getRelicsId());
            userComment.setContent(commentAction.getContent().getContent());
            userComment.setCommentStatus(convertCommentStatus(commentAction.getStatus()));
            userComment.setStatus(commentAction.isDeleted() ? 1 : 0);
            userComment.setCreateTime(commentAction.getCreateTime());
            userComment.setUpdateTime(LocalDateTime.now());

            int result = userCommentDao.insert(userComment);
            return result > 0;

        } catch (Exception e) {
            log.error("保存单个评论失败: {} - {} - {}", username.getValue(),
                    commentAction.getId(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteFavorite(Username username, Long relicsId) {
        try {
            log.debug("删除单个收藏: {} - {}", username.getValue(), relicsId);

            UserFavorite existing = userFavoriteDao.selectByUsernameAndRelicsId(
                    username.getValue(), relicsId);

            if (existing != null && existing.getStatus() == 0) {
                existing.setStatus(1); // 逻辑删除
                existing.setUpdateTime(LocalDateTime.now());
                int result = userFavoriteDao.update(existing);
                return result > 0;
            }

            return true; // 已经删除或不存在

        } catch (Exception e) {
            log.error("删除单个收藏失败: {} - {} - {}", username.getValue(), relicsId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteComment(Username username, Long commentId) {
        try {
            log.debug("删除单个评论: {} - {}", username.getValue(), commentId);

            UserComment existing = userCommentDao.selectByCommentId(commentId);

            if (existing != null && existing.getUsername().equals(username.getValue())
                    && existing.getStatus() == 0) {
                existing.setStatus(1); // 逻辑删除
                existing.setUpdateTime(LocalDateTime.now());
                int result = userCommentDao.update(existing);
                return result > 0;
            }

            return true; // 已经删除或不存在

        } catch (Exception e) {
            log.error("删除单个评论失败: {} - {} - {}", username.getValue(), commentId, e.getMessage(), e);
            return false;
        }
    }

    // ==================== 缓存清理辅助方法 ====================

    /**
     * 清除收藏相关缓存
     */
    private void evictFavoriteRelatedCaches(UserInteraction userInteraction) {
        try {
            Username username = userInteraction.getUsername();

            // 清除用户交互聚合根缓存
            cacheService.evictUserInteraction(username);

            // 清除用户收藏列表缓存
            cacheService.evictUserFavoritesList(username);

            // 清除用户收藏总数缓存
            cacheService.evictUserFavoriteCount(username);

            // 清除涉及的文物收藏状态和数量缓存
            var favoriteChanges = userInteraction.getFavoriteChanges();
            for (var change : favoriteChanges) {
                FavoriteAction favorite = (FavoriteAction) change.getEntity();
                Long relicsId = favorite.getRelicsId();

                // 清除收藏状态缓存
                cacheService.evictFavoriteStatus(username, relicsId);

                // 清除文物收藏数量缓存
                cacheService.evictRelicsFavoriteCount(relicsId);
            }

            log.debug("清除收藏相关缓存: {}", username.getValue());
        } catch (Exception e) {
            log.error("清除收藏相关缓存失败: {} - {}",
                     userInteraction.getUsername().getValue(), e.getMessage(), e);
        }
    }

    /**
     * 清除评论相关缓存
     */
    private void evictCommentRelatedCaches(UserInteraction userInteraction) {
        try {
            Username username = userInteraction.getUsername();

            // 清除用户交互聚合根缓存
            cacheService.evictUserInteraction(username);

            // 清除涉及的文物评论缓存
            var commentChanges = userInteraction.getCommentChanges();
            for (var change : commentChanges) {
                CommentAction comment = (CommentAction) change.getEntity();
                Long relicsId = comment.getRelicsId();

                // 清除用户评论列表缓存
                cacheService.evictUserComments(username, relicsId);
                cacheService.evictUserComments(username, null); // 清除所有评论缓存

                // 清除文物评论相关缓存
                cacheService.evictRelicsApprovedComments(relicsId);
                cacheService.evictRelicsCommentCount(relicsId);
            }

            log.debug("清除评论相关缓存: {}", username.getValue());
        } catch (Exception e) {
            log.error("清除评论相关缓存失败: {} - {}",
                     userInteraction.getUsername().getValue(), e.getMessage(), e);
        }
    }
}
