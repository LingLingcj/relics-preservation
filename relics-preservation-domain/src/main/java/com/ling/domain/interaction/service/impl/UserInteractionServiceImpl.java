package com.ling.domain.interaction.service.impl;

import com.ling.domain.interaction.adapter.IGalleryManagerRepository;
import com.ling.domain.interaction.adapter.IUserCommentsRepository;
import com.ling.domain.interaction.adapter.IUserFavoritesRepository;
import com.ling.domain.interaction.adapter.IUserInteractionRepository;
import com.ling.domain.interaction.model.entity.GalleryManager;
import com.ling.domain.interaction.model.entity.UserComments;
import com.ling.domain.interaction.model.entity.UserFavorites;
import com.ling.domain.interaction.model.valobj.*;
import com.ling.domain.interaction.service.IUserInteractionService;
import com.ling.domain.user.model.valobj.Username;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户交互服务实现（重构为门面服务）
 * @Author: LingRJ
 * @Description: 协调三个聚合根的操作，保持向后兼容性
 * @DateTime: 2025/7/13
 */
@Service
@Slf4j
public class UserInteractionServiceImpl implements IUserInteractionService {

    @Autowired
    private IUserInteractionRepository userInteractionRepository;

    @Autowired
    private IUserFavoritesRepository userFavoritesRepository;

    @Autowired
    private IUserCommentsRepository userCommentsRepository;

    @Autowired
    private IGalleryManagerRepository galleryManagerRepository;
    
    // ==================== 收藏相关 ====================
    
    @Override
    @Transactional
    public InteractionResult addFavorite(String username, Long relicsId) {
        try {
            log.info("用户 {} 尝试收藏文物 {}", username, relicsId);

            // 获取或创建用户收藏聚合根
            UserFavorites userFavorites = getUserFavoritesOrCreate(username);

            // 执行收藏操作
            InteractionResult result = userFavorites.addFavorite(relicsId);

            if (result.isSuccess()) {
                // 保存聚合根
                boolean saved = userFavoritesRepository.saveIncremental(userFavorites);
                if (!saved) {
                    log.error("保存用户收藏失败: {}", username);
                    return InteractionResult.failure("保存失败");
                }
            }

            return result;

        } catch (Exception e) {
            log.error("添加收藏失败: {} - {}", username, e.getMessage(), e);
            return InteractionResult.failure("添加收藏失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public InteractionResult removeFavorite(String username, Long relicsId) {
        try {
            log.info("用户 {} 尝试取消收藏文物 {}", username, relicsId);

            Optional<UserFavorites> userFavoritesOpt = userFavoritesRepository.findByUsername(Username.of(username));
            if (userFavoritesOpt.isEmpty()) {
                return InteractionResult.failure("用户收藏记录不存在");
            }

            UserFavorites userFavorites = userFavoritesOpt.get();
            InteractionResult result = userFavorites.removeFavorite(relicsId);

            if (result.isSuccess()) {
                boolean saved = userFavoritesRepository.saveIncremental(userFavorites);
                if (!saved) {
                    log.error("保存用户收藏失败: {}", username);
                    return InteractionResult.failure("保存失败");
                }

                // 同时从所有收藏馆中移除该文物
                removeRelicsFromAllGalleries(username, relicsId);
            }

            return result;

        } catch (Exception e) {
            log.error("取消收藏失败: {} - {}", username, e.getMessage(), e);
            return InteractionResult.failure("取消收藏失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean isFavorited(String username, Long relicsId) {
        try {
            Optional<UserFavorites> userFavoritesOpt = userFavoritesRepository.findByUsername(Username.of(username));
            return userFavoritesOpt.map(uf -> uf.isFavorited(relicsId)).orElse(false);
        } catch (Exception e) {
            log.error("检查收藏状态失败: {} - {}", username, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public FavoriteListResult getUserFavorites(String username, int page, int size) {
        try {
            Optional<UserFavorites> userFavoritesOpt = userFavoritesRepository.findByUsername(Username.of(username));
            if (userFavoritesOpt.isEmpty()) {
                return new FavoriteListResult(List.of(), 0, page, size, false);
            }

            UserFavorites userFavorites = userFavoritesOpt.get();
            List<Long> favoritedRelicsIds = userFavorites.getFavoritedRelicsIds();

            // 简单分页（实际应该在仓储层实现）
            int start = (page - 1) * size;
            int end = Math.min(start + size, favoritedRelicsIds.size());

            List<FavoriteAction> favorites = favoritedRelicsIds.subList(start, end).stream()
                    .map(FavoriteAction::create)
                    .collect(Collectors.toList());

            boolean hasNext = end < favoritedRelicsIds.size();

            return new FavoriteListResult(favorites, favoritedRelicsIds.size(), page, size, hasNext);

        } catch (Exception e) {
            log.error("获取用户收藏列表失败: {} - {}", username, e.getMessage(), e);
            return new FavoriteListResult(List.of(), 0, page, size, false);
        }
    }
    
    // ==================== 评论相关 ====================
    
    @Override
    @Transactional
    public InteractionResult addComment(String username, Long relicsId, String content) {
        try {
            log.info("用户 {} 尝试评论文物 {}: {}", username, relicsId,
                    content.length() > 50 ? content.substring(0, 50) + "..." : content);

            // 获取或创建用户评论聚合根
            UserComments userComments = getUserCommentsOrCreate(username);

            // 执行评论操作
            InteractionResult result = userComments.addComment(relicsId, content);

            if (result.isSuccess()) {
                // 保存聚合根
                boolean saved = userCommentsRepository.saveIncremental(userComments);
                if (!saved) {
                    log.error("保存用户评论失败: {}", username);
                    return InteractionResult.failure("保存失败");
                }
            }

            return result;

        } catch (Exception e) {
            log.error("添加评论失败: {} - {}", username, e.getMessage(), e);
            return InteractionResult.failure("添加评论失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public InteractionResult deleteComment(String username, Long commentId) {
        try {
            log.info("用户 {} 尝试删除评论 {}", username, commentId);

            Optional<UserComments> userCommentsOpt = userCommentsRepository.findByUsername(Username.of(username));
            if (userCommentsOpt.isEmpty()) {
                return InteractionResult.failure("用户评论记录不存在");
            }

            UserComments userComments = userCommentsOpt.get();
            InteractionResult result = userComments.deleteComment(commentId);

            if (result.isSuccess()) {
                boolean saved = userCommentsRepository.saveIncremental(userComments);
                if (!saved) {
                    log.error("保存用户评论失败: {}", username);
                    return InteractionResult.failure("保存失败");
                }
            }

            return result;

        } catch (Exception e) {
            log.error("删除评论失败: {} - {}", username, e.getMessage(), e);
            return InteractionResult.failure("删除评论失败: " + e.getMessage());
        }
    }
    
    @Override
    public CommentListResult getUserComments(String username, Long relicsId, int page, int size) {
        try {
            Optional<UserComments> userCommentsOpt = userCommentsRepository.findByUsername(Username.of(username));
            if (userCommentsOpt.isEmpty()) {
                return new CommentListResult(List.of(), 0, page, size, false);
            }

            UserComments userComments = userCommentsOpt.get();
            List<CommentAction> comments = userComments.getComments(relicsId);

            // 简单分页（实际应该在仓储层实现）
            int start = (page - 1) * size;
            int end = Math.min(start + size, comments.size());

            List<CommentAction> pageComments = comments.subList(start, end);
            boolean hasNext = end < comments.size();

            return new CommentListResult(pageComments, comments.size(), page, size, hasNext);

        } catch (Exception e) {
            log.error("获取用户评论列表失败: {} - {}", username, e.getMessage(), e);
            return new CommentListResult(List.of(), 0, page, size, false);
        }
    }

    @Override
    public RelicsCommentListResult getRelicsComments(Long relicsId, int page, int size) {
        try {
            // 参数验证
            if (relicsId == null || relicsId <= 0) {
                log.warn("文物ID无效: {}", relicsId);
                return RelicsCommentListResult.empty(relicsId, page, size);
            }

            if (page < 1) page = 1;
            if (size < 1) size = 10;
            if (size > 100) size = 100; // 限制最大页面大小

            // 获取已通过审核的评论列表
            List<RelicsComment> comments = userInteractionRepository.getApprovedCommentsByRelicsId(relicsId, page, size);

            // 获取总数
            Long totalCount = userInteractionRepository.countApprovedCommentsByRelicsId(relicsId);

            log.debug("获取文物评论成功: relicsId={}, page={}, size={}, totalCount={}",
                     relicsId, page, size, totalCount);

            return RelicsCommentListResult.success(comments, totalCount, page, size, relicsId);

        } catch (Exception e) {
            log.error("获取文物评论列表失败: relicsId={}, page={}, size={} - {}",
                     relicsId, page, size, e.getMessage(), e);
            return RelicsCommentListResult.empty(relicsId, page, size);
        }
    }


    // ==================== 私有辅助方法 ====================

    /**
     * 获取用户收藏聚合根，如果不存在则创建
     */
    private UserFavorites getUserFavoritesOrCreate(String username) {
        Optional<UserFavorites> userFavoritesOpt = userFavoritesRepository.findByUsername(Username.of(username));
        return userFavoritesOpt.orElseGet(() -> createUserFavorites(username));
    }

    /**
     * 获取用户评论聚合根，如果不存在则创建
     */
    private UserComments getUserCommentsOrCreate(String username) {
        Optional<UserComments> userCommentsOpt = userCommentsRepository.findByUsername(Username.of(username));
        return userCommentsOpt.orElseGet(() -> createUserComments(username));
    }

    /**
     * 获取收藏馆管理聚合根，如果不存在则创建
     */
    private GalleryManager getGalleryManagerOrCreate(String username) {
        Optional<GalleryManager> galleryManagerOpt = galleryManagerRepository.findByUsername(Username.of(username));
        return galleryManagerOpt.orElseGet(() -> createGalleryManager(username));
    }

    /**
     * 创建用户收藏聚合根
     */
    private UserFavorites createUserFavorites(String username) {
        try {
            UserFavorites userFavorites = UserFavorites.create(Username.of(username));
            boolean saved = userFavoritesRepository.saveIncremental(userFavorites);
            if (!saved) {
                throw new RuntimeException("创建用户收藏失败");
            }
            return userFavorites;
        } catch (Exception e) {
            log.error("创建用户收藏失败: {} - {}", username, e.getMessage(), e);
            throw new RuntimeException("创建用户收藏失败: " + e.getMessage());
        }
    }

    /**
     * 创建用户评论聚合根
     */
    private UserComments createUserComments(String username) {
        try {
            UserComments userComments = UserComments.create(Username.of(username));
            boolean saved = userCommentsRepository.saveIncremental(userComments);
            if (!saved) {
                throw new RuntimeException("创建用户评论失败");
            }
            return userComments;
        } catch (Exception e) {
            log.error("创建用户评论失败: {} - {}", username, e.getMessage(), e);
            throw new RuntimeException("创建用户评论失败: " + e.getMessage());
        }
    }

    /**
     * 创建收藏馆管理聚合根
     */
    private GalleryManager createGalleryManager(String username) {
        try {
            GalleryManager galleryManager = GalleryManager.create(Username.of(username));
            boolean saved = galleryManagerRepository.saveIncremental(galleryManager);
            if (!saved) {
                throw new RuntimeException("创建收藏馆管理失败");
            }
            return galleryManager;
        } catch (Exception e) {
            log.error("创建收藏馆管理失败: {} - {}", username, e.getMessage(), e);
            throw new RuntimeException("创建收藏馆管理失败: " + e.getMessage());
        }
    }

    /**
     * 从所有收藏馆中移除指定文物
     */
    private void removeRelicsFromAllGalleries(String username, Long relicsId) {
        try {
            Optional<GalleryManager> galleryManagerOpt = galleryManagerRepository.findByUsername(Username.of(username));
            if (galleryManagerOpt.isPresent()) {
                GalleryManager galleryManager = galleryManagerOpt.get();
                // 遍历所有收藏馆，移除该文物
                for (CollectionGallery gallery : galleryManager.getAllGalleries()) {
                    if (gallery.containsRelics(relicsId)) {
                        galleryManager.removeRelicsFromGallery(gallery.getGalleryId(), relicsId);
                    }
                }
                // 保存变更
                galleryManagerRepository.saveIncremental(galleryManager);
            }
        } catch (Exception e) {
            log.error("从收藏馆移除文物失败: {} - {}", username, e.getMessage(), e);
            // 这里不抛出异常，因为这是一个辅助操作
        }
    }
}
