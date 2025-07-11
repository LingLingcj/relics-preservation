package com.ling.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    
    // ==================== 聚合根操作 ====================
    
    @Override
    @Transactional
    public boolean save(UserInteraction userInteraction) {
        try {
            log.debug("保存用户交互: {}", userInteraction.getDisplayName());
            
            // 保存收藏数据
            saveFavorites(userInteraction);
            
            // 保存评论数据
            saveComments(userInteraction);
            
            return true;
            
        } catch (Exception e) {
            log.error("保存用户交互失败: {} - {}", userInteraction.getDisplayName(), e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public Optional<UserInteraction> findByUsername(Username username) {
        try {
            log.debug("查找用户交互: {}", username.getValue());
            
            // 查询收藏数据
            List<UserFavorite> favorites = userFavoriteDao.selectByUsername(
                    username.getValue(), 0, Integer.MAX_VALUE);
            
            // 查询评论数据
            List<UserComment> comments = userCommentDao.selectByUsername(
                    username.getValue(), null, 0, Integer.MAX_VALUE);
            
            if (favorites.isEmpty() && comments.isEmpty()) {
                return Optional.empty();
            }
            
            // 构建聚合根
            UserInteraction userInteraction = buildUserInteraction(username, favorites, comments);
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
            UserFavorite favorite = userFavoriteDao.selectByUsernameAndRelicsId(
                    username.getValue(), relicsId);
            return favorite != null && favorite.getStatus() == 0;
        } catch (Exception e) {
            log.error("检查收藏状态失败: {} - {}", username.getValue(), e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public List<Long> getUserFavoriteRelicsIds(Username username, int page, int size) {
        try {
            int offset = (page - 1) * size;
            List<UserFavorite> favorites = userFavoriteDao.selectByUsername(
                    username.getValue(), offset, size);
            return favorites.stream()
                    .map(UserFavorite::getRelicsId)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取用户收藏列表失败: {} - {}", username.getValue(), e.getMessage(), e);
            return List.of();
        }
    }
    
    @Override
    public long getUserFavoriteCount(Username username) {
        try {
            return userFavoriteDao.countByUsername(username.getValue());
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
            return userFavoriteDao.countByRelicsId(relicsId);
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
            return userCommentDao.countByRelicsId(relicsId);
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
            int offset = (page - 1) * size;
            // 查询已通过审核的评论（comment_status = 1）
            List<UserComment> comments = userCommentDao.selectApprovedCommentsByRelicsId(
                    relicsId, offset, size);
            return comments.stream()
                    .map(this::convertToRelicsComment)
                    .collect(Collectors.toList());
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

    // 统计查询、批量操作、时间范围查询方法已移至对应的应用服务
    
    // ==================== 私有辅助方法 ====================
    
    /**
     * 保存收藏数据
     */
    private void saveFavorites(UserInteraction userInteraction) {
        try {
            String username = userInteraction.getUsername().getValue();
            Set<FavoriteAction> currentFavorites = userInteraction.getFavorites();

            // 获取数据库中现有的收藏记录
            List<UserFavorite> existingFavorites = userFavoriteDao.selectByUsername(
                    username, 0, Integer.MAX_VALUE);

            // 构建现有记录的映射（relicsId -> UserFavorite）
            Map<Long, UserFavorite> existingMap = existingFavorites.stream()
                    .collect(Collectors.toMap(UserFavorite::getRelicsId, f -> f));

            // 构建当前聚合根中的映射（relicsId -> FavoriteAction）
            Map<Long, FavoriteAction> currentMap = currentFavorites.stream()
                    .collect(Collectors.toMap(FavoriteAction::getRelicsId, f -> f));

            // 处理新增和更新
            for (FavoriteAction favorite : currentFavorites) {
                UserFavorite existing = existingMap.get(favorite.getRelicsId());

                if (existing == null) {
                    // 新增收藏记录
                    UserFavorite newFavorite = new UserFavorite();
                    newFavorite.setUsername(username);
                    newFavorite.setRelicsId(favorite.getRelicsId());
                    newFavorite.setStatus(favorite.isDeleted() ? 1 : 0);
                    newFavorite.setCreateTime(favorite.getCreateTime());
                    newFavorite.setUpdateTime(LocalDateTime.now());

                    userFavoriteDao.insert(newFavorite);
                    log.debug("新增收藏记录: {} - {}", username, favorite.getRelicsId());

                } else {
                    // 更新现有记录（主要是状态变化）
                    int newStatus = favorite.isDeleted() ? 1 : 0;
                    if (existing.getStatus() != newStatus) {
                        existing.setStatus(newStatus);
                        existing.setUpdateTime(LocalDateTime.now());
                        userFavoriteDao.update(existing);
                        log.debug("更新收藏记录状态: {} - {} -> {}", username, favorite.getRelicsId(), newStatus);
                    }
                }
            }

            // 处理删除（在聚合根中不存在但在数据库中存在的记录）
            for (UserFavorite existing : existingFavorites) {
                if (!currentMap.containsKey(existing.getRelicsId()) && existing.getStatus() == 0) {
                    // 逻辑删除
                    existing.setStatus(1);
                    existing.setUpdateTime(LocalDateTime.now());
                    userFavoriteDao.update(existing);
                    log.debug("删除收藏记录: {} - {}", username, existing.getRelicsId());
                }
            }

        } catch (Exception e) {
            log.error("保存收藏数据失败: {} - {}", userInteraction.getUsername().getValue(), e.getMessage(), e);
            throw new RuntimeException("保存收藏数据失败", e);
        }
    }
    
    /**
     * 保存评论数据
     */
    private void saveComments(UserInteraction userInteraction) {
        try {
            String username = userInteraction.getUsername().getValue();
            List<CommentAction> currentComments = userInteraction.getComments();

            // 获取数据库中现有的评论记录
            List<UserComment> existingComments = userCommentDao.selectByUsername(
                    username, null, 0, Integer.MAX_VALUE);

            // 构建现有记录的映射（commentId -> UserComment）
            Map<Long, UserComment> existingMap = existingComments.stream()
                    .collect(Collectors.toMap(UserComment::getCommentId, c -> c));

            // 构建当前聚合根中的映射（commentId -> CommentAction）
            Map<Long, CommentAction> currentMap = currentComments.stream()
                    .collect(Collectors.toMap(CommentAction::getId, c -> c));

            // 处理新增和更新
            for (CommentAction comment : currentComments) {
                UserComment existing = existingMap.get(comment.getId());

                if (existing == null) {
                    // 新增评论记录
                    UserComment newComment = new UserComment();
                    newComment.setCommentId(comment.getId());
                    newComment.setUsername(username);
                    newComment.setRelicsId(comment.getRelicsId());
                    newComment.setContent(comment.getContent().getContent());
                    newComment.setCommentStatus(convertCommentStatus(comment.getStatus()));
                    newComment.setStatus(comment.isDeleted() ? 1 : 0);
                    newComment.setCreateTime(comment.getCreateTime());
                    newComment.setUpdateTime(LocalDateTime.now());

                    userCommentDao.insert(newComment);
                    log.debug("新增评论记录: {} - {}", username, comment.getId());

                } else {
                    // 更新现有记录
                    boolean needUpdate = false;

                    // 检查内容是否变化
                    if (!existing.getContent().equals(comment.getContent().getContent())) {
                        existing.setContent(comment.getContent().getContent());
                        needUpdate = true;
                    }

                    // 检查状态是否变化
                    int newStatus = comment.isDeleted() ? 1 : 0;
                    if (existing.getStatus() != newStatus) {
                        existing.setStatus(newStatus);
                        needUpdate = true;
                    }

                    // 检查评论状态是否变化
                    int newCommentStatus = convertCommentStatus(comment.getStatus());
                    if (existing.getCommentStatus() != newCommentStatus) {
                        existing.setCommentStatus(newCommentStatus);
                        needUpdate = true;
                    }

                    if (needUpdate) {
                        existing.setUpdateTime(LocalDateTime.now());
                        userCommentDao.update(existing);
                        log.debug("更新评论记录: {} - {}", username, comment.getId());
                    }
                }
            }

            // 处理删除（在聚合根中不存在但在数据库中存在的记录）
            for (UserComment existing : existingComments) {
                if (!currentMap.containsKey(existing.getCommentId()) && existing.getStatus() == 0) {
                    // 逻辑删除
                    existing.setStatus(1);
                    existing.setUpdateTime(LocalDateTime.now());
                    userCommentDao.update(existing);
                    log.debug("删除评论记录: {} - {}", username, existing.getCommentId());
                }
            }

        } catch (Exception e) {
            log.error("保存评论数据失败: {} - {}", userInteraction.getUsername().getValue(), e.getMessage(), e);
            throw new RuntimeException("保存评论数据失败", e);
        }
    }
    
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
}
